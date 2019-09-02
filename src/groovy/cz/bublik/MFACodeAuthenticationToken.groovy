package cz.bublik

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class MFACodeAuthenticationToken extends AbstractAuthenticationToken {
    String textMessageResponse
    Object credentials
    Object principal
    String accessToken

    MFACodeAuthenticationToken(Object principal, Object credentials, String textMessageResponse) {
        super(null)
        this.principal = principal
        this.credentials = credentials
        this.textMessageResponse = textMessageResponse
        this.setAuthenticated(false)
    }

    MFACodeAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String textMessageResponse, Object principal, String accessToken) {
        super(authorities)
        this.textMessageResponse = textMessageResponse
        this.credentials = credentials
        this.principal = principal
        this.accessToken = accessToken
    }

    MFACodeAuthenticationToken(Object principal,
                               Object credentials,
                               Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
        this.principal = principal
        this.credentials = credentials
        super.setAuthenticated(true)
    }

    @Override
    void setAuthenticated(boolean isAuthenticated) {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    'Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead')
        }
        super.setAuthenticated(false)
    }

    @Override
    public Object getCredentials() {
        return credentials
    }

    @Override
    public Object getPrincipal() {
        return principal
    }
}
