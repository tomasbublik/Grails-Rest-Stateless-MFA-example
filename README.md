# Optional stateless REST MFA Grails 2.3.7 + Rest Security Plugin 1.5.4 example

## Description
This is a sample project of extended Spring Boot Grails REST plugin implementing the MFA by sending the generated message to a user.

The project uses H2 in-memory database with bootstrapped 2 users. One having the MFA disabled, the second one having the MFA feature enabled. The implementation is completely stateless, so there is no state between the first and second step preserved. 

## Running

```
grails run-app
```

## How to test
There are two users inserted into the database during the app startup. The first one (username: user1) has the MFA feature disabled, so just send a simple POST request:

```html
POST http://localhost:8080/api/login
Content-Type: application/json

{
  "username": "user1",
  "password": "password1"
}
```

to get the authentication tokens

The second one has the MFA feature enabled, and that's why he's forced to authentication by a two-factor authentication process. To get an MFA code for the second step, send the following POST request:

```html
POST http://localhost:8080/api/login
Content-Type: application/json

{
  "username": "user2",
  "password": "password2"
}
```

Despite the request seems similar, the answer does not contain the authentication details, but a code. This code won't be sent in production, obviously, however, will be delivered to a user by the other way (SMS).

So the second POST request must contain this code to authenticate a user. In this sample, always _123456_:

```html
POST http://localhost:8080/api/mfa_code_message
Content-Type: application/json

{
  "username": "user2",
  "mfa_code_response": "123456"
}
```

A user should get the authentications tokens as the response

## Workflow details

