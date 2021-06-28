package com.halalin.vendor.review.model

import com.halalin.auth.model.User
import com.halalin.util.prettyClassString
import java.util.*

data class Review(
    val comment: String? = null,
    val date: Date? = null,
    val id: String? = null,
    val rating: Int? = null,
    val user: User? = null
) {
    override fun toString() = """
        |Review {
        |    comment: ${comment ?: "_"}
        |    date: ${date ?: "_"}
        |    id: ${id ?: "_"}
        |    rating: ${rating ?: "_"}
        |    user: ${user ?: "_"}
        |}""".prettyClassString()
}
