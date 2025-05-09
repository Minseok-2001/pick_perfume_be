package ym_cosmetic.pick_perfume_be.security

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    fun encode(rawPassword: CharSequence): String {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(10))
    }


    fun matches(rawPassword: CharSequence, encodedPassword: String?): Boolean {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword)
    }
}