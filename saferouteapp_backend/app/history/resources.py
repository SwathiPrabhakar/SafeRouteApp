from flask import Blueprint, g

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse

from app import db
from app.auth.models import User
from app.history.models import History
from app.base.decorators import login_required
from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort

history_bp = Blueprint('history_api', __name__)
api = Api(history_bp)

class HistoryStore(Resource):
    add_args = {
        'src_lat': fields.String(required=True),
        'src_long': fields.String(required=True),
        'dest_lat': fields.String(required=True),
        'dest_long': fields.String(required=True),
        'user_id': fields.String(required=True)
    }

    #def post(self, src_lat, src_long, dest_lat, dest_long, user_id):
    @use_kwargs(add_args)
    def post(self, src_lat, src_long, dest_lat, dest_long, user_id):
        # todo store and verify uid 
        record = History(
        	src_lat,
        	src_long,
        	dest_lat,
        	dest_long,
        	user_id
            )
        db.session.add(record)
        db.session.commit()
        return record.id

api.add_resource(HistoryStore, '/')