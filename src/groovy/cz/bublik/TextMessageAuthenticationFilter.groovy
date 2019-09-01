package cz.bublik

import com.google.common.io.CharStreams
import groovy.json.JsonSlurper
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TextMessageAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public final static String TEXT_MESSAGE_RESPONSE_KEY = 'text_message_response'

    String usernamePropertyName = "username"

    public TextMessageAuthenticationFilter() {
        super('/j_spring_security_text_message')
    }

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
    Authentication attemptAuthentication(HttpServletRequest request,
                                         HttpServletResponse response) throws AuthenticationException {
        logger.error("Attempting text message authentication")

        if (!request.post) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: $request.method")
        }

        def jsonBody = getJsonBody(request)

        String username = jsonBody."${usernamePropertyName}"
        // String userName = SecurityContextHolder.context?.authentication.principal.username
        def user = User.findByUsername(username)

        String textMessageResponse = request.getParameter(TEXT_MESSAGE_RESPONSE_KEY)

        TextMessageAuthenticationToken authentication = new TextMessageAuthenticationToken(username, null, textMessageResponse)
        Authentication authToken = authenticationManager.authenticate(authentication)

        return authToken
    }

   /* @Override
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = req as HttpServletRequest
        def jsonBody = getJsonBody(httpServletRequest)
        super.doFilter(req, res, chain)
    }*/
}
