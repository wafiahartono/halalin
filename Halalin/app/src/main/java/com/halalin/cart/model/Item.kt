package com.halalin.cart.model

import com.halalin.util.prettyClassString
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.product.model.Product

data class Item(
    val id: String? = null,
    val product: Product? = null,
    val quantity: Int? = null,
    val vendor: Vendor? = null
) {
    override fun toString() = """
        |Product {
        |    id: ${id ?: "_"}
        |    product: ${product ?: "_"}
        |    quantity: ${quantity ?: "_"}
        |    vendor: ${vendor ?: "_"}
        |}""".prettyClassString()
}
