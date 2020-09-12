package com.jaanerikpihel.secrethit.controller

import java.util.*


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
