package cz.bublik

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class MFACodeAuthenticationProvider implements AuthenticationProvider {
    UserDetailsService userDetailsService

    /**
     * Much of this is copied directly from
     * {@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider}
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MFACodeAuthenticationToken authToken = (MFACodeAuthenticationToken) authentication
        String username = (authToken.principal == null) ? 'NONE_PROVIDED' : authToken.name
        UserDetails user = userDetailsService.loadUserByUsername(username)

        def userFromDb = User.findByUsername(username)

        // TODO verify the MFA code expiration here
        // TODO load and compare the UserDetails from the stored token

        Boolean verifiedResponse = authToken.textMessageResponse == userFromDb.mfaCode

        if (!verifiedResponse) {
            throw new WrongTextMessageResponse("Incorrect text message response from ${username}")
        }
        return createSuccessAuthentication(user, authToken)
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication) {
        MFACodeAuthenticationToken result = new MFACodeAuthenticationToken(
                principal,
                authentication.credentials,
                principal.authorities)

        result.details = authentication.details

        return result
    }

    boolean supports(Class<? extends Object> authentication) {
        return (MFACodeAuthenticationToken.isAssignableFrom(authentication))
    }
}
