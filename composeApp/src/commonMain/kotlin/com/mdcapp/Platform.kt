package com.mdcapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform