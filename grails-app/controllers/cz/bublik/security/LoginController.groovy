package cz.bublik.security

import cz.bublik.StepOneUserDetailsProviderService
import cz.bublik.TextMessageAuthenticationFilter
import grails.plugin.springsecurity.SpringSecurityUtils

import static grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted

class LoginController extends grails.plugin.springsecurity.LoginController {
    def beforeInterceptor = {
        println "Tracing action ${actionUri}"
    }

    def denied() {
        if (ifAnyGranted(StepOneUserDetailsProviderService.ROLE_STEP_ONE_AUTHENTICATED)) {
            redirect action: 'steptwo'
        }
    }

    def steptwo() {
        [
                postUrl  : "${request.contextPath}/${SpringSecurityUtils.securityConfig.textMessage.filterProcessesUrl}",
                tokenName: TextMessageAuthenticationFilter.TEXT_MESSAGE_RESPONSE_KEY
        ]

    }
}
