package com.jaanerikpihel.secrethit.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CardPackTest {
    private val cardPack = CardPack()

    @Test
    internal fun testPeek() {
        val peekedCards = cardPack.peekThree()
        Assertions.assertThat(peekedCards.size).isEqualTo(3)
        Assertions.assertThat(cardPack.cards.size).isEqualTo(17)
    }

    @Test
    internal fun testPeekShortDeck() {
        cardPack.discardPile = cardPack.drawN(15) as MutableList<String>
        val peekedCards = cardPack.peekThree()
        Assertions.assertThat(peekedCards.size).isEqualTo(3)
        Assertions.assertThat(cardPack.cards.size).isEqualTo(2)
    }

    @Test
    internal fun testTakeShortDeck() {
        cardPack.discardPile = cardPack.drawN(15) as MutableList<String>
        val drawnCards = cardPack.drawThree()
        Assertions.assertThat(drawnCards.size).isEqualTo(3)
        Assertions.assertThat(cardPack.cards.size).isEqualTo(14)
        Assertions.assertThat(cardPack.discardPile.size).isEqualTo(0)
    }
}