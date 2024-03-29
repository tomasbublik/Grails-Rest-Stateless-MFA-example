import cz.bublik.AuthenticationSuccessHandler
import cz.bublik.MFACodeAuthenticationFilter
import cz.bublik.MFACodeAuthenticationProvider
import cz.tomas.bublik.extractors.CustomJsonPayloadCredentialsExtractor
import cz.tomas.bublik.filters.CustomRestAuthenticationFilter
import cz.tomas.bublik.handlers.CustomAuthenticationSuccessHandler
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.dao.DaoAuthenticationProvider

// Place your Spring DSL code here
beans = {
    def conf = SpringSecurityUtils.securityConfig

    authenticationSuccessHandler(AuthenticationSuccessHandler) {
        it.autowire = true
    }

    customAuthenticationSuccessHandler(CustomAuthenticationSuccessHandler) {
        it.autowire = true
    }

    daoAuthenticationProvider(DaoAuthenticationProvider) {
        it.autowire = true
        userDetailsService = ref('stepOneUserDetailsProviderService')
    }

    mFACodeAuthenticationProvider(MFACodeAuthenticationProvider) {
        it.autowire = true
        userDetailsService = ref('userDetailsService')
    }

    mFACodeAuthenticationFilter(MFACodeAuthenticationFilter) {
        it.autowire = true
        endpointUrl = '/api/mfa_code_message'
        authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
    }

    credentialsExtractor(CustomJsonPayloadCredentialsExtractor) {
        it.autowire = true
    }

    restAuthenticationFilter(CustomRestAuthenticationFilter) {
        authenticationManager = ref('authenticationManager')
        authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
        mfaCodeAuthenticationSuccessHandler = ref('customAuthenticationSuccessHandler')
        authenticationFailureHandler = ref('restAuthenticationFailureHandler')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        credentialsExtractor = ref('credentialsExtractor')
        endpointUrl = '/api/login'
        tokenGenerator = ref('tokenGenerator')
        tokenStorageService = ref('tokenStorageService')
    }
}
