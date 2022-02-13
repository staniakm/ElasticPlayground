package com.example.elastic_playgroud

import java.time.LocalDate
import java.time.LocalDateTime

data class ElastiCarRequest(val car: Car, val date: LocalDate, val range: Double, val creation_date: LocalDateTime)
