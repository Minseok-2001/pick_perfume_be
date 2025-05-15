package ym_cosmetic.pick_perfume_be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [ElasticsearchDataAutoConfiguration::class])
class PickPerfumeBeApplication

fun main(args: Array<String>) {
    runApplication<PickPerfumeBeApplication>(*args)
}
