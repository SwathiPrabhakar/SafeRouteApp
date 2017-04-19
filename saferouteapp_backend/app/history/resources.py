from flask import Blueprint, g

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse

from app import db
from app.auth.models import Permission, User
from app.base.decorators import login_required
from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort

history_bp = Blueprint('history_api', __name__)

class Record(Resource):
    add_args = {
        'email': fields.String(required=True),
        'uid': fields.String(required=True),
    }

    @use_kwargs(add_args)
    def post(self, email, uid):
        # todo store and verify uid 
        user = User.query.filter_by(username=email).first()
        if not user:
            user = User(
                username=email
            )
            user.set_password(uid)
            db.session.add(user)
            db.session.commit()
        token = user.generate_auth_token()
        return {'token': token.decode('ascii')}, 200