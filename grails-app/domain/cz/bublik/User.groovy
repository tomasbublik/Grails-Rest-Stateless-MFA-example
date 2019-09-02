package cz.bublik

class User {

    transient springSecurityService

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    boolean mfaEnabled = false
    String token
    String mfaCode
    Date tokenGenerationTime

    static transients = ['springSecurityService']

    static constraints = {
        username blank: false, unique: true
        password blank: false
        token nullable: true, maxSize: 5000
        mfaCode nullable: true
        tokenGenerationTime nullable: true
    }

    static mapping = {
        password column: '`password`'
        token type: 'text'
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService.encodePassword(password)
    }
}
