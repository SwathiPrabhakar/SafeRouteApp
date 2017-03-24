def query(slat, slong, dlat, dlong):
	baseurl = 'https://maps.googleapis.com/maps/api/directions/json?'
	return "{}origin={},{}&destination={},{}&mode=walking&alternatives=true&key={}".format(baseurl, slat, slong, dlat, dlong, key)

# query_string("33.416565", "-111.925015", "33.416207", "-111.922558")
# key = 'AIzaSyDmNBpYDBoxkwYTW5Aw9H3YrEXaSi-tnAo'

def get_safe_routes(query):
	# gmaps.directions("33.416565,-111.925015", "33.416207, -111.922558")[0]
	routes = gmaps.directions("33.416565,-111.925015", "33.418000, -111.931827 ", alternatives=True, mode="walking")
	# routes = gmaps.directions("33.416565,-111.925015", "33.416207, -111.922558")[0][u'legs'][0][u'steps']
	legs = [d[u'legs'] for d in gmaps.directions("33.416565,-111.925015", "33.416207, -111.922558")][0]
	steps = [d[u'steps'] for d in legs[0]]
	starts = [d[u'start_location'] for d in steps[0]]


def query_crime(long = -122.444586, lat = 37.782745, radius = 1000):
	return "https://data.sfgov.org/resource/cuks-n6tp.json?$limit=500&$where=category in('SEX OFFENSES, FORCIBLE','ASSAULT', 'LOITERING','LARCENY/THEFT','KIDNAPPING','WEAPON LAWS','DISORDERLY CONDUCT','DRUNKENNESS','DRUG/NARCOTIC') AND within_circle(location, {}, {}, 1000)&$order=date DESC".format(long, lat)