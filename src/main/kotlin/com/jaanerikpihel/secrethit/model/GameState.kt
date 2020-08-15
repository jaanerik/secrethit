package com.jaanerikpihel.secrethit.model

import com.google.gson.Gson
import com.jaanerikpihel.secrethit.model.GameState.Companion.REGISTER

class GameState() {

    var gameState = REGISTER
    var facPolicies: Int = 0
    var libPolicies: Int = 0
    var cardPack: CardPack = CardPack()
    var lastGovernment: Pair<Player, Player>? = null
    var president: Player? = null
    var chancellor: Player? = null
    var players: List<Player>? = null
    var failedGovernments: Int = 0
    var extraInfo: String = "{}"
    var alivePlayerOrder: List<Player> = emptyList()

    constructor(
            gameState: String = "",
            facPolicies: Int = 0,
            libPolicies: Int = 0,
            cardPack: CardPack = CardPack(),
            lastGovernment: Pair<Player, Player>? = null,
            president: Player? = null,
            chancellor: Player? = null,
            players: List<Player>? = null,
            nullGovernments: Int = 0,
            extraInfo: String = "{}"
    ) : this() {
        this.gameState = gameState
        this.facPolicies = facPolicies
        this.libPolicies = libPolicies
        this.lastGovernment = lastGovernment
        this.cardPack = cardPack
        this.lastGovernment = lastGovernment
        this.president = president
        this.chancellor = chancellor
        this.players = players
        this.failedGovernments = nullGovernments
        this.extraInfo = extraInfo
    }

    companion object {
        const val VOTE_RESULTS = "VoteResults"
        const val REGISTER = "Register"
        const val ENACTING = "Enacting"
        const val VOTING = "Voting"
        const val INTRODUCTION = "Introduction"
        const val LIB_VICTORY = "LiberalVictory"
        const val FAC_VICTORY = "FascistVictory"

    }

    override fun toString(): String {
        return "GameState(gameState='$gameState', facPolicies=$facPolicies, libPolicies=$libPolicies, " +
                "cardPack=${cardPack.cards.size}, lastGovernment=$lastGovernment, president=$president, " +
                "chancellor=$chancellor, players=$players, nullGovernments=$failedGovernments)"
    }

    fun toJSON(): String {
        return GameStateShareable(this).toJSON()
    }

}

class GameStateShareable(gameState: GameState) {

    private var gameState = REGISTER
    private var fascistCardsPlayed: Int = 0
    private var liberalCardsPlayed: Int = 0
    private var cardPackSize: Int = 0
    private var lastGovernment: Pair<String, String>? = null
    private var president: String = ""
    private var chancellor: String = ""
    private var players: List<String>? = null
    private var nullGovernments: Int = 0
    private var extraInfo: String = "{}"

    init {
        this.gameState = gameState.gameState
        this.fascistCardsPlayed = gameState.facPolicies
        this.liberalCardsPlayed = gameState.libPolicies
        this.lastGovernment = gameState.lastGovernment?.run { Pair(this.first.name, this.second.name) }
        this.cardPackSize = gameState.cardPack.cards.size
        this.president = gameState.president?.name ?: ""
        this.chancellor = gameState.chancellor?.name ?: ""
        this.players = gameState.players?.map { it.name }
        this.nullGovernments = gameState.failedGovernments
        this.extraInfo = gameState.extraInfo
    }

    fun toJSON(): String {
        return Gson().toJson(this)
    }

}

class CardPack {

    var cards: MutableList<String> = (MutableList(11) { "fascist" } + MutableList(6) { "liberal" })
            .shuffled() as MutableList<String>

    fun takeN(n: Int): List<String> {
        val takenCards = cards.toList().subList(0, n)
        for (i in 0 until n) {
            cards.removeAt(0)
            if (cards.isEmpty()) newPack()
        }
        return takenCards
    }

    fun takeThree(): List<String> {
        if (cards.size < 3) {
            val howManyNew = 3 - cards.size
            val oldCards = takeN(cards.size)
            return oldCards.map { it.toUpperCase() } + takeN(howManyNew)
        }
        return takeN(3)
    }

    fun newPack() {
        cards = (MutableList(11) { "fascist" } + MutableList(6) { "liberal" }).shuffled() as MutableList<String>
    }
}
