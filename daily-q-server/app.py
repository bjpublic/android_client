import os
from flask import Flask, render_template
from flask_restx import Api
from flask_sqlalchemy import event
from werkzeug.serving import WSGIRequestHandler
import firebase_admin

from config import get_config_by_name
from models import db, ma, enable_foreign_keys
from resources.v1 import api as api_v1
from resources.v2 import api as api_v2
from resources.v2 import jwt
import dummy

WSGIRequestHandler.protocol_version = 'HTTP/1.1'


def init_firebase():
    if not os.path.exists(os.environ['GOOGLE_APPLICATION_CREDENTIALS']):
        return
    firebase_admin.initialize_app()


def index():
    try:
        firebase = firebase_admin.get_app()
    except ValueError:
        firebase = None

    return render_template('index.html', firebase=firebase)


def swagger():
    return render_template('swagger.yaml')

def init_dummy_data():

    db.create_all()
    dummy.create_users()
    dummy.create_relations()
    dummy.create_question_and_answers()


def create_app(config_name='dev'):
    app = Flask(__name__)
    app.config.from_object(get_config_by_name(config_name))
    app.add_url_rule('/', 'index', view_func=index)
    app.add_url_rule('/swagger.json', 'swagger', view_func=swagger)

    api = Api(
        app,
        version='0.1.1',
        doc='/doc')
    api.add_namespace(api_v1, '/v1')
    api.add_namespace(api_v2, '/v2')

    jwt.init_app(app)

    db.init_app(app)
    ma.init_app(app)
    event.listen(db.get_engine(app), 'connect', enable_foreign_keys)

    init_firebase()

    with app.app_context() as context:
        context.push()
        init_dummy_data()
        context.pop()

    return app
