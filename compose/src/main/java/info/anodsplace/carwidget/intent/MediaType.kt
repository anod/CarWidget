package info.anodsplace.carwidget.intent

import java.util.*
import java.util.regex.Pattern

val MediaTypes = listOf(
        "application/",
        "application/javascript",
        "application/json",
        "application/pdf",
        "application/sql",
        "application/vnd.api+json",
        "application/x-www-form-urlencoded",
        "application/xml",
        "application/zip",
        "audio/",
        "audio/mpeg",
        "audio/ogg",
        "image/",
        "image/gif",
        "image/apng",
        "image/flif",
        "image/webp",
        "image/x-mng",
        "image/jpeg",
        "image/png",
        "multipart/form-data",
        "text/",
        "text/css",
        "text/csv",
        "text/html",
        "text/php",
        "text/plain",
        "text/xml",
        "video/"
).associateBy { it }

private const val TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)"
private const val QUOTED = "\"([^\"]*)\""
private val TYPE_SUBTYPE = Pattern.compile("$TOKEN/$TOKEN")
private val PARAMETER = Pattern.compile(";\\s*(?:$TOKEN=(?:$TOKEN|$QUOTED))?")

fun String.toMediaType(): Triple<String, String, List<String>> {
    val typeSubtype = TYPE_SUBTYPE.matcher(this)
    require(typeSubtype.lookingAt()) { "No subtype found for: \"$this\"" }
    val type = typeSubtype.group(1).toLowerCase(Locale.US)
    val subtype = typeSubtype.group(2).toLowerCase(Locale.US)

    val parameterNamesAndValues = mutableListOf<String>()
    val parameter = PARAMETER.matcher(this)
    var s = typeSubtype.end()
    while (s < length) {
        parameter.region(s, length)
        require(parameter.lookingAt()) {
            "Parameter is not formatted correctly: \"${substring(s)}\" for: \"$this\""
        }

        val name = parameter.group(1)
        if (name == null) {
            s = parameter.end()
            continue
        }

        val token = parameter.group(2)
        val value = when {
            token == null -> {
                // Value is "double-quoted". That's valid and our regex group already strips the quotes.
                parameter.group(3)
            }
            token.startsWith("'") && token.endsWith("'") && token.length > 2 -> {
                // If the token is 'single-quoted' it's invalid! But we're lenient and strip the quotes.
                token.substring(1, token.length - 1)
            }
            else -> token
        }

        parameterNamesAndValues += name
        parameterNamesAndValues += value
        s = parameter.end()
    }

    return Triple(type, subtype, parameterNamesAndValues)
}

fun String.toMediaTypeOrNull(): Triple<String, String, List<String>>? {
    return try {
        toMediaType()
    } catch (_: IllegalArgumentException) {
        null
    }
}