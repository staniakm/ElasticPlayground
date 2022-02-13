package com.example.elastic_playgroud

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class CarTrip(val car: Car, val runDate: LocalDate, val kilometers: Double)

data class Car(val carName: String, val make: String, val model: String)

data class CarDistanceSummary(val car: String, val date: LocalDate, val range: Range)
data class Range(val average: Double, val sum: Double, @JsonProperty("value_count") val value: Int)