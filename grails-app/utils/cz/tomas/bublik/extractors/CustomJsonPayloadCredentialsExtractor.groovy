package cz.tomas.bublik.extractors

import grails.plugin.springsecurity.rest.credentials.AbstractJsonPayloadCredentialsExtractor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

class CustomJsonPayloadCredentialsExtractor extends AbstractJsonPayloadCredentialsExtractor {

    String usernamePropertyName = "username"
    String passwordPropertyName = "password"

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        def jsonBody = getJsonBody(httpServletRequest)

        if (jsonBody) {
            String username = jsonBody."${usernamePropertyName}"
            String password = jsonBody."${passwordPropertyName}"

            println "Extracted credentials from JSON payload. Username: ${username}, password: ${password?.size() ? '[PROTECTED]' : '[MISSING]'}"

            new UsernamePasswordAuthenticationToken(username, password)
        } else {
            println "No JSON body sent in the request"
            return null
        }
    }

}