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

This implementation is based on an extension of the REST Security plugin classes. Especially _RestAuthenticationFilter_. Follow these steps to do your own extension:

####1. Create _CustomRestAuthenticationFilter_
Locate your _resources.groovy_ file and create your own _CustomRestAuthenticationFilter_ bean:

https://github.com/tomasbublik/Grails-Rest-Stateless-MFA-example/blob/733250a2d149ed173256ec9de23bf36a0ec47bc6/grails-app/conf/spring/resources.groovy#L42

Don't forget to specify only a certain URL for which should the filter be applied: 
```groovy
endpointUrl = '/api/login'
```
And also create a custom _customAuthenticationSuccessHandler_ to prevent a token generation, but generate a MFA code instead.

The _CustomRestAuthenticationFilter_ would be almost the same as the extended _RestAuthenticationFilter_ filter, except a few details. LEave the first part as it is, and add the following snippet into the authenticated section:

```groovy
def user = User.findByUsername(authenticationResult.principal.username)
if (user && user.mfaEnabled) {
    updateUser(accessToken, user)
    mfaCodeAuthenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, accessToken)
    return
}
```

This will cause the MFA code generation when user sent proper credentials, and also saves the generated token, timestamp and MFA code to the database. But only for those users having the MFA enabled.

####2. Create _StepOneUserDetailsProviderService_

This class prevents creation the user details having all the permissions. It passes the _ROLE_PRE_AUTH_ permission only. 

####3. Create _MFACodeAuthenticationFilter_
Create another extension of the _RestAuthenticationFilter_ class, but this time only for the _/api/mfa_code_message_ URL only. Pass the username and MFA code received by an SMS and sent as a POST request on the mentioned URL by a user to the _MFACodeAuthenticationProvider_:

https://github.com/tomasbublik/Grails-Rest-Stateless-MFA-example/blob/733250a2d149ed173256ec9de23bf36a0ec47bc6/src/groovy/cz/bublik/MFACodeAuthenticationProvider.groovy#L16

And create the authentication object base on _MFACodeAuthenticationToken_ which returned to the filter does the same steps as the first step of the proper authentication; generate and returns the access and refresh tokens.

From now on, a user can authenticate himself by this token as during a one-step authentication  