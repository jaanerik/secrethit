package com.jaanerikpihel.secrethit.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscardMessage (
        @JsonProperty("cards")
        var cards: List<String>
)