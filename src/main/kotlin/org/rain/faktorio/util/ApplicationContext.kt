package org.rain.faktorio.util

import io.ktor.server.application.Application

interface ApplicationContext {
    val application: Application
}