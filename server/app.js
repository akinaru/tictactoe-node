/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
var express = require('express');
var fs = require('fs');
var https = require('https');
var app = express();
var options = {
   key  : fs.readFileSync('server.key'),
   cert : fs.readFileSync('server.crt')
};

var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var gcm = require('node-gcm');

//read authenticaiton json configuration
var fs = require('fs');
try {
	var authent = JSON.parse(fs.readFileSync('authentication.json', 'utf8'));
	if ( !("couchdb_route" in authent) || !("couchdb_username" in authent) || !("couchdb_password" in authent)){
		console.log('[CONFIGURATION ERROR] in authentication.json file, you must have defined values for "couchdb_route" and "couchdb_username" and "couchdb_password"');
		return;
	}
	if (!("gcm_api_key" in authent)){
		console.log('[CONFIGURATION ERROR] in authentication.json file, you must have defined values for "gcm_api_key"');
		return;
	}
}
catch(e){
	console.log("Error occured while reading file authentication.json");
	return;
}

// Set up the sender with you API key
var sender = new gcm.Sender( authent.gcm_api_key );

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0" // Avoids DEPTH_ZERO_SELF_SIGNED_CERT error for self-signed certs

var nano     = require('nano')({
	'url': authent.couchdb_route
})
  , username = authent.couchdb_username
  , userpass = authent.couchdb_password
  , callback = console.log // this would normally be some callback
  , cookies  = {} // store cookies, normally redis or something
  ;

var deviceDB;

function authentication(){

	nano.auth(username, userpass, function (err, body, headers) {

		if (err) {
			return callback(err);
		}

		if (headers && headers['set-cookie']) {
			auth = headers['set-cookie'];
		}

		deviceDB = require('nano')(
			{ 
				url : authent.couchdb_route + '/devices', 
				cookie: auth
			}
		);

		// update an entry
		deviceDB.update = function(obj, key, callback){

			var db = this;
			db.get(key, function (error, existing){ 

				if(!error) {
					obj._rev = existing._rev;
				}
				else{
					console.log(error);
				}
				db.insert(obj, key, callback);
			});
		}

	});
}

authentication();
//request authentication cookie each 9 minutes
setInterval(authentication,540000);

function notify_connection(deviceId,deviceName){

	deviceDB.list({"include_docs": true},function(err, body) {

		if (!err) {
			body.rows.forEach(function(doc) {

				if (("deviceName" in doc.doc) && (doc.id!=deviceId)){
					

					var message = new gcm.Message();
					message.addData('message', { 'topic' : 6 ,'deviceId' : deviceId,'deviceName' : deviceName });

					sender.send(message, { topic: '/topics/' + doc.id },5, function (err, response) {

						if (err)
							console.log(err);
					});

				}
			});
		}
		else{
			console.log(err);
		}
	});
}

/*
// Load the Cloudant library.
var Cloudant = require('cloudant');

var me = authent.cloudant_username;
var password = authent.cloudant_password;

// Initialize the library with my account.
var cloudant = Cloudant({account:me, password:password});

*/

// update connection state for given deviceId 
app.post('/connect', function (req, res) {

	if (("deviceId" in req.body) && ("deviceName" in req.body)){

		deviceDB.get(req.body.deviceId, {'include_docs': true },function(err, body) {

			if (!err) {

				body.state=1;
				body.deviceName=req.body.deviceName;

				deviceDB.update(body, req.body.deviceId, function(err, result){

					if(!err){
						res.status(200).send({"status":200});
					}
					else{
						res.status(500).send({"status":500,"message":err.message});
					}
				});
			}
			else{

				//no username inserted the first time
				deviceDB.update({state:1},req.body.deviceId, function(err, body, header) {

				if (err) {
					console.log('[Insert Error]', err.message);
					res.status(500).send({"status":500,"message":"database insertion error"});
					return;
				}
				res.status(200).send({"status":200});
				});
			}
			notify_connection(req.body.deviceId, req.body.deviceName);
		});
	}
	else {
		res.status(500).send({"status":500,"message":"key missing in body : " + JSON.stringify(req.body)});
	}
})

