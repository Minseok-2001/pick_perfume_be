package ym_cosmetic.pick_perfume_be.search.config

import org.opensearch.client.RestHighLevelClient
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration
import org.opensearch.data.client.orhlc.ClientConfiguration
import org.opensearch.data.client.orhlc.RestClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.time.Duration

@Configuration
@EnableElasticsearchRepositories()
class OpenSearchConfig(
    @Value("\${spring.elasticsearch.uris:}") private val elasticsearchUri: String,
    @Value("\${spring.elasticsearch.username:}") private val username: String?,
    @Value("\${spring.elasticsearch.password:}") private val password: String?,
    @Value("\${spring.elasticsearch.connection-timeout:5s}") private val connectionTimeout: String,
    @Value("\${spring.elasticsearch.socket-timeout:60s}") private val socketTimeout: String
) : AbstractOpenSearchConfiguration() {


    @Bean
    override fun opensearchClient(): RestHighLevelClient {
        val isHttps = elasticsearchUri.startsWith("https://")
        val cleanUri = elasticsearchUri
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")

        val hostAndPort = cleanUri.split(":")
        val host = hostAndPort[0]
        val port = if (hostAndPort.size > 1) hostAndPort[1].toInt() else if (isHttps) 443 else 9200

        val clientConfigBuilder = ClientConfiguration.builder()
            .connectedTo("$host:$port")

        if (isHttps) {
            clientConfigBuilder.usingSsl()
        }
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            clientConfigBuilder.withBasicAuth(username, password)
        }

        val connectTimeoutSeconds = connectionTimeout.removeSuffix("s").toLong()
        val socketTimeoutSeconds = socketTimeout.removeSuffix("s").toLong()

        clientConfigBuilder
            .withConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .withSocketTimeout(Duration.ofSeconds(socketTimeoutSeconds))

        val clientConfiguration = clientConfigBuilder.build()
        return RestClients.create(clientConfiguration).rest()
    }

}