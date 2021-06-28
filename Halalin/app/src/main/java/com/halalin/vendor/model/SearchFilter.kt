package com.halalin.vendor.model

data class SearchFilter(
    val serviceId: String?,
    val sort: Sort?
) {
    data class Sort(val field: Field, val order: Order) {
        enum class Field {
            LOCATION, NAME, RATING, REVIEW_NUMBER
        }

        enum class Order {
            ASC, DESC
        }
    }
}
