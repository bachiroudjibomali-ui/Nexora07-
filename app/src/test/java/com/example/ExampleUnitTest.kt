package com.example

import com.example.config.AccessControl
import com.example.config.UserTier
import com.example.data.HistoricalData
import com.example.engine.LotoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHistoricalDataIntegrity() {
        val draws = HistoricalData.DRAWS
        assertEquals("Historical data must have exactly 261 draws", 261, draws.size)
        val firstDraw = draws.first()
        val lastDraw = draws.last()

        assertEquals("01/01/2026", firstDraw.date)
        assertEquals("18/09/2026", lastDraw.date)

        for (draw in draws) {
            assertEquals(5, draw.numbers.size)
            assertTrue(draw.numbers.all { it in 1..90 })
            assertEquals(5, draw.numbers.toSet().size)
        }
    }

    @Test
    fun testLotoEngineStats() {
        val stats = LotoEngine.computeNumberStats(HistoricalData.DRAWS)
        assertEquals(90, stats.size)
        val totalOccurrences = stats.sumOf { it.frequency }
        assertEquals(261 * 5, totalOccurrences)

        // Check Wilson Interval
        val wilson = LotoEngine.wilsonInterval(15, 261)
        assertTrue(wilson.first < wilson.second)
        assertTrue(wilson.first in 0.0..1.0)
        assertTrue(wilson.second in 0.0..1.0)
    }

    @Test
    fun testRelationsAB() {
        val relations = LotoEngine.computeRelationsAB(HistoricalData.DRAWS, 46)
        assertEquals(89, relations.size)
        assertTrue(relations.all { it.numA == 46 && it.numB != 46 })
    }

    @Test
    fun testAccessControlTiers() {
        assertEquals(UserTier.FREE, AccessControl.fromServerTier(null))
        assertEquals(UserTier.FREE, AccessControl.fromServerTier(""))
        assertEquals(UserTier.FREE, AccessControl.fromServerTier("free"))
        assertEquals(UserTier.PREMIUM, AccessControl.fromServerTier("prem"))
        assertEquals(UserTier.PREMIUM_PLUS, AccessControl.fromServerTier("plus"))

        assertTrue(AccessControl.LIMITS.canAccessAnalysisFull(UserTier.PREMIUM))
        assertTrue(AccessControl.LIMITS.canAccessBacktest(UserTier.PREMIUM_PLUS))
    }
}
