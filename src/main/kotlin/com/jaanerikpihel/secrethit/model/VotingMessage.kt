package com.jaanerikpihel.secrethit.model

import com.fasterxml.jackson.annotation.JsonProperty

data class VotingMessage(
        @JsonProperty("chancellor")
        var chancellor: String,
        @JsonProperty("yayOrNay")
        var yayOrNay: String
)