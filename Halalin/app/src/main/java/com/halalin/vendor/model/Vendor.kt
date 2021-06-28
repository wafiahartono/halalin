package com.halalin.vendor.model

import android.net.Uri
import com.halalin.service.model.Service
import com.halalin.util.prettyClassString
import com.halalin.util.prettyString
import com.halalin.vendor.review.model.Review

data class Vendor(
    val contacts: Map<Contact, String>? = null,
    val description: String? = null,
    val id: String? = null,
    val latestReviewList: List<Review>? = null,
    val location: String? = null,
    val logoUrl: String? = null,
    val name: String? = null,
    val priceRange: PriceRange? = null,
    val rating: Float? = null,
    val reviewNumber: Int? = null,
    val serviceList: List<Service>? = null
) {
    companion object {
        fun getContactUri(type: Contact, data: String): Uri = Uri.parse(
            when (type) {
                Contact.FACEBOOK -> "https://www.facebook.com/${data}"
                Contact.INSTAGRAM -> "https://www.instagram.com/${data}"
                Contact.PHONE -> "tel:{${data}}"
                Contact.WEBSITE -> data
                Contact.WHATSAPP -> "https://wa.me/${data}"
            }
        )
    }

    override fun toString() = """
        |Vendor {
        |    contacts: ${contacts ?: "_"}
        |    description: ${description ?: "_"}
        |    id: ${id ?: "_"}
        |    latestReviewList: ${latestReviewList?.prettyString(2) ?: "_"}
        |    location: ${location ?: "_"}
        |    logoUrl: ${logoUrl ?: "_"}
        |    name: ${name ?: "_"}
        |    priceRange: ${priceRange ?: "_"}
        |    rating ${rating ?: "_"}
        |    reviewNumber: ${reviewNumber ?: "_"}
        |    serviceList: ${serviceList?.prettyString(2) ?: "_"}
        |}""".prettyClassString()

    enum class Contact {
        FACEBOOK, INSTAGRAM, PHONE, WEBSITE, WHATSAPP
    }

    enum class PriceRange {
        LOW, MEDIUM, HIGH
    }
}
