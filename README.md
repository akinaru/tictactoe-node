# Tic-Tac-Toe remote game

Tic Tac Toe game between 2 remote players featuring :

* Android client application
* HTTP REST Apis for uplink connection
* GCM push notifications for downling connection 
* CouchDB database with Stud TLS termination proxy

## Architecture

## Configuration

## Setup CouchDB + Stud backend

### CouchDB

1) install couchDB v1.2.0+

```
apt-get install couchdb
```

2) launch couchDB service

```
service couchDB start
```

3) create an admin user : 

```
curl -X PUT http://127.0.0.1:5984/_config/admins/<admin_username> -d '"<admin_password>"'
```

Now that you have created at least one admin user, admin party is disabled

4) create a user that will be granted `reader` right :

```
curl -X POST http://admin:admin@127.0.0.1:5984/_users --header "Content-Type: application/json" -d '{"_id": "org.couchdb.user:<username>","name": "<username>","type": "user","roles": [],"password": "<password>"}'
```

5) create database `devices` :

```
curl -X PUT http://admin:admin@127.0.0.1:5984/devices
```

6) validation function for writing/updating : 

```
curl --header "Content-Type: application/json" -d '{"_id": "_design/blockAnonymousWrites","language": "javascript","validate_doc_update": "function(new_doc, old_doc, userCtx) {   if(userCtx.name !='\''<username>'\'') {     throw({forbidden: '\''Not Authorized'\''});   } }"}' -X POST http://admin:admin@127.0.0.1:5984/devices
```

Now, anonymous write to database devices is disabled

7) setup authorized reader for database `devices` :

```
curl -X PUT http://<username>:<password>@127.0.0.1:5984/devices/_security --header "Content-Type: application/json" -d '{"readers": {"names": ["<username>"],"roles": ["reader"]}}'
```

8) create a design view for getting element by key: 

```
curl --header "Content-Type: application/json" -d '{"_id": "_design/devices","language": "javascript","views": { "by_key": { "map": "function(doc) { emit([doc._id, null], doc._id); }" } }}' -X POST http://admin:admin@127.0.0.1:5984/devices
```

### Stud

A big thanks to Nuno Job for <a href="http://blog.yld.io/2014/03/26/migrating-a-production-couchdb-database-with-joyent-and-stud">his amazing tutorial on Stud setup</a>

1) install Stud :

```
apt-get install libev4 libssl-dev libev-dev -y  
git clone git://github.com/bumptech/stud.git  
cd stud  
make  
sudo make install
```

2) create running and config directory

```
mkdir /var/run/stud  
mkdir /usr/local/var/run/stud  
mkdir /usr/local/etc/stud
```

3) get your certificate chain and concatenate it into one PEM file

```
cp <certificate_chain.pem> /usr/local/etc/stud/stud.pem
```

4) setup a `stud` user and assign rights to stud directories and files

```
useradd -d /var/lib/_stud _stud  
chown _stud: /usr/local/etc/stud/stud.pem  
chown _stud: /var/run/stud  
chown -R _stud: /usr/local/var/run/stud /usr/local/etc/stud  
chmod 0770 /usr/local/var/run/stud/  
chmod 664 /usr/local/etc/stud/*.conf  
chmod 600 /usr/local/etc/stud/stud.pem  
```

5) create other directories

```
mkdir /etc/stud  
mkdir /etc/default  
```

6) create init.d service :

```
curl https://gist.githubusercontent.com/akinaru/0172b0d076f9a8f237bd/raw/f8fd8b03eae8a4251aee79abd6bd39d69bc24e15/stud > /etc/init.d/stud
chmod +x /etc/init.d/stud
```

7) edit `Stud` configuration file : 

`vi /usr/local/etc/stud/stud.conf`


```
frontend="[*]:6984"  
backend="[127.0.0.1]:5984"  
pem-file="/usr/local/etc/stud/stud.pem"  
ssl=on  
workers=2  
syslog=on
```

8) start Stud service :

```
update-rc.d stud defaults  
service stud start  
```

Then check that couchDB traffic is redirected : 

```
curl http://localhost:5984
curl -k https://localhost:6984
```

## CouchDB Troubleshoots

In case of `{"error":"error","reason":"eacces"}` :

```
sudo chown -R couchdb /usr/local/etc/couchdb
sudo chown -R couchdb /usr/local/var/lib/couchdb
sudo chown -R couchdb /usr/local/var/log/couchdb
```

## Bluemix integration

This project can also be integrated in Bluemix platform (substituing CouchDB/Stud with Cloudant webservice) through cloudfoundry CLI 

You can begin to create a mobile app that will provide you with :

* SDK for NodeJS
* Cloudant DB webservice

### Cloudant credentials 

Get Cloudant DB credentials from Bluemix dashboard :

![screenshot](cloudant.png)

In `server/authentication.json` setup cloudant credetials : 

```
{
	"couchdb_route"    : "<bluemix_route>",
	"couchdb_username" : "<cloudant_username>",
	"couchdb_password" : "<cloudant_password>",
	"gcm_token"        : "<project_number>",
	"gcm_api_key"      : "<api_key>"
} 
```

### Push to Bluemix platform

You can push project directly to Bluemix platform :

```
cd server
cf api https://api.ng.bluemix.net
cf login -u <bluemix_username> -o <bluemix_username> -s <namespace>
cf push <project_name>
```

## Useful links

* http://blog.mattwoodward.com/2012/03/definitive-guide-to-couchdb.html 
* http://guide.couchdb.org/draft/security.html
* http://wiki.apache.org/couchdb/How_to_enable_SSL
* http://blog.yld.io/2014/03/26/migrating-a-production-couchdb-database-with-joyent-and-stud/

## License

The MIT License (MIT) Copyright (c) 2015-2016 Bertrand Martel
