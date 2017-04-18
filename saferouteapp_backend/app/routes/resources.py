from app import db

from flask import Blueprint, g, jsonify, request

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with

from app.base.decorators import login_required, has_permissions
from app.routes.models import Route

from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort
from crime_helper import get_safe_routes_raw
import json

route_bp = Blueprint('route_api', __name__)
api = Api(route_bp)

class Route(Resource):
    add_args = {
        'frm': fields.String(required=True),
        'to': fields.String(required=True),
    }

    @use_kwargs(add_args)
    def get(self, frm, to):
        # page = Page.query.filter_by(slug=slug).first()
        # if not page:
        #     abort(404, message="Page {} doesn't exist".format(slug))
        # return page
        # r = {
        #     "content": "ndull", 
        #     "id": 0, 
        #     "slug": "ndull", 
        #     "title": "ndull"
        # }
        routes = get_safe_routes_raw(frm, to)
        return routes
        #return jsonify(r)

    @use_kwargs(add_args)
    def post(self, frm, to):
        routes = get_safe_routes_raw(frm, to)
        return routes

api.add_resource(Route, '')
