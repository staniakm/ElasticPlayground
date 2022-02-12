package com.example.elastic_playgroud

import java.time.LocalDate

data class CarTrip(val car: Car, val runDate: LocalDate, val kilometers: Double)

data class Car(val carName: String, val make: String, val model: String)