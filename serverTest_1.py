from flask import Flask, request, url_for
import json
app = Flask(__name__)
heart_rate = 0
threshold = 80

#This is trigererd when the watch makes a post request to our server
#It will send a json file with heart rate
@app.route('/api/hr', methods=['POST'])
def handle_watch_hr():
	global heart_rate
	hr = request.json["hr"]
	id = request.json["ID"]
	num_str = ''.join([char for char in hr if char.isdigit()])
	if num_str != '':
		heart_rate = int(num_str)
		print(heart_rate)
	return {"status": "Recieved"}

#This handler will be triggered when the VR POSTS the alert status
#The user either decided to accept the alert or disregard it
@app.route('/vr', methods=['GET'])
def handle_vr_alert():
	global heart_rate
	global threshold
	print(heart_rate)
	response = {}
	if heart_rate > threshold:
		response["hr"] = str(heart_rate)
		response["notifFlag"] = str(True)
	else:
		response["hr"] = str(heart_rate)
		response["notifFlag"] = str(False)
	# alert_status = request.json['decision']
	return response

@app.route('/updateThreshold', methods=['POST'])
def handle_threshold_update():
	global threshold
	threshold += 5
	return {"status": "Recieved"}

@app.route('/getThreshold', methods=['GET'])
def handle_threshold_get():
	global threshold
	return {"threshold": threshold}

if __name__ == '__main__':
	app.run('localhost', port=4040)