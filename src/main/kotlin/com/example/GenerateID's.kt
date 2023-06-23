package com.example

val totalCount = 50
fun generateRandomId(totalCount: Int): Int {
    val numbers = (1..totalCount).toMutableList()
    numbers.shuffle()
    return numbers.first()
}
