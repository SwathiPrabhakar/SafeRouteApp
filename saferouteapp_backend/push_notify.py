from gcm import GCM
import fire


# usage python2 pusher.py push --message "msg" --reg_id "reg_id"
class Pusher(object):
	def push(self, message="test", reg_id=None):
		API_KEY = 'AIzaSyAEN09LeNCfS92A3_qgXrxIekiKDJC2ets'

		print(message)
		gcm = GCM(API_KEY)
		data = {'message': message}

		# Downstream message using JSON request
		reg_id = reg_id or 'cZwjGCf2elE:APA91bFXSN0b6vPH79JermZbCPUZGZvw_0odphwJK9VGdh2TPLsElehR0SHpxMYaKD06kknZ8uS5_oqkW0rP0yC8bdT8zrPDJSMWPxCIOj1Ksos9Q0qKkdb0rjczQRI9K_7UjMrPvGps'
		response = gcm.json_request(registration_ids=[reg_id], data=data)

		# Downstream message using JSON request with extra arguments
		res = gcm.json_request(
		    registration_ids=[reg_id], data=data,
		    collapse_key='uptoyou', delay_while_idle=True, time_to_live=3600
		)

		# Topic Messaging
		# topic = 'topic name'
		# gcm.send_topic_message(topic=topic, data=data)

if __name__ == '__main__':
  fire.Fire(Pusher)