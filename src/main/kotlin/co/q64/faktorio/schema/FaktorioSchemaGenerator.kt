package co.q64.faktorio.schema

import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion

internal val FaktorioSchemaGenerator by lazy {
    SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12).apply {
        forMethods().withIgnoreCheck { true }
    }.build().let(::SchemaGenerator)
}