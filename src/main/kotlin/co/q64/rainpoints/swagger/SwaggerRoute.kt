@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package co.q64.rainpoints.swagger

import co.q64.rainpoints.RainpointsConfig
import co.q64.rainpoints.endpoint.endpoints
import co.q64.rainpoints.impl.RainEndpoint
import co.q64.rainpoints.scope.scopes
import co.q64.rainpoints.util.swag
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
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityScheme
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe

class SwaggerRoute(private val config: RainpointsConfig) {

    private val mapper by lazy {
        ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter()
    }

    fun route(app: Application) {
        val paths = Paths().apply {
            app.endpoints
                .asSequence()
                .filter { !it.secret }
                .filterIsInstance<RainEndpoint>()
                .toList().groupBy { it.path }
                .map { (path, points) ->
                    addPathItem(path, PathItem().apply {
                        points.forEach { endpoint ->
                            operation(endpoint.method.swag, endpoint.build(app).apply {
                                if (config.swagger.defaultDescriptionByScope && endpoint.description == null)
                                    description = endpoint.scope?.description
                            })
                        }
                    })
                }
                .toList()
        }
        val api = OpenAPI().apply {
            info = config.swagger.info
            config.swagger.oauth?.let { auth ->
                schemaRequirement("rain", SecurityScheme().apply {
                    name = "Bearer"
                    `in` = SecurityScheme.In.HEADER
                    bearerFormat
                    type = SecurityScheme.Type.OAUTH2
                    flows = OAuthFlows().authorizationCode(auth.apply {
                        scopes = Scopes().apply {
                            app.scopes.forEach { addString(it.path, it.description) }
                        }
                    })
                })
            }
            paths(paths)
        }
        app.routing {
            val json = mapper.writeValueAsString(api)
                .replace("\"type\" : \"OAUTH2\",", "\"type\" : \"oauth2\",") // ???
            configureSwagger(json)
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