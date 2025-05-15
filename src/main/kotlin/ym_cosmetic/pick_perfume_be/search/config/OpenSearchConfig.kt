package ym_cosmetic.pick_perfume_be.search.config

import org.opensearch.data.client.orhlc.ClientConfiguration
import org.opensearch.data.client.orhlc.OpenSearchRestTemplate
import org.opensearch.data.client.orhlc.RestClients
import org.opensearch.data.core.OpenSearchOperations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import java.time.Duration

@Configuration
@EnableElasticsearchRepositories(basePackages = ["ym_cosmetic.pick_perfume_be.search.repository"])
class OpenSearchConfig(
    @Value("\${spring.elasticsearch.uris:}") private val elasticsearchUri: String,
    @Value("\${spring.elasticsearch.username:}") private val username: String?,
    @Value("\${spring.elasticsearch.password:}") private val password: String?,
    @Value("\${spring.elasticsearch.connection-timeout:5s}") private val connectionTimeout: String,
    @Value("\${spring.elasticsearch.socket-timeout:60s}") private val socketTimeout: String
)  {
    private val logger = LoggerFactory.getLogger(OpenSearchConfig::class.java)

    @Bean
    @Primary
    fun openSearchRestTemplate(): OpenSearchRestTemplate {
        logger.info("Configuring OpenSearchRestTemplate with URI: {}", elasticsearchUri)
        
        val isHttps = elasticsearchUri.startsWith("https://")
        val cleanUri = elasticsearchUri
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")
        
        val hostAndPort = cleanUri.split(":")
        val host = hostAndPort[0]
        val port = if (hostAndPort.size > 1) hostAndPort[1].toInt() else if (isHttps) 443 else 9200
        
        logger.info("Connecting to OpenSearch at {}:{} with {}", host, port, if (isHttps) "HTTPS" else "HTTP")
        
        val clientConfigBuilder = ClientConfiguration.builder()
            .connectedTo("$host:$port")
        
        if (isHttps) {
            clientConfigBuilder.usingSsl()
        }
        
        // 인증 정보 설정
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            logger.info("Using basic authentication for OpenSearch with username: {}", username)
            clientConfigBuilder.withBasicAuth(username, password)
        }
        
        // 타임아웃 설정
        val connectTimeoutSeconds = connectionTimeout.removeSuffix("s").toLong()
        val socketTimeoutSeconds = socketTimeout.removeSuffix("s").toLong()
        
        clientConfigBuilder
            .withConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .withSocketTimeout(Duration.ofSeconds(socketTimeoutSeconds))
        
        val clientConfiguration = clientConfigBuilder.build()
        val client = RestClients.create(clientConfiguration).rest()
        
        return OpenSearchRestTemplate(client)
    }

    @Bean
    fun openSearchOperations(): OpenSearchOperations {
        return openSearchRestTemplate()
    }

    @Bean
    fun openSearchIndexInitializer(openSearchOperations: OpenSearchOperations): ApplicationListener<ContextRefreshedEvent> {
        return ApplicationListener<ContextRefreshedEvent> { event ->
            try {
                logger.info("Initializing OpenSearch indices")
                val indexOps = openSearchOperations.indexOps(PerfumeDocument::class.java)

                if (!indexOps.exists()) {
                    logger.info("Creating index for PerfumeDocument")
                    indexOps.create()

                    // 매핑 설정
                    val mapping = indexOps.createMapping()
                    indexOps.putMapping(mapping)

                    logger.info("Index created successfully")
                } else {
                    logger.info("Index already exists for PerfumeDocument")
                }
            } catch (e: Exception) {
                logger.error("Failed to initialize OpenSearch indices: {}", e.message, e)
            }
        }
    }
}