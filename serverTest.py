from flask import Flask, request, url_for
import json
app = Flask(__name__)

#This is trigererd when the watch makes a post request to our server
#It will send a json file with heart rate
@app.route('/api/hr', methods=['POST'])
def handle_watch_hr():
	hr = json.load(request.files['hr'])
	return {"status": "Recieved"}

#This handler will be triggered when the VR POSTS the alert status
#The user either decided to accept the alert or disregard it
@app.route('/api/vr', methods=['POST'])
def handle_vr_alert():
	alert_status = json.load(request.files['decision'])
	return {"status": "Recieved"}


