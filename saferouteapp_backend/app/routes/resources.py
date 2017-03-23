from app import db

from flask import Blueprint, g, jsonify

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse

from app.base.decorators import login_required, has_permissions
from app.routes.models import Route

route_bp = Blueprint('route_api', __name__)
api = Api(route_bp)

route_fields = {
    'id': fields.Integer,
    'created': fields.DateTime,
    'modified': fields.DateTime,
    'title': fields.String,
    'content': fields.String,
    'slug': fields.String,
}

parser = reqparse.RequestParser()
parser.add_argument('title', type=str)
parser.add_argument('content', type=str)


class Route(Resource):

    def get(self):
        # page = Page.query.filter_by(slug=slug).first()
        # if not page:
        #     abort(404, message="Page {} doesn't exist".format(slug))
        # return page 
        r = {
            "content": "ndull", 
            "id": 0, 
            "slug": "ndull", 
            "title": "ndull"
        }
        return jsonify(r)

    @marshal_with(route_fields)
    def post(self):
        # page = Page.query.filter_by(slug=slug).first()
        # if not page:
        #     abort(404, message="Page {} doesn't exist".format(slug))
        # return page
        parsed_args = parser.parse_args()
        # print(parse_args)
        r = {
            "content": "ndull", 
            "id": 1, 
            "slug": "ndull", 
            "title": "ndull"
        }
        return parsed_args

api.add_resource(Route, '/')
