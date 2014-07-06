__author__ = 'Ingvord'

import sys
import urllib2
import json

# create a password manager
password_mgr = urllib2.HTTPPasswordMgrWithDefaultRealm()

username = "ingvord"
password = "test"
# Add the username and password.
# If we knew the realm, we could use it instead of None.
top_level_url = "http://localhost:8080/mtango/rest/"
password_mgr.add_password(None, top_level_url, username, password)

handler = urllib2.HTTPBasicAuthHandler(password_mgr)

# create "opener" (OpenerDirector instance)
opener = urllib2.build_opener(handler)

# use the opener to fetch a URL
opener.open("http://localhost:8080/mtango/rest/devices")

# Install the opener.
# Now all calls to urllib2.urlopen use our opener.
urllib2.install_opener(opener)

response = urllib2.urlopen("http://localhost:8080/mtango/rest/devices")
result = json.loads(response.read())

print result[u'argout']