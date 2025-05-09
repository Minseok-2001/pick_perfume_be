package ym_cosmetic.pick_perfume_be.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer

@Configuration
@EnableJdbcHttpSession
class SessionConfig : AbstractHttpSessionApplicationInitializer()

