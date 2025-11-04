package com.animal.avatar.charactor.maker.core.extensions

import kotlin.ranges.random

fun generateRandomFileName(): String {
    val randomNumber = (100000000000..999999999999).random()
    return "IMG_$randomNumber.png"
}