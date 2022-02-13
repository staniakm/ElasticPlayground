package com.example.elastic_playgroud

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.xcontent.XContentType
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random
import kotlin.streams.toList
import kotlin.system.measureTimeMillis


val cars: List<Car> = listOf(
    Car("USA car 1", "Ford", "Mondeo"),
    Car("USA car 2", "Ford", "Kuga"),
    Car("USA car 3", "Ford", "Avenger"),
    Car("USA car 4", "Ford", "F150"),
    Car("Europe car 1", "Honda", "Civic"),
    Car("Europe car 2", "Honda", "HRV"),
    Car("Japan car 1", "Toyota", "Avensis"),
)

val dateRange: List<LocalDate> =
    LocalDate.of(2022, 1, 1).datesUntil(LocalDate.of(2022, 3, 30)).toList()


@SpringBootApplication
class ElasticPlaygroudApplication(private val elasticLoader: ElasticLoader) : CommandLineRunner {
    override fun run(vararg args: String?) {
//        elasticLoader.loadRandomData()
    }
}

fun main(args: Array<String>) {
    runApplication<ElasticPlaygroudApplication>(*args)
}

@Service
class ElasticLoader(private val client: RestHighLevelClient, private val objectMapper: ObjectMapper) {
    fun loadRandomData() {

        (0..2000)
            .map {
                ElastiCarRequest(
                    cars.random(),
                    dateRange.random(),
                    Random.nextDouble(100.0),
                    LocalDateTime.now()
                ).let { request ->
                    IndexRequest("car-trip")
                        .id(UUID.randomUUID().toString())
                        .source(
                            objectMapper.writeValueAsString(request), XContentType.JSON
                        )
                }
            }.windowed(500, 500)
            .map { requestList ->
                println("bulk size = ${requestList.size}")
                val bulkrequest = BulkRequest("car-trip")

                requestList.forEach {
                    bulkrequest.add(it)
                }
                bulkrequest.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
                measureTimeMillis {
                    println(client.bulk(bulkrequest, RequestOptions.DEFAULT).status())
                }.let {
                    println("execution time: $it")
                }
            }
    }

    fun getCarSummaryPerCarName(carName: String): List<CarDistanceSummary> {
        val response: SearchResponse = SearchSourceBuilder()
            .query(QueryBuilders.termQuery("car.carName", carName))
            .let { query ->
                SearchRequest("car-summary")
                    .source(query)
            }.let {
                client.search(it, RequestOptions.DEFAULT)
            }

        return response.hits.map {
            objectMapper.readValue(it.sourceAsString, CarDistanceSummary::class.java)
        }

    }
}

@RestController
@RequestMapping("/elastic")
class FetchDataController(private val elasticLoader: ElasticLoader) {

    @GetMapping("/sampleData")
    fun createSampleData() = elasticLoader.loadRandomData()

    @GetMapping("")
    fun getData(@RequestParam carName: String) = elasticLoader.getCarSummaryPerCarName(carName)
}

@Configuration
class ElasticConfig {


    @Bean
    fun createClient(): RestHighLevelClient {
        return RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200)))
    }
}