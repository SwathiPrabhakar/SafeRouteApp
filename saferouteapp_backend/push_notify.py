from gcm import GCM
API_KEY = 'AIzaSyAEN09LeNCfS92A3_qgXrxIekiKDJC2ets'

gcm = GCM(API_KEY)
data = {'message': 'value1', 'param2': 'value2'}

# Downstream message using JSON request
reg_ids = ['cZwjGCf2elE:APA91bFXSN0b6vPH79JermZbCPUZGZvw_0odphwJK9VGdh2TPLsElehR0SHpxMYaKD06kknZ8uS5_oqkW0rP0yC8bdT8zrPDJSMWPxCIOj1Ksos9Q0qKkdb0rjczQRI9K_7UjMrPvGps']
response = gcm.json_request(registration_ids=reg_ids, data=data)

# Downstream message using JSON request with extra arguments
res = gcm.json_request(
    registration_ids=reg_ids, data=data,
    collapse_key='uptoyou', delay_while_idle=True, time_to_live=3600
)

# Topic Messaging
topic = 'topic name'
gcm.send_topic_message(topic=topic, data=data)