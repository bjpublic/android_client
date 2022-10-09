import os
import uuid
import string
import time
from urllib.parse import urljoin
from werkzeug import exceptions
from flask import request, send_from_directory, make_response, jsonify
from flask_restx import Resource, Api, abort, Namespace
from webargs import fields, validate
from webargs.flaskparser import parser, use_args, use_kwargs
from flask_jwt_extended import (
    JWTManager, jwt_required,
    create_access_token, create_refresh_token,
    get_jwt_identity, decode_token
)
from werkzeug.http import generate_etag
from sqlalchemy.orm import noload, joinedload, lazyload

from models import db, User, user_schema, Question, question_schema, \
    question_answer_schema, \
    Answer, answer_schema, Follower, follower_schema, Device, device_schema, func

from sqlalchemy import and_

from http import HTTPStatus

import push


def today():
    return time.strftime('%Y-%m-%d')


api = Namespace('V2', description='v2 apis')
jwt = JWTManager()


@parser.error_handler
def handle_error(error, req, schema, *, error_status_code, error_headers):
    status_code = error_status_code or parser.DEFAULT_VALIDATION_STATUS
    print(error)

    try:
        abort(status_code)
    except exceptions.HTTPException as err:
        # err.description = repr(error.messages)
        raise err


@api.route('/token')
class TokenResource(Resource):

    @use_args({
        'grant_type': fields.Str(required=True, validate=validate.OneOf(['password', 'refresh_token'])),
        'username': fields.Str(required=False, validate=[
            validate.ContainsOnly(string.ascii_lowercase + string.digits),
            validate.Length(min=5, max=20)
        ]),
        'password': fields.Str(required=False, validate=validate.Length(min=8, max=16)),
        'refresh_token': fields.Str(required=False),
    }, location='form')
    def post(self, args):
        grant_type = args['grant_type']
        if grant_type == 'password':
            user = User.query.get(args['username'])
            if not user or not user.check_password(args['password']):
                raise exceptions.Unauthorized("The username or password was not correct")
            uid = user.id
        elif grant_type == 'refresh_token':
            refresh_token = args['refresh_token']
            decoded_token = decode_token(refresh_token)

            if decoded_token['type'] != 'refresh':
                # raise errors.InvalidRefreshToken("Invalid refresh token")
                raise exceptions.BadRequest("Invalid refresh token")
            uid = decoded_token['sub']
        else:
            raise exceptions.BadRequest("Unsupported grant type")

        access_token = create_access_token(identity=uid)
        refresh_token = create_refresh_token(identity=uid)
        # TODO header no-store
        return {'access_token': access_token,
                'refresh_token': refresh_token,
                'token_type': 'Bearer'}


@api.route('/user/push-tokens')
class DeviceResource(Resource):
    @jwt_required()
    @use_args({
        'token': fields.Str(required=True),
    }, location='form')
    def post(self, args):
        token = args.get('token', None)

        uid = get_jwt_identity()

        device = Device.query.get((uid, token))
        if not device:
            device = Device(
                uid=uid,
                token=token
            )

            db.session.add(device)
            db.session.commit()
        return None, HTTPStatus.NO_CONTENT


@api.route('/users/<uid>')
class UserResource(Resource):

    @jwt_required()
    def get(self, uid):
        user = User.query.get(uid)

        if not user:
            raise exceptions.NotFound("User not found")

        data = user_schema.dump(user)
        data['is_following'] = Follower.query.get((uid, get_jwt_identity())) is not None
        res = jsonify(data)

        time.sleep(0.5)

        return res


@api.route('/questions/<qid>')
class QuestionResource(Resource):

    @jwt_required()
    def get(self, qid):
        if qid > today():
            raise exceptions.NotFound("Question not found")

        question = Question.query.get(qid)
        if not question:
            raise exceptions.NotFound("Question not found")

        dump = question_schema.dump(question)
        res = make_response(dump)

        if request.if_none_match.is_strong(generate_etag(res.get_data())):
            res.cache_control.no_cache = True
            res.cache_control.no_store = True
            return None, HTTPStatus.NOT_MODIFIED

        res.add_etag()
        return res


