package com.jaanerikpihel.secrethit.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PresidentPowerMessage(
        @JsonProperty("power")
        var power: String,
        @JsonProperty("object")
        var theObject: String
)