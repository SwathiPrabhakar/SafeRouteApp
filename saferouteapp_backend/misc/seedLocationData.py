from app.History.models import History
from datetime import timedelta, datetime

homeLat = "33.4284328"
homeLng = "-111.9501358"
sdfcLat = "33.415661"
sdfcLng = "-111.931986"
byLat = "33.4235668"
byLng = "-111.9392688"


dt_start = datetime.strptime("2017-03-01 00:00:00", "%Y-%m-%d %H:%M:%S")
for x in range(30):
	# weekday(): Monday 0, Sunday 6
	day_of_week = dt_start.weekday()
	# weekend
	if day_of_week == 5 or day_of_week == 6:
		# to sdfc
		dt_rec = dt_start + timedelta(hours=17)
		record = History( homeLat, homeLng, sdfcLat, sdfcLng, user_id, created = dt_rec)
		db.session.add(record)
		db.session.commit()

		# back from sdfc
		dt_rec = dt_start + timedelta(hours=19)
		record = History( sdfcLat, sdfcLng, homeLat, homeLng, user_id, created = dt_rec)
		db.session.add(record)
		db.session.commit()

	else :
		# to sdfc
		dt_rec = dt_start + timedelta(hours=17)
		record = History( homeLat, homeLng, sdfcLat, sdfcLng, user_id, created = dt_rec)
		db.session.add(record)
		db.session.commit()

		# back from sdfc
		dt_rec = dt_start + timedelta(hours=19)
		record = History( sdfcLat, sdfcLng, homeLat, homeLng, user_id, created = dt_rec)
		db.session.add(record)
		db.session.commit()


record = History( src_lat, src_long, dest_lat, dest_long, user_id, created = )
db.session.add(record)
db.session.commit()