@api.route('/questions')
class QuestionCollection(Resource):
    @use_args({
        'from_date': fields.Str(required=True, validate=validate.Length(equal=10)),
        'page_size': fields.Int(required=False, validate=validate.Range(min=1, max=50)),
    }, location='query')
    @jwt_required()
    def get(self, args):
        from_date = args.get('from_date')
        page_size = args.get('page_size', 5)

        if from_date > today():
            raise exceptions.BadRequest("from_date is invalid")

        questions = Question.query.filter(Question.id <= from_date).order_by(
            Question.id.desc()).limit(page_size).all()

        if not questions or questions[0].id != from_date:
            raise exceptions.NotFound("Question not found")

        time.sleep(0.5)

        data = question_schema.dump(questions, many=True)
        res = jsonify(data)

        res.cache_control.max_age = 120

        return res


@api.route('/users/<uid>/answers')
class UsersAnswerCollectionResource(Resource):
    @jwt_required()
    @use_args({
        'from_date': fields.Str(required=False, validate=validate.Length(equal=10)),
        'page_size': fields.Str(required=False)
    }, location='query')
    def get(self, args, uid):
        page_size = args.get('page_size', 3)
        from_date = args.get('from_date', None)

        if from_date:
            answers = Answer.query.filter(and_(Answer.uid == uid, Answer.qid <= min(from_date, today()))).order_by(
                Answer.qid.desc()).limit(page_size).all()
        else:
            answers = Answer.query.filter(and_(Answer.uid == uid, Answer.qid <= today())).order_by(
                Answer.qid.desc()).limit(page_size).all()

        questions = Question.query.filter(Question.id.in_([a.qid for a in answers])).order_by(Question.id.desc()).all()

        question_dict = dict([(q.id, q) for q in questions])

        return question_answer_schema.dump(
            [{'question': question_dict[answer.qid], 'answer': answer} for answer in answers], many=True)


@api.route('/questions/<qid>/answers')
class AnswerCollectionResource(Resource):
    @jwt_required()
    def get(self, qid):
        data = answer_schema.dump(Answer.query.options(joinedload(Answer.answerer)).filter_by(qid=qid).all(), many=True)
        res = jsonify(data)

        if request.if_none_match.is_strong(generate_etag(res.get_data())):
            res.cache_control.no_cache = True
            res.cache_control.no_store = True
            return None, HTTPStatus.NOT_MODIFIED

        res.add_etag()
        return res

    @jwt_required()
    @use_args({
        'text': fields.Str(required=False),
        'photo': fields.Str(required=False)
    }, location='form')
    def post(self, args, qid):
        text = args.get('text', None) or None
        photo = args.get('photo', None) or None

        question = Question.query.get(qid)
        if not question:
            raise exceptions.NotFound("Question not found")

        uid = get_jwt_identity()

        answer = Answer.query.get((uid, qid))
        if answer:
            raise exceptions.Conflict("Already answered")

        answer = Answer(uid=uid, qid=qid, text=text, photo=photo)

        db.session.add(answer)

        user = User.query.get(uid)
        user.answer_count += 1
        db.session.add(user)

        db.session.commit()

        devices = Device.query.filter(
            Device.uid.in_(Follower.query.with_entities(Follower.follower_id).filter(Follower.uid == uid)))

        tokens = list(map(lambda device: device.token, devices))
        push.send(tokens, {'type': 'answer', 'qid': qid, 'uid': uid, 'username': user.name})

        return answer_schema.dump(answer), HTTPStatus.CREATED, {'Location': urljoin(request.url, uid)}


