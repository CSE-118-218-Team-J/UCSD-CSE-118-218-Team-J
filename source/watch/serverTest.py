from flask import Flask, request, url_for
import json
app = Flask(__name__)

#This is trigererd when the watch makes a post request to our server
#It will send a json file with heart rate
@app.route('/api/hr', methods=['POST'])
def handle_watch_hr():
	hr = request.json["hr"]
	id = request.json["ID"]
	return {"status": "Recieved"}

#This handler will be triggered when the VR POSTS the alert status
#The user either decided to accept the alert or disregard it
@app.route('/vr', methods=['POST'])
def handle_vr_alert():
	alert_status = request.json['decision']
	return {"status": "Recieved"}

if __name__ == '__main__':
	app.run('localhost', port=4040)

