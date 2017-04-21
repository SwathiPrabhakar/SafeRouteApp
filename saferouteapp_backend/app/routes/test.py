import requests

def query_crime(loc, shape='within_circle', rad=500, s_date='2015-01-01', e_date='2015-10-01', granular='ym'):
    print "hello"
    # api usage reference http://www.racketracer.com/2015/10/19/most-frequented-crimes-in-san-francisco-normalized-by-neighborhood/
    lat, lng = loc
    print lat, lng
    url = "https://data.sfgov.org/resource/cuks-n6tp.json?$select=location&$where=" + shape + "(location," + str(lat) + "," + str(lng) + "," + str(rad) + ") AND date>" + \
    "'" + s_date + "' AND date<'" + e_date + "'&$limit=20"
    response = requests.get(url).json()
    print response
query_crime((37.7847,-122.4145))