@api.route('/questions/<qid>/answers/<uid>')
class QuestionAnswerResource(Resource):
    @jwt_required()
    def get(self, qid, uid):
        answer = Answer.query.options(joinedload(Answer.answerer)).get((uid, qid))
        if not answer:
            raise exceptions.NotFound("Answer not found")
        return answer_schema.dump(answer)

    @jwt_required()
    @use_args({
        'text': fields.Str(required=False),
        'photo': fields.Str(required=False)
    }, location='form')
    def put(self, args, qid, uid):
        text = args.get('text', None) or None
        photo = args.get('photo', None) or None

        if uid != get_jwt_identity():
            raise exceptions.Forbidden("Permission denied")

        if qid > today():
            raise exceptions.NotFound("Question not found")

        question = Question.query.get(qid)
        if not question:
            raise exceptions.NotFound("Question not found")

        answer = Answer.query.get((uid, qid))
        if not answer:
            raise exceptions.NotFound("Answer not found")

        answer.text = text
        answer.photo = photo

        db.session.add(answer)
        db.session.commit()

        return answer_schema.dump(answer)

    @jwt_required()
    def delete(self, qid, uid):
        if uid != get_jwt_identity():
            raise exceptions.Forbidden("Permission denied")

        answer = Answer.query.options(joinedload(Answer.answerer)).get((uid, qid))
        if not answer:
            raise exceptions.NotFound("Answer not found")

        db.session.delete(answer)

        user = User.query.get(uid)
        user.answer_count -= 1
        db.session.add(user)

        db.session.commit()

        return None, HTTPStatus.NO_CONTENT


@api.route('/images')
class ImageCollectionResource(Resource):

    @jwt_required()
    @use_kwargs(
        {'image': fields.Field(required=True, validate=lambda file: file.mimetype == 'image/jpeg' or 'image/png')},
        location='files')
    def post(self, image):
        path = os.path.join(os.getcwd(), 'images')
        if not os.path.exists(path):
            os.mkdir(path)

        fname = uuid.uuid4().hex
        image.save(os.path.join(path, fname))

        image_url = urljoin(request.host_url, f'v2/images/{fname}')
        return {'url': image_url}, HTTPStatus.CREATED, {'Location': image_url }


@api.route('/images/<fname>')
class ImageResource(Resource):

    def get(self, fname):
        path = os.path.join(os.getcwd(), 'images', fname)
        if not os.path.exists(path):
            raise exceptions.NotFound("Image not found")

        if open(path, 'rb').read(3) == b'\xff\xd8\xff':
            mimetype = 'image/jpeg'
        else:
            mimetype = 'image/png'

        return send_from_directory('images', fname, mimetype=mimetype)


@api.route('/user/following/<followee_uid>')
class FollowerResource(Resource):

    @jwt_required()
    def post(self, followee_uid):
        if not User.query.get(followee_uid):
            raise exceptions.NotFound('User not found')

        uid = get_jwt_identity()
        if uid == followee_uid:
            raise exceptions.BadRequest("Can't follow yourself")

        relation = Follower.query.get((followee_uid, uid))
        if relation:
            raise exceptions.Conflict

        relation = Follower(uid=followee_uid, follower_id=uid)
        db.session.add(relation)

        followee = User.query.get(followee_uid)
        followee.follower_count += 1
        db.session.add(followee)

        follower = User.query.get(uid)
        follower.following_count += 1
        db.session.add(follower)

        db.session.commit()

        devices = Device.query.filter(
            Device.uid.in_(Follower.query.with_entities(Follower.uid).filter(Follower.uid == uid)))

        tokens = list(map(lambda device: device.token, devices))
        push.send(tokens, {'type': 'follow', 'uid': uid, 'username': follower.name})

        return None, HTTPStatus.NO_CONTENT

    @jwt_required()
    def delete(self, followee_uid):
        uid = get_jwt_identity()
        if uid == followee_uid:
            raise exceptions.BadRequest("Can't follow yourself")

        relation = Follower.query.get((followee_uid, uid))
        if not relation:
            raise exceptions.NotFound("Not following")

        db.session.delete(relation)

        followee = User.query.get(followee_uid)
        followee.follower_count -= 1
        db.session.add(followee)

        follower = User.query.get(uid)
        follower.following_count -= 1
        db.session.add(follower)

        db.session.commit()

        return None, HTTPStatus.NO_CONTENT