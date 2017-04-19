from flask import Blueprint, g

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse

from app import db
from app.auth.models import Permission, User
from app.base.decorators import login_required
from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort
import pdb

auth_bp = Blueprint('auth_api', __name__)
api = Api(auth_bp)

perm_fields = {
    'name': fields.String,
    'code': fields.String,
}

user_fields = {
    'id': fields.Integer,
    'created': fields.DateTime,
    'modified': fields.DateTime,
    'username': fields.String,
    'password': fields.String,
    'permissions': fields.Nested(perm_fields),
}

token_fields = {
    'token': fields.String,
}


class UserBase(Resource):

    def get_user(self, username):
        user = User.query.filter_by(username=username).first()
        if not user:
            abort(404, message="User {} doesn't exist".format(username))
        return user

    def add_permissions(self, user, perms):
        user.permissions = []
        if perms is None:
            perms = []
        for p in perms:
            user.add_permission(p)

class UserDetail(UserBase):

    put_parser = reqparse.RequestParser()
    put_parser.add_argument('cur_password', type=str)
    put_parser.add_argument('new_password', type=str)
    put_parser.add_argument('permissions', type=str, action='append')

    @marshal_with(user_fields)
    @login_required
    def get(self, username):
        user = self.get_user(username)
        return user 

    @login_required
    def delete(self, username):
        user = self.get_user(username)
        db.session.delete(user)
        db.session.commit()
        return {}, 204

    @marshal_with(user_fields)
    @login_required
    def put(self, username):
        args = self.put_parser.parse_args()
        user = self.get_user(username)
        # Update password if current one matches
        if None not in [args['cur_password'], args['new_password']]:
            if user.check_password(args['cur_password']):
                user.set_password(args['new_password'])
            else:
                abort(400, message="Invalid password")
        # Update permissions
        self.add_permissions(user, args['permissions'])
        db.session.add(user)
        db.session.commit()
        return user, 200


class UserList(UserBase):

    parser = reqparse.RequestParser()
    parser.add_argument('username', type=str)
    parser.add_argument('password', type=str)
    parser.add_argument('permissions', type=str, action='append')

    @marshal_with(user_fields)
    @login_required
    def get(self):
        user = User.query.all()
        return user 

    @marshal_with(user_fields)
    def post(self):
        # registration, setting uname/pass as email/uid
        parsed_args = self.parser.parse_args()
        user = User(
            username=parsed_args['email']
        )
        user.set_password(parsed_args['uid'])
        self.add_permissions(user, parsed_args['permissions'])
        db.session.add(user)
        db.session.commit()
        return user, 201

class Register(UserBase):
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
        # return uid, 201

class AuthToken(UserBase):
    add_args = {
        'email': fields.String(required=True),
        'uid': fields.String(required=True),
    }
    @use_kwargs(add_args)
    def post(self):
        args = self.token_parser.parse_args()
        user = self.get_user(email) 
        if user.check_password(uid):
            token = user.generate_auth_token()
            return {'token': token.decode('ascii')}, 200
        else:
            abort(401, message="Invalid login info")

class PushToken(UserBase):
    add_args = {
        'push_token': fields.String(required=True),
    }

    @use_kwargs(add_args)
    @login_required
    def post(self, push_token):
        user = g.user
        try:
            user.set_push_token(push_token)
            db.session.add(user)
            db.session.commit()
            return "success", 200
        except:
            return "err", 400

api.add_resource(AuthToken, '/login/')
api.add_resource(UserDetail, '/users/<string:username>')
api.add_resource(UserList, '/users/')
api.add_resource(Register, '/register/')
api.add_resource(PushToken, '/push_token/')

