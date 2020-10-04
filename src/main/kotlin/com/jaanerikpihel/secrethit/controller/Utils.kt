package com.jaanerikpihel.secrethit.controller

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.Player
import com.jaanerikpihel.secrethit.service.LIBERAL
import org.apache.tomcat.util.buf.StringUtils
import java.util.*


fun introMessage(allPlayers: MutableList<Player>, isFascist: Boolean, role: String): String {
    val information: String = if (isFascist) {
        "Hitler doesn't see this message. You have 10-15 seconds to memorise your team. Your team:" +
                allPlayers
                        .filter { it.role != LIBERAL }
                        .map { "${it.name} is ${it.role}" }
                        .run { ";" + StringUtils.join(this, ';') }
    } else {
        "Please look at the screen for 10-15 seconds to allow Fascists to learn roles of the players."
    }
    mapOf("role" to role)
    mapOf("information" to information)
    return Gson().toJson(mapOf(
            "Introduction" to mapOf(
                    "role" to role,
                    "extraInfo" to information
            )
    ))
}

fun waitAndDo(waitFor: Long, function: () -> Unit) {
    Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    function()
                }
            },
            waitFor
    )
}
