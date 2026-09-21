package com.example.data

data class Draw(
    val id: Int,
    val date: String,
    val numbers: List<Int>,
    val isCustom: Boolean = false
) {
    init {
        require(numbers.size == 5) { "Un tirage de Loto Niger doit comporter exactement 5 numéros." }
    }

    val sortedNumbers: List<Int> get() = numbers.sorted()
    val sum: Int get() = numbers.sum()
    val evenCount: Int get() = numbers.count { it % 2 == 0 }
    val oddCount: Int get() = 5 - evenCount
}
