package cz.bublik

import com.google.common.io.CharStreams
import grails.plugin.springsecurity.rest.RestAuthenticationFilter
import grails.plugin.springsecurity.rest.token.AccessToken
import groovy.json.JsonSlurper
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MFACodeAuthenticationFilter extends RestAuthenticationFilter {
    public final static String MFA_CODE_RESPONSE_KEY = 'mfa_code_response'

    String usernamePropertyName = "username"

    Object getJsonBody(HttpServletRequest httpServletRequest) {
        try {
            String body = CharStreams.toString(httpServletRequest.reader)
            JsonSlurper slurper = new JsonSlurper()
            slurper.parseText(body)
        } catch (exception) {
            [:]
        }
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request as HttpServletRequest
        HttpServletResponse httpServletResponse = response as HttpServletResponse

        def actualUri = httpServletRequest.requestURI - httpServletRequest.contextPath

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            if (!httpServletRequest.post) {
                throw new AuthenticationServiceException(
                        "Authentication method not supported: $httpServletRequest.method")
            }

            def jsonBody = getJsonBody(httpServletRequest)

            String username = jsonBody."${usernamePropertyName}"
            String textMessageResponse = jsonBody."${MFA_CODE_RESPONSE_KEY}"

            MFACodeAuthenticationToken authentication = new MFACodeAuthenticationToken(username, null, textMessageResponse)
            Authentication authenticationResult = authenticationManager.authenticate(authentication)
            if (authenticationResult.authenticated) {
                SecurityContextHolder.context.setAuthentication(authenticationResult)
                println("Is authenticated by a MFA code")
                AccessToken accessToken = tokenGenerator.generateAccessToken(authenticationResult.principal as UserDetails)
                log.debug "Generated token: ${accessToken}"

                tokenStorageService.storeToken(accessToken.accessToken, authenticationResult.principal as UserDetails)
                authenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, accessToken)
            } else {
                log.debug "Not authenticated. Cannot verify the MFA code provided"
            }
        } else {
            super.doFilter(request, response, chain)
        }
    }
}
