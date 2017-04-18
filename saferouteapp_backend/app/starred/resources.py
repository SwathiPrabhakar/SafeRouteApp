from app import db

from flask import Blueprint, g, jsonify

from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse

from app.base.decorators import login_required, has_permissions
from app.starred.models import Starred as Star

from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort

starred_bp = Blueprint('starred_api', __name__)
api = Api(starred_bp)
import pdb

class Starred(Resource):
    add_args = {
        'place': fields.Nested({
            'lat': fields.Str(required=True),
            'lng': fields.Str(required=True)
            })
    }
    
    @login_required
    def get(self):
        user = g.user
        user_id = user.id
        sl = Star.query.filter_by(user_id=user_id).all()
        locs = []
        for s in sl:
            locs.append({'name': s.name, 'lat': s.latitude, 'lng' : s.longitude})
        return locs, 200
        #return jsonify(r)

    @login_required
    @use_kwargs(add_args)
    def post(self, place):
        user = g.user
        user_id = user.id
        star = Star(place['lat'], place['lng'], user_id)
        db.session.add(star)
        db.session.commit()
        routes = "get_safe_routes_raw(frm, to)"
        return routes, 200

api.add_resource(Starred, '/')
