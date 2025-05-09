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
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import ym_cosmetic.pick_perfume_be.search.document.PerfumeDocument

@Configuration
@EnableElasticsearchRepositories("ym_cosmetic.pick_perfume_be.search.repository")
class ElasticsearchConfig(
    @Value("\${elasticsearch.host:localhost}") private val host: String,
    @Value("\${elasticsearch.port:9200}") private val port: Int
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ElasticsearchConfig::class.java)
    }

    @Bean
    fun elasticsearchClient(): ElasticsearchClient {
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
    fun indexInitializer(elasticsearchOperations: ElasticsearchOperations): InitializingBean {
        return InitializingBean {
            try {
                if (!elasticsearchOperations.indexOps(PerfumeDocument::class.java).exists()) {
                    elasticsearchOperations.indexOps(PerfumeDocument::class.java).create()
                }
            } catch (e: Exception) {
                logger.error("Failed to initialize Elasticsearch indexes", e)
            }
        }
    }
}