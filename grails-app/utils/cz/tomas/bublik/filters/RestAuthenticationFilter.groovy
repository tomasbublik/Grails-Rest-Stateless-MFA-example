package cz.tomas.bublik.filters

import cz.bublik.User
import cz.tomas.bublik.handlers.CustomAuthenticationSuccessHandler
import grails.plugin.springsecurity.rest.RestAuthenticationFilter
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.transaction.Transactional
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomRestAuthenticationFilter extends RestAuthenticationFilter {

    CustomAuthenticationSuccessHandler mfaCodeAuthenticationSuccessHandler

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        println("goes here into the filter")
        HttpServletRequest httpServletRequest = request as HttpServletRequest
        HttpServletResponse httpServletResponse = response as HttpServletResponse

        def actualUri = httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            log.debug "Applying authentication filter to this request"

            //Only POST is supported
            if (httpServletRequest.method != 'POST') {
                log.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
                return
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
            Authentication authenticationResult

            UsernamePasswordAuthenticationToken authenticationRequest = credentialsExtractor.extractCredentials(httpServletRequest)

            boolean authenticationRequestIsCorrect = (authenticationRequest?.principal && authenticationRequest?.credentials)

            if (authenticationRequestIsCorrect) {
                authenticationRequest.details = authenticationDetailsSource.buildDetails(httpServletRequest)

                try {
                    log.debug "Trying to authenticate the request"
                    authenticationResult = authenticationManager.authenticate(authenticationRequest)

                    if (authenticationResult.authenticated) {
                        log.debug "Request authenticated. Storing the authentication result in the security context"
                        log.debug "Authentication result: ${authenticationResult}"

                        SecurityContextHolder.context.setAuthentication(authenticationResult)
                    }
                } catch (AuthenticationException ae) {
                    log.debug "Authentication failed: ${ae.message}"
                    authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, ae)
                }

            } else {
                log.debug "Username and/or password parameters are missing."
                if (!authentication) {
                    log.debug "Setting status to ${HttpServletResponse.SC_BAD_REQUEST}"
                    httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
                    return
                } else {
                    log.debug "Using authentication already in security context."
                    authenticationResult = authentication
                }
            }

            if (authenticationResult?.authenticated) {
                AccessToken accessToken = tokenGenerator.generateAccessToken(authenticationResult.principal as UserDetails)
                log.debug "Generated token: ${accessToken}"

                tokenStorageService.storeToken(accessToken.accessToken, authenticationResult.principal as UserDetails)

                def user = User.findByUsername(authenticationResult.principal.username)
                if (user && user.mfaEnabled) {
                    updateUser(accessToken, user)
                    mfaCodeAuthenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, accessToken)
                    return
                }

                authenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, accessToken)
            } else {
                log.debug "Not authenticated. Rest authentication token not generated."
            }

        } else {
            chain.doFilter(request, response)
        }
    }

    @Transactional
    private void updateUser(AccessToken accessToken, User user) {
        user.token = accessToken.accessToken
        user.tokenGenerationTime = new Date()
        //and generate code 123456
        user.mfaCode = "123456"
        user = user.merge()
        user.save(failOnError: true)
    }
}
