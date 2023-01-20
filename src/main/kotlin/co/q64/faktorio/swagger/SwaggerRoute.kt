@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package co.q64.faktorio.swagger

import co.q64.faktorio.FaktorioConfig
import co.q64.faktorio.internal.endpoints
import co.q64.faktorio.util.swag
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.fromFilePath
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe

class SwaggerRoute(private val config: FaktorioConfig) {

    private val mapper by lazy {
        ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter()
    }

    fun route(app: Application) {
        val paths = Paths().apply {
            app.endpoints.filter { !it.secret }.toList().groupBy { it.path }
                .map { (path, points) ->
                    addPathItem(path, PathItem().apply {
                        points.forEach { endpoint ->
                            operation(endpoint.method.swag, endpoint.build(app))
                        }
                    })
                }
        }
        val api = OpenAPI().apply { paths(paths) }
        app.routing {
            configureSwagger(mapper.writeValueAsString(api))
        }

    }

    /**
     * Method from ktor is copied here as it is internal and cannot be accessed
     * using @Suppress("INVISIBLE_REFERENCE") because of weird kotlins.
     * @see io.ktor.server.plugins.swagger.swaggerUI
     */
    private fun Route.configureSwagger(api: String) {


        with(config) {
            route(swagger.docs) {
                get(swagger.api) {
                    call.respondText(api, ContentType.fromFilePath(swagger.api).firstOrNull())
                }
                get {
                    val fullPath = call.request.path()
                    call.respondHtml {
                        head {
                            title { +"Swagger UI" }
                            link(
                                href = "${swagger.packageLocation}@${swagger.version}/swagger-ui.css",
                                rel = "stylesheet"
                            )
                            swagger.customStyle?.let {
                                link(href = it, rel = "stylesheet")
                            }
                        }
                        body {
                            div { id = "swagger-ui" }
                            script(src = "${swagger.packageLocation}@${swagger.version}/swagger-ui-bundle.js") {
                                attributes["crossorigin"] = "anonymous"
                            }

                            val src = "${swagger.packageLocation}@${swagger.version}/swagger-ui-standalone-preset.js"
                            script(src = src) {
                                attributes["crossorigin"] = "anonymous"
                            }

                            script {
                                unsafe {
                                    +"""
window.onload = function() {
    window.ui = SwaggerUIBundle({
        url: '$fullPath/${swagger.api}',
        dom_id: '#swagger-ui',
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: 'StandaloneLayout'
    });
}
                            """.trimIndent()
                                }
                            }
                        }
                    }
                }
            }
        }

    }


}