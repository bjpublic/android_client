from urllib.parse import urljoin

from flask import request
import flask_bcrypt
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import func
from flask_marshmallow import Marshmallow
from marshmallow import post_dump
from marshmallow import fields

fields.DateTime.DEFAULT_FORMAT = '%Y-%m-%dT%H:%M:%SZ'

db = SQLAlchemy()
ma = Marshmallow()


def enable_foreign_keys(dbcon, con_rec):
    dbcon.execute('PRAGMA foreign_keys=ON;')


class User(db.Model):
    id = db.Column(db.String(22), primary_key=True)
    password_hash = db.Column(db.String(100), nullable=False)
    name = db.Column(db.String(50), nullable=False)
    description = db.Column(db.Text(), nullable=True)
    photo = db.Column(db.String(1024), nullable=True)
    answer_count = db.Column(db.Integer, nullable=False, default=0)
    follower_count = db.Column(db.Integer, nullable=False, default=0)
    following_count = db.Column(db.Integer, nullable=False, default=0)
    updated_at = db.Column(db.DateTime, onupdate=func.now(), server_default=func.now())
    created_at = db.Column(db.DateTime, nullable=False, server_default=func.now())

    @property
    def password(self):
        raise AttributeError('password: write-only')

    @password.setter
    def password(self, password):
        self.password_hash = flask_bcrypt.generate_password_hash(password).decode('utf-8')

    def check_password(self, password):
        return flask_bcrypt.check_password_hash(self.password_hash, password)


class UserSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = User
        exclude = ('password_hash',)

    @post_dump
    def build_photo_url(self, data, **kwargs):
        if data.get('photo', None):
            data['photo'] = urljoin(request.host_url, f'v2/images/{data["photo"]}')
        return data


user_schema = UserSchema()


class Device(db.Model):
    __table_args__ = (db.PrimaryKeyConstraint('uid', 'token'),)
    uid = db.Column(db.String(22), db.ForeignKey('user.id', ondelete="CASCADE"), nullable=False)
    token = db.Column(db.String(255), nullable=True)  # android 152, 174, ios 64
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=func.now())


class DeviceSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Device


device_schema = DeviceSchema()


class Question(db.Model):
    id = db.Column(db.String(10), nullable=False, primary_key=True)
    text = db.Column(db.String(500), nullable=True)
    answer_count = db.column_property(db.select([db.func.count(db.text('qid'))])
                                      .select_from(db.text('answer'))
                                      .where(db.text('qid == question.id')).correlate_except(db.text('answer'))
                                      .label('answer_count'))

    answerers = db.relationship('User', lazy='noload', secondary='answer', order_by='Answer.created_at',
                                backref='answer.uid')
    updated_at = db.Column(db.DateTime, onupdate=func.now(), server_default=func.now())
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=func.now())


class Answer(db.Model):
    __table_args__ = (db.PrimaryKeyConstraint('uid', 'qid'),)

    uid = db.Column(db.String(22), db.ForeignKey('user.id', ondelete="CASCADE"), nullable=False)
    qid = db.Column(db.String(10), db.ForeignKey('question.id', ondelete="CASCADE"), nullable=False)
    text = db.Column(db.String, nullable=True)
    photo = db.Column(db.String, nullable=True)
    answerer = db.relationship('User', lazy='noload', viewonly=True)
    updated_at = db.Column(db.DateTime, onupdate=func.now(), server_default=func.now())
    created_at = db.Column(db.DateTime, nullable=False, server_default=func.now())


class AnswerSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Answer
        include_fk = True

    answerer = fields.Nested(UserSchema)

    @post_dump
    def remove_skip_values(self, data, **kwargs):
        return {key: value for key, value in data.items() if value is not None}

    @post_dump
    def build_image_url(self, data, **kwargs):
        if data.get('photo', None):
            data['photo'] = urljoin(request.host_url, f'v2/images/{data["photo"]}')
        return data


answer_schema = AnswerSchema()


class QuestionSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Question
        include_fk = False

    answer_count = fields.Integer()

    @post_dump
    def remove_skip_values(self, data, **kwargs):
        return {key: value for key, value in data.items() if value is not None}


class QuestionAnswersSchema(QuestionSchema):
    answers = fields.List(fields.Nested('AnswerSchema'), required=False)


class QuestionUsersSchema(QuestionSchema):
    users = fields.List(fields.Nested('UserSchema'), required=False)


class QuestionAnswersUsersSchema(QuestionAnswersSchema, QuestionUsersSchema):
    users = fields.List(fields.Nested('UserSchema'), required=False)


question_schema = QuestionSchema()
question_answers_schema = QuestionAnswersSchema()
question_users_schema = QuestionUsersSchema()
question_answers_users_schema = QuestionAnswersUsersSchema()


class QuestionAnswerSchema(ma.Schema):
    question = fields.Nested(QuestionSchema)
    answer = fields.Nested(AnswerSchema)


question_answer_schema = QuestionAnswerSchema()


class Follower(db.Model):
    __table_args__ = (db.PrimaryKeyConstraint('uid', 'follower_id'),)

    uid = db.Column(db.String(22), db.ForeignKey('user.id', ondelete='CASCADE'), nullable=False)
    follower_id = db.Column(db.String(22), db.ForeignKey('user.id', ondelete='CASCADE'), nullable=False)

    created_at = db.Column(db.DateTime, nullable=False, server_default=func.now())


class FollowerSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Follower
        load_instance = True

    uid = fields.Str()
    follower_id = fields.Str()


follower_schema = FollowerSchema()
