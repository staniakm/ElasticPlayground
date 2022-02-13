package com.example.elastic_playgroud

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.LocalDate

@JsonSerialize
data class Car(val carName: String, val make: String, val model: String)

data class CarDistanceSummary(val car: CarName, val date: LocalDate, val range: Range)
data class Range(val average: Double, val sum: Double, @JsonProperty("value_count") val value: Int)
data class CarName(val carName: String)