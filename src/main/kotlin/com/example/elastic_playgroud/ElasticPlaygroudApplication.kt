package com.example.elastic_playgroud

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.security.RefreshPolicy
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.random.Random
import kotlin.streams.toList
import kotlin.system.measureTimeMillis


val cars: List<Car> = listOf(
    Car("USA car 1", "Ford", "Mondeo"),
    Car("USA car 2", "Ford", "Kuga"),
    Car("Europe car 1", "Honda", "Civic"),
    Car("Europe car 2", "Honda", "HRV"),
    Car("Japan car 1", "Toyota", "Avensis"),
)

val dateRange: List<LocalDate> =
    LocalDate.of(2022, 1, 1).datesUntil(LocalDate.of(2022, 3, 30)).toList()


@SpringBootApplication
class ElasticPlaygroudApplication(private val elasticLoader: ElasticLoader) : CommandLineRunner {
    override fun run(vararg args: String?) {
        elasticLoader.loadRandomData()
    }
}

fun main(args: Array<String>) {
    runApplication<ElasticPlaygroudApplication>(*args)
}

@Service
class ElasticLoader(private val client: RestHighLevelClient, private val objectMapper: ObjectMapper) {
    fun loadRandomData() {

        (0..2000).map {

            val document = IndexRequest("car-trip")
                .id(it.toString())
                .source(
                    mapOf(
                        "car" to cars.random().carName,
                        "date" to dateRange.random(),
                        "range" to Random.nextDouble(100.0)
                    )
                )

            document
        }.windowed(500, 500)
            .map { requestList ->
                println("bulk size = ${requestList.size}")
                val bulkrequest = BulkRequest("car-trip")

                requestList.forEach {
                    bulkrequest.add(it)
                }
                bulkrequest.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
                measureTimeMillis {
                    client.bulk(bulkrequest, RequestOptions.DEFAULT).let {
                        println(it.status())
                    }
                }.let {
                    println("execution time: $it")
                }
            }
    }
}

@Configuration
class ElasticConfig {


    @Bean
    fun createClient(): RestHighLevelClient {
        return RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200)))
    }
}