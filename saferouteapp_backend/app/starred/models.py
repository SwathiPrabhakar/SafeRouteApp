from app import db, gmaps
from app.base.models import Base
import pdb
import json
class Starred(Base):

    __tablename__ = 'starred_locations'

    latitude = db.Column(db.String(255), nullable=False)
    longitude = db.Column(db.String(), nullable=False)
    name = db.Column(db.String, default=False)
    user_id = db.Column(db.String(255), nullable=False, unique=False)
    lat_lng = db.Column(db.String(255), unique=True)

    def __init__(self, lat, lng, user_id, **kwargs):
        self.latitude = lat 
        self.longitude = lng 
        query = gmaps.reverse_geocode((lat, lng))
        if query:
            name = query[0]['formatted_address']
        else:
            name = "{} {}".format(lat, lng)
        self.name = name
        self.user_id = user_id
        self.lat_lng = lat + lng
        # self.set_slug(self.name)

    def __repr__(self):
        return '<Starred %r>' % (self.lat_lng)


