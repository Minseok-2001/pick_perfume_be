import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.http.HttpHost
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Configuration
@EnableElasticsearchRepositories(basePackages = ["ym_cosmetic.pick_perfume_be.search.repository"])
class ElasticsearchConfig(
    @Value("\${elasticsearch.host:localhost}") private val host: String,
    @Value("\${elasticsearch.port:9200}") private val port: Int
) {
    private val logger = LoggerFactory.getLogger(ElasticsearchConfig::class.java)

    @Bean
    fun elasticsearchClient(): ElasticsearchClient {
        logger.info("Connecting to Elasticsearch at {}:{}", host, port)

        val restClient = RestClient.builder(HttpHost(host, port, "http"))
            .setRequestConfigCallback { requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout(5000)
                    .setSocketTimeout(60000)
            }
            .setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultIOReactorConfig(
                    IOReactorConfig.custom()
                        .setIoThreadCount(4)
                        .build()
                )
            }
            .build()

        val mapper = JacksonJsonpMapper(
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        )

        val transport: ElasticsearchTransport = RestClientTransport(restClient, mapper)

        return ElasticsearchClient(transport)
    }

    @Bean
    fun elasticsearchTemplate(): ElasticsearchOperations {
        return ElasticsearchTemplate(elasticsearchClient())
    }

    @Bean
    fun elasticsearchIndexInitializer(elasticsearchOperations: ElasticsearchOperations): ApplicationListener<ContextRefreshedEvent> {
        return ApplicationListener<ContextRefreshedEvent> { event ->
            try {
                logger.info("Initializing Elasticsearch indices")
                val indexOps = elasticsearchOperations.indexOps(PerfumeDocument::class.java)

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
                logger.error("Failed to initialize Elasticsearch indices: {}", e.message, e)
                // 애플리케이션 시작에 실패하지 않도록 예외를 흡수합니다
            }
        }
    }
}
