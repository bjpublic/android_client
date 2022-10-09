import os

basedir = os.path.abspath(os.path.dirname(__file__))


class Config:
    SECRET_KEY = os.getenv('SECRET_KEY', 'my_secret_key')
    DEBUG = False
    ERROR_INCLUDE_MESSAGE = True
    ERROR_404_HELP = False
    DEFAULT_VALIDATION_STATUS = 400
    DEFAULT_VALIDATION_MESSAGE = "Invalid arguments"
    JWT_ACCESS_TOKEN_EXPIRES = 300
    JWT_REFRESH_TOKEN_EXPIRES = 2592000


class DevelopmentConfig(Config):
    DEBUG = True
    SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(basedir, 'dev.sqlite')
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_ECHO = False


class TestingConfig(Config):
    DEBUG = True
    TESTING = True
    SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(basedir, 'testing.db')
    PRESERVE_CONTEXT_ON_EXCEPTION = False
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_ECHO = False


CONFIG_SET = {'tests': TestingConfig, 'dev': DevelopmentConfig}

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = os.path.join(os.getcwd(), 'service_account.json')


def get_config_by_name(name):
    return CONFIG_SET[name]
