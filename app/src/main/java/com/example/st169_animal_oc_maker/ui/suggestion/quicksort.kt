package com.example.st169_animal_oc_maker.ui.suggestion

fun partici(array: Array<Int>, low: Int, high: Int): Int {
    val pivot = array[high]
    var i = low - 1
    for (j in low until high) {
        if (array[j] <= pivot) {
            i++
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }
    val temp = array[i + 1]
    array[i + 1] = array[high]
    array[high] = temp
    return i + 1
}

fun quickSort(array: Array<Int>, low: Int, high: Int) {
    if (low < high) {
        val pi = partici(array, low, high)
        quickSort(array, low, pi - 1)
        quickSort(array, pi + 1, high)
    }
}

fun main() {
    val array = arrayOf(10, 7, 8, 9, 1, 5)
    val n = array.size
    quickSort(array, 0, n - 1)
    println("Sorted array:")
    for (i in 0 until n) {
        print("${array[i]} ")
    }
}