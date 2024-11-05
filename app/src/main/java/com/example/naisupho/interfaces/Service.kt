package com.example.naisupho.interfaces

//Matrix Distance API
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
//Postcode API
data class ZipcloudResponse(
    val message: String?,
    val results: List<AddressResult>?,
    val status: Int
)

data class AddressResult(
    val address1: String, // 都道府県名
    val address2: String, // 市区町村名
    val address3: String, // 町域名
    val kana1: String?,   // 都道府県名カナ
    val kana2: String?,   // 市区町村名カナ
    val kana3: String?,   // 町域名カナ
    val prefcode: String,
    val zipcode: String
)