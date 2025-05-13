package ym_cosmetic.pick_perfume_be.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Value("\${SWAGGER_USERNAME:admin}")
    private lateinit var swaggerUsername: String
    
    @Value("\${SWAGGER_PASSWORD:password}")
    private lateinit var swaggerPassword: String
    
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearer-key"
        val basicAuthSchemeName = "basicAuth"
        
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme().type(SecurityScheme.Type.HTTP)
                            .scheme("bearer").bearerFormat("JWT")
                    )
                    .addSecuritySchemes(
                        basicAuthSchemeName,
                        SecurityScheme().type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .addSecurityItem(SecurityRequirement().addList(basicAuthSchemeName))
            .info(
                Info()
                    .title("PickPerfume API")
                    .description("PickPerfume 백엔드 API 문서")
                    .version("v1.0.0")
            )
    }
}