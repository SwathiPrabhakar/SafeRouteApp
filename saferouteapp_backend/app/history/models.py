from app import db, gmaps
from app.base.models import Base
from app.auth.models import User
import pdb
import json

class History(Base):
    __tablename__ = 'history'

    latitude = db.Column(db.String(255), nullable=False)
    longitude = db.Column(db.String(255), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    
    def __init__(self, lat, lng, user):
        self.latitude = lat 
        self.longitude = lng 
        self.user = user

    def __repr__(self):
        return '<History- %r>' % (self.latitude+self.longitude)