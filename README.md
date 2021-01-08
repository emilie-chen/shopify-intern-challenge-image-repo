# Shopify Internship Coding Challenge - Image Repository
## Overview
### Technologies used
1. Java
2. Spring Boot
3. MySQL

### Potential further improvements
1. Access control (public, private, sharing) is implemented, but not every function has a public API

### REST API Documentation
### User login
```http request
PUT URL/api/user/login 
```
#### body
```json
{
  "username": "USERNAME",
  "password": "PASSWORD"
}
```
#### cookie
```
name: token
value: session id (UUID)
expires: current time plus 15 minutes
```
### User login status
```http request
GET URL/api/user/status/{session-id}
```
#### response
if logged in
```json
{
    "expiry": "2021-01-08T18:38:58.201+00:00",
    "loggedIn": true
}
```
if not
```json
{
    "expiry": null,
    "loggedIn": false
}
```

### Upload image
Note: every time you make an image related your requeset, your session expiry time will
automatically be set to current time plus 15 minutes. A new cookie with will be returned
to you every time.
```http request
POST URL/api/image/upload
```
#### body
```json
{
  "file": "REPLACE THIS VALUE WITH YOUR FILE",
  "session": "SESSION UUID",
  "isPublic": true / false
}
```
#### response
```json
{
  "id": "YOUR IMAGE UUID"
}
```

### Downloading an image
```http request
GET URL/api/image/{id}
```
- ```id``` is the image UUID
- The body must contain a key ```"session"``` with your session id as the value
#### response
- 404 if it doesn't exist
- 403 if it exists but you cannot access it or if you are not logged in
- 200 if found and accessible. The file will start downloading

### Deleting an image
```http request
DELETE URL/api/image/delete/{id}
```
- ```id``` is the image UUID
- The body must contain a key ```"session"``` with your session id as the value
#### response
- 404 if it doesn't exist
- 403 if it exists but you cannot delete it due to permissions or if you are not logged in
- 200 if found, accessible and successfully deleted

### Finding images by name
```http request
GET URL/api/image/name/{name}
```
- A session ID is not required
#### response
```json
[
  {
    "id": "IMAGE UUID 1",
    "name": "IMAGE NAME 1"
  },
  {
    "id": "IMAGE UUID 2",
    "name": "IMAGE NAME 2"
  },
  {
    "id": "IMAGE UUID 3",
    "name": "IMAGE NAME 3"
  },
  etc
]
```
Note: just because you have the image IDs doesn't mean you have access to it.

## How to set it up
Modify applications.properties to suit your need, e.g. changing the database name

### Database
For the database, create a table to store images:
```sql
CREATE TABLE images (
    id varchar(1023),
    name varchar(1023),
    owner varchar(1023),
    accessible_to LONGTEXT,
    image LONGBLOB
);
```

Create another table to store users:
```sql
CREATE TABLE users (
    id varchar(1023),
    username varchar(1023)
);
```

### To build
```shell
./mvnw install
```

### To run
```shell
java -jar PATH_OF_JAR_FILE
```
Replace PATH_OF_JAR_FILE with the path of the jar file that was built in the previous step
