package com.halalin.message.model

import com.halalin.util.prettyClassString
import java.util.*

data class Message(
    val date: Date? = null,
    val fromUser: Boolean? = null,
    val id: String? = null,
    val text: String? = null
) {
    override fun toString() = """
        |SignInMessage {
        |    date: ${date ?: "_"}
        |    fromUser: ${fromUser ?: "_"}
        |    id: ${id ?: "_"}
        |    text: ${text ?: "_"}
        |}""".prettyClassString()
}
