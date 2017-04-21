import flask
from flask_apscheduler import APScheduler
from flask.ext.sqlalchemy import SQLAlchemy
from flask_restful import Api
from flask.ext.restful import abort
from flask.ext.migrate import Migrate
from flask.ext.cors import CORS
import googlemaps
import sys
from apscheduler.schedulers.background import BackgroundScheduler

app = flask.Flask(__name__)
app.config.from_object('config')
db = SQLAlchemy(app)
migrate = Migrate(app, db)

key = 'AIzaSyDmNBpYDBoxkwYTW5Aw9H3YrEXaSi-tnAo'
gmaps = googlemaps.Client(key=key)

CORS(app, resources=r'/*', allow_headers='*')

@app.errorhandler(404)
def not_found(error):
    err = {'message': "Resource doesn't exist."}
    return flask.jsonify(**err)

from app.blog.resources import blog_bp
from app.auth.resources import auth_bp
from app.pages.resources import page_bp
from app.routes.resources import route_bp
from app.starred.resources import starred_bp
from app.history.resources import history_bp
from app.history.resources import test_scheduler

app.register_blueprint(
    blog_bp,
    url_prefix='/blog'
)

app.register_blueprint(
    auth_bp,
    url_prefix='/auth'
)

app.register_blueprint(
    page_bp,
    url_prefix='/page'
)

app.register_blueprint(
    route_bp,
    url_prefix='/routes'
)

app.register_blueprint(
    starred_bp,
    url_prefix='/starred'
)

app.register_blueprint(
    history_bp,
    url_prefix='/history'
)

sched = BackgroundScheduler(timezone='MST')
sched.add_job(test_scheduler, 'interval', id='my_job_id', seconds=60)
sched.start()