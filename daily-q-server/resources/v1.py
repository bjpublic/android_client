import datetime

from urllib.parse import urljoin
from werkzeug import exceptions
from flask import request, make_response
from flask_restx import Resource, Namespace, fields as fds
from webargs import fields

from webargs.flaskparser import use_args

from sqlalchemy.orm import joinedload

from models import db, User, Question, question_schema, \
    Answer, answer_schema

from http import HTTPStatus

api = Namespace('Version 1')


@api.route('/hello-world')
class HelloWorld(Resource):
    @api.produces(['text/plain', 'application/json'])
    def get(self):
        accept_type = request.headers.get('Accept')
        if accept_type == 'application/json':
            now = datetime.datetime.utcnow()
            return {'date': now.strftime('%Y-%m-%dT%H:%M:%SZ'),
                    'message': 'Hello, world!'}
        else:  # 'text/plain':
            response = make_response('Hello, world!')
            response.headers['Content-Type'] = 'text/plain'
            return response


@api.route('/questions/<qid>')
class QuestionResource(Resource):
    def get(self, qid):
        question = Question.query.get(qid)
        if not question:
            raise exceptions.NotFound("Question not found")

        return question_schema.dump(question)


@api.route('/questions/<qid>/answers')
class AnswerCollectionResource(Resource):

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

        uid = 'anonymous'  # get_jwt_identity()

        answer = Answer.query.get((uid, qid))
        if answer:
            raise exceptions.Conflict("Already answered")

        answer = Answer(uid=uid, qid=qid, text=text, photo=photo)

        db.session.add(answer)

        user = User.query.get(uid)
        user.answer_count += 1
        db.session.add(user)

        db.session.commit()

        # devices = Device.query.filter(
        #     Device.uid.in_(Relation.query.with_entities(Relation.follower_id).filter(Relation.followee_id == uid)))

        # tokens = list(map(lambda device: device.token, devices))
        # push.send(tokens, {'type': 'answer', 'qid': qid, 'uid': uid, 'username': user.name})

        return answer_schema.dump(answer), HTTPStatus.CREATED, {'Location': urljoin(request.url, uid)}


@api.route('/questions/<qid>/answers/<uid>', endpoint='a')
class QuestionAnswerResource(Resource):

    def get(self, qid, uid):
        # TODO answerer 제거 v1에서는 필요없다
        answer = Answer.query.get((uid, qid))
        if not answer:
            raise exceptions.NotFound("Answer not found")
        return answer_schema.dump(answer)

    @use_args({
        'text': fields.Str(required=False),
    }, location='form')
    def put(self, args, qid, uid):
        text = args.get('text', None) or None
        photo = args.get('photo', None) or None

        if uid != 'anonymous':  # get_jwt_identity():
            raise exceptions.Forbidden("Permission denied")

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

    def delete(self, qid, uid):
        if uid != 'anonymous':
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
