package ym_cosmetic.pick_perfume_be.security

import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    private val LOG_ROUNDS = 10

    fun encode(rawPassword: CharSequence): String {
        return try {
            BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(LOG_ROUNDS))
        } catch (e: Exception) {
            BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt())
        }
    }

    fun matches(rawPassword: CharSequence, encodedPassword: String?): Boolean {
        return try {
            if (encodedPassword.isNullOrEmpty() || rawPassword.isEmpty()) {
                return false
            }
            BCrypt.checkpw(rawPassword.toString(), encodedPassword)
        } catch (e: Exception) {
            false
        }
    }
}