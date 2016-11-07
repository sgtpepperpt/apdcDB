#Client-Proxy Communication Protocol
Clients issue extended SQL requests to the proxy, which always answers in a JSON format.

**Extended SQL queries:**
+ A subset of SQL, as defined by CryptDB's system
+ Image queries (of the form _SELECT * FROM images WHERE tag='t' OR IMAGE ~<base64>_, where _t_ is a defined tag and _base64_ is an image encoded in this format)
+ Meta queries, described in the following section.

**Meta queries**
+ META LOGIN _username_ _password_
+ META LOGOUT
+ META STATUS
