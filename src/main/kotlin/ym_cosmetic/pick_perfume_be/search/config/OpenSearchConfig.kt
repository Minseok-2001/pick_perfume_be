package ym_cosmetic.pick_perfume_be.search.config

import org.opensearch.client.RestHighLevelClient
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration
import org.opensearch.data.client.orhlc.ClientConfiguration
import org.opensearch.data.client.orhlc.RestClients
import org.opensearch.data.core.OpenSearchOperations
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument
import java.time.Duration

@Configuration
@EnableElasticsearchRepositories(basePackages = ["ym_cosmetic.pick_perfume_be.search.repository"])
class OpenSearchConfig(
    @Value("\${spring.elasticsearch.uris:http://localhost:9200}") private val elasticsearchUri: String,
    @Value("\${spring.elasticsearch.username:}") private val username: String?,
    @Value("\${spring.elasticsearch.password:}") private val password: String?,
    @Value("\${spring.elasticsearch.connection-timeout:5s}") private val connectionTimeout: String,
    @Value("\${spring.elasticsearch.socket-timeout:60s}") private val socketTimeout: String
) : AbstractOpenSearchConfiguration() {
    private val logger = LoggerFactory.getLogger(OpenSearchConfig::class.java)

    @Bean
    override fun opensearchClient(): RestHighLevelClient? {
        val uri = elasticsearchUri.removePrefix("https://").removeSuffix("/").split(":")
        val host = uri[0]
        val port = if (uri.size > 1) uri[1].toInt() else 9200


        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .usingSsl()
            .withConnectTimeout(Duration.ofSeconds(5))
            .withSocketTimeout(Duration.ofSeconds(60))
            .apply {
                if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                    withBasicAuth(username, password)
                }
            }
            .build()

        return RestClients.create(clientConfiguration).rest()
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