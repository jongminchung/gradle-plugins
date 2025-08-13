package util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

class PostmanPostProcessor(
    private val file: File,
    private val variables: Map<String, String>
) {
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    fun process() {
        if (!file.exists()) {
            println("Warning: Postman collection file does not exist: ${file.absolutePath}")
            return
        }

        try {
            val postman: MutableMap<String, Any> = objectMapper.readValue(file)

            @Suppress("UNCHECKED_CAST")
            val variablesList = (postman["variable"] as? MutableList<MutableMap<String, Any>>)
                ?: mutableListOf<MutableMap<String, Any>>().also {
                    postman["variable"] = it
                }

            // Add new variables
            variables.forEach { (key, value) ->
                // Check if variable already exists
                val existingVar = variablesList.find { it["key"] == key }
                if (existingVar != null) {
                    // Update existing variable
                    existingVar["value"] = value
                    println("Updated existing Postman variable: $key")
                } else {
                    // Add new variable
                    variablesList.add(
                        mutableMapOf(
                            "key" to key,
                            "value" to value,
                            "type" to "string",
                            "description" to "Auto-generated variable"
                        )
                    )
                    println("Added new Postman variable: $key")
                }
            }

            // Write back to file
            objectMapper.writeValue(file, postman)
            println("Successfully processed Postman collection with ${variables.size} variables")

        } catch (e: Exception) {
            println("Error: Failed to post-process Postman collection: ${e.message}")
            e.printStackTrace()
        }
    }
}
