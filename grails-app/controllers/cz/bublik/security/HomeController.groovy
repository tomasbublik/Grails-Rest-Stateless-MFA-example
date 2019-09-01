package cz.bublik.security

import cz.bublik.Role
import grails.plugin.springsecurity.annotation.Secured

class HomeController {
    def beforeInterceptor = {
        println "Tracing action ${actionUri}"
    }

    @Secured([Role.ROLE_NORMAL])
    def index() {
    }
}
