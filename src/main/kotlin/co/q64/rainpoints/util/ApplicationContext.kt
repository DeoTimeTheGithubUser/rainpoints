package co.q64.rainpoints.util

import io.ktor.server.application.Application

interface ApplicationContext {
    val application: Application
}