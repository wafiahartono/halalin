package com.halalin.vendor.product.model

import com.halalin.util.prettyClassString

data class Product(
    val description: String? = null,
    val id: String? = null,
    val imageUrl: String? = null,
    val name: String? = null,
    val price: Float? = null
) {
    override fun toString() = """
        |Product {
        |    description: ${description ?: "_"}
        |    id: ${id ?: "_"}
        |    imageUrl: ${imageUrl ?: "_"}
        |    name: ${name ?: "_"}
        |    price: ${price ?: "_"}
        |}""".prettyClassString()
}
