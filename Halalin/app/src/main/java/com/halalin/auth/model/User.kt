package com.halalin.auth.model

import com.halalin.util.prettyClassString

data class User(
    val displayName: String? = null,
    val emailAddress: String? = null,
    val id: String? = null,
    val password: String? = null,
    val profilePictureUrl: String? = null
) {
    override fun toString() = """
        |User {
        |    displayName: ${displayName ?: "_"}
        |    emailAddress: ${emailAddress ?: "_"}
        |    id: ${id ?: "_"}
        |    profilePictureUrl: ${profilePictureUrl ?: "_"}
        |}""".prettyClassString()
}
