from flask import Blueprint, g
from gcm import GCM
from flask_restful import Api, Resource 
from flask.ext.restful import abort, fields, marshal_with, reqparse
from app.auth.models import User
from app.history.models import History
from webargs.flaskparser import use_args, use_kwargs, parser, abort
from datetime import datetime, timedelta
from geopy.distance import vincenty

API_KEY = 'AIzaSyAEN09LeNCfS92A3_qgXrxIekiKDJC2ets'
gcm = GCM(API_KEY)
message = "Hello Swathi"
data = {'message': message}


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
            if count > 0:
                allHistory3.append({"element":el1, "count":count})

        finalObj = {"result" : allHistory3}
        response = gcm.json_request(registration_ids = registration_ids, data=data)