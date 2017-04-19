from app import db, gmaps
from app.base.models import Base
from app.auth.models import User
import pdb
import json

class History(Base):
    __tablename__ = 'history'

    src_latitude = db.Column(db.String(255), nullable=False)
    src_longitude = db.Column(db.String(255), nullable=False)
    dest_latitude = db.Column(db.String(255), nullable=False)
    dest_longitude = db.Column(db.String(255), nullable=False)
    user_id = db.Column(db.Integer, nullable=False)
    
    def __init__(self, src_lat, src_lng, dest_lat, dest_lng, user_id, **kwargs):
        self.src_latitude = src_lat 
        self.src_longitude = src_lng 
        self.dest_latitude = dest_lat  
        self.dest_longitude = dest_lng 
        self.user_id = user_id

    def __repr__(self):
        return '<History- %r>' % (self.src_latitude+self.src_longitude)