// update user name for given deviceId
app.post('/username', function (req, res) {

	if (("deviceId" in req.body) && ("deviceName" in req.body)){

		deviceDB.get(req.body.deviceId, {'include_docs': true },function(err, body) {

			if (!err) {

				body.deviceName=req.body.deviceName;

				deviceDB.update(body, req.body.deviceId, function(err, result){

					if(!err){
						notify_connection(req.body.deviceId,req.body.deviceName);
						res.status(200).send({"status":200,"message":"change devicename to " + body.deviceName});
					}
					else{
						res.status(500).send({"status":500,"message":err.message});
					}
				});
			}
			else{
				res.status(500).send({"status":500,"message":err.message});
			}
		});
	}
	else{
		res.status(500).send({"status":500,"message":"key missing"});
	}
});

// get device list with filter except deviceId given in url parameter (optionnal)
app.get('/devices', function (req, res) {

	var devicesList = [];

	deviceDB.list({"include_docs": true},function(err, body) {

		if (!err) {
			body.rows.forEach(function(doc) {
				devicesList.push(doc);
			});
			res.status(200).send({"status":200,"devices" : devicesList});
			return;
		}
		else{
			res.status(500).send({"status":"error","message":err});
		}
	});
})

app.post('/challenge', function (req, res) {

	if (("challengerId" in req.body) && ("targetId" in req.body) && ("challengerName" in req.body)) {

		var message = new gcm.Message();
		message.addData('message', { 'topic' : 1 ,'challengerId' : req.body.challengerId,'challengerName' : req.body.challengerName });

		sender.send(message, { topic: '/topics/' + req.body.targetId }, 5,function (err, response) {

			if(err) {
				res.status(500).send({"status":500,"message":err.message});
			}
			else {
				res.status(200).send({"status":200,"message":response});
			}
		});
	}
	else {
		res.status(500).send({"status":500,"message":"key missing"});
	}

})

app.post('/accept_challenge', function (req, res) {

	if (("challengerId" in req.body) && ("targetId" in req.body) && ("challengerName" in req.body)) {

		var message = new gcm.Message();
		message.addData('message', { 'topic' : 2 ,'challengerId' : req.body.challengerId,'challengerName' : req.body.challengerName });

		sender.send(message, { topic: '/topics/' + req.body.targetId }, 5,function (err, response) {

			if(err) {
				res.status(500).send({"status":500,"message":err.message});
			}
			else {
				res.status(200).send({"status":200,"message":response});
			}
		});
	}
	else {
		res.status(500).send({"status":500,"message":"key missing"});
	}

})

app.post('/decline_challenge', function (req, res) {

	if (("challengerId" in req.body) && ("targetId" in req.body) && ("challengerName" in req.body)) {

		var message = new gcm.Message();
		message.addData('message', { 'topic' : 3 ,'challengerId' : req.body.challengerId,'challengerName' : req.body.challengerName });

		sender.send(message, { topic: '/topics/' + req.body.targetId },5, function (err, response) {

			if(err) {
				res.status(500).send({"status":500,"message":err.message});
			}
			else {
				res.status(200).send({"status":200,"message":response});
			}
		});
	}
	else {
		res.status(500).send({"status":500,"message":"key missing"});
	}

})

app.post('/play', function (req, res) {

	if (("targetId" in req.body) && ("play" in req.body)) {

		var message = new gcm.Message();
		message.addData('message', { 'topic' : 5 ,'play' : req.body.play });

		sender.send(message, { topic: '/topics/' + req.body.targetId }, 5,function (err, response) {

			if(err) {
				res.status(500).send({"status":500,"message":err.message});
			}
			else {
				res.status(200).send({"status":200,"message":response});
			}
		});
	}
	else {
		res.status(500).send({"status":500,"message":"key missing"});
	}

})

/*
var port = (process.env.VCAP_APP_PORT || 3000);
var host = (process.env.VCAP_APP_HOST || 'localhost');
*/
var port = 4747;
var host ='localhost';

// launch listening loop
var server = https.createServer(options, app).listen(port, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Server listening at https://%s:%s", host, port)

})