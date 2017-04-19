from app import gmaps
import requests
import pandas as pd
import pdb
import datetime
import multiprocessing

def query(slat, slong, dlat, dlong):
    baseurl = 'https://maps.googleapis.com/maps/api/directions/json?'
    return "{}origin={},{}&destination={},{}&mode=walking&alternatives=true&key={}".format(baseurl, slat, slong, dlat, dlong, key)

def get_lat_long(query = '1600 Amphitheatre Parkway, Mountain View, CA'):
    geocode_result = gmaps.geocode(query)
    return "{}, {}".format(geocode_result[0][u'geometry'][u'location'][u'lat'], geocode_result[0][u'geometry'][u'location'][u'lng'])
    # return [geocode_result[0][u'geometry'][u'location'][u'lat'], geocode_result[0][u'geometry'][u'location'][u'lng']]

def get_safe_routes(frm="33.416565,-111.925015", to="33.418000, -111.931827"):
    """
    todo: fix better metric, parallelize calls to query_crime with async
    """
    departure_time = datetime.datetime.now()
    routes = gmaps.directions(frm, to, alternatives=True, mode="driving", departure_time=departure_time )
    route_coords = []
    routes_with_score = []
    for route in routes:
        legs = route[u'legs'][0]
        steps = legs[u'steps']
        starts = [d[u'start_location'] for d in steps]
        ends = [d[u'end_location'] for d in steps]
        coords = list(set(map(lambda x: (x[u'lat'], x[u'lng']), starts + ends)))
        route_coords.append(coords)
    for i, r in enumerate(routes):
        pool = multiprocessing.Pool(processes=10)
        scores = pool.map(query_crime, route_coords[i])
        # scores = [query_crime(x) for x in route_coords[i]]
        pool.close()
        pool.join()
        print(scores)
        routes_with_score.append({"score":sum(scores)/max(1, len(scores)), "route":r})
    final_score = {}
    final_score["routes"] = routes_with_score
    return final_score

def query_crime(loc, shape='within_circle', rad=500, s_date='2015-01-01', e_date='2015-10-01', granular='ym'):
    # api usage reference http://www.racketracer.com/2015/10/19/most-frequented-crimes-in-san-francisco-normalized-by-neighborhood/
    lat, lng = loc
    url = "https://data.sfgov.org/resource/cuks-n6tp.json?$select=COUNT(*)&$where=" + shape + "(location," + str(lat) + "," + str(lng) + "," + str(rad) + ") AND date>" + \
    "'" + s_date + "' AND date<'" + e_date + "'&$limit=50000"
    response = requests.get(url).json()
    try:
        return int(response[0]['COUNT'])
    except:
        return 0

# query_crime(37.757396, -122.492781)
# q = ["37.293089, -122.213628", "37.318691, -122.088144"]


def get_safe_routes_raw(frm, to):
    a, b = get_lat_long(frm), get_lat_long(to)
    return get_safe_routes(a, b)

# a, b = get_lat_long("Cupertino Library, 10800 Torre Ave, Cupertino, CA 95014"), get_lat_long("Amphitheatre Pkwy, Mountain View, CA 94043")
# get_safe_routes(a, b)
# query_string("33.416565", "-111.925015", "33.416207", "-111.922558")

# url = query_crime('within_circle', 500, 37.757396, -122.492781, '2015-01-01', '2015-10-01','ym')
# response = requests.get(url).json()
# df = pd.DataFrame(response)
# events = df['COUNT'].count()
# score = df['COUNT'].map(int).sum() // df['COUNT'].count()