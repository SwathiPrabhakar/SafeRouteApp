from flask import Blueprint, g
from gcm import GCM
from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse
from app import db
from app.auth.models import User
from app.history.models import History
from app.base.decorators import login_required
from webargs import fields, validate
from webargs.flaskparser import use_args, use_kwargs, parser, abort
from datetime import datetime, timedelta
from geopy.distance import vincenty

history_bp = Blueprint('history_api', __name__)
api = Api(history_bp)
API_KEY = 'AIzaSyAEN09LeNCfS92A3_qgXrxIekiKDJC2ets'
gcm = GCM(API_KEY)

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


def test_scheduler():
    currentDateTime = datetime.now()
    currentDay = currentDateTime.weekday()
    dayCheckList = range(5)
    if currentDay in [5,6]:
        dayCheckList = [5,6]

    currentTime = currentDateTime.time()
    currentTimeUpper = (currentDateTime + timedelta(minutes=30)).time()
    currentTimeLower = (currentDateTime - timedelta(minutes=30)).time()
    output = {}
    users = User.query.all()
    for currUser in users:
        user_id = currUser.id
        pushToken = currUser.push_token
        registration_ids = [pushToken]
        allHistory = History.query.filter_by(user_id=user_id).all()
        allHistory2 = []

        for element in allHistory:
            elCreatedTS = element.created
            elDayofWeek = elCreatedTS.weekday()
            elTime = elCreatedTS.time()
            if elDayofWeek in dayCheckList and currentTimeLower<=elTime<=currentTimeUpper:
                allHistory2.append(element)

        allHistory3 = []
        flag = [0]*len(allHistory2)
        for index in range(len(allHistory2)):
            el1 = allHistory2[index]
            count = 0
            for index2 in range(len(allHistory2)):
                el2 = allHistory2[index2]
                src = (float(el1.src_latitude), float(el1.src_longitude))
                dest = (float(el2.src_latitude), float(el2.src_longitude))
                dist = vincenty(src, dest).miles
                print dist, el1, el2, flag[index2]
                if dist<=0.5 and flag[index2]==0 :
                    flag[index2] = 1
                    count = count + 1
                    print "hello"
            if count>5:
                allHistory3.append({"count":count, "src_lat" : el1.src_latitude, "src_lng" : el1.src_longitude, "dest_lat" : el1.dest_latitude, "dest_lng" : el1.dest_longitude})

        # if len(allHistory3) > 0:
        #     response = gcm.json_request(registration_ids = registration_ids, data = { "message" : finalObj} )    
        #finalObj = {"result" : allHistory3}
        
        # Hardcode source locations for testing push notfications
        # Home : 33.4284328,-111.9501358
        # Nobel Library : 33.4201427,-111.9285955
        finalObj = { "result" : [ {"count": 3, "src_lat" : "33.4284328", "src_lng" : "-111.9501358", "dest_lat" : "33.415661", "dest_lng" : "-111.931986"}, {"count": 7, "src_lat" : "33.4284328", "src_lng" : "-111.9501358", "dest_lat" : "33.4235668", "dest_lng" : "-111.9392688"} ] }
        print finalObj
        response = gcm.json_request(registration_ids = registration_ids, data = { "message" : finalObj} )
