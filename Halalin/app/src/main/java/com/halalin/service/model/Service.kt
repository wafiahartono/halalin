package com.halalin.service.model

import com.halalin.util.prettyClassString

data class Service(
    val iconUrl: String? = null,
    val id: String? = null,
    val imageUrl: String? = null,
    val name: String? = null
) {
    override fun toString() = """
        |Service {
        |    iconUrl: ${iconUrl ?: "_"}
        |    id: ${id ?: "_"}
        |    imageUrl: ${imageUrl ?: "_"}
        |    name: ${name ?: "_"}
        |}""".prettyClassString()
}
