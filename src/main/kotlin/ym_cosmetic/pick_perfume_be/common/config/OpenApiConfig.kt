package ym_cosmetic.pick_perfume_be.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI? {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-key",
                        SecurityScheme().type(SecurityScheme.Type.HTTP)
                            .scheme("bearer").bearerFormat("JWT")
                    )
            )
            .info(
                Info()
                    .title("PickPerfume API")
                    .description("PickPerfume 백엔드 API 문서")
                    .version("v1.0.0")
            )
    }
}