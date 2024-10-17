package com.example.naisupho.interfaces

data class TravelTimeResponse(
    val rows: List<Row>
)

data class Row(
    val elements: List<Element>
)

data class Element(
    val status: String,
    val duration: Duration
)

data class Duration(
    val text: String
)