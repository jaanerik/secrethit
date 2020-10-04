package com.jaanerikpihel.secrethit.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResetMessage (
        @JsonProperty("message")
        var message: String
)