package com.example.engine

import com.example.data.Draw
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class NumberStats(
    val number: Int,
    val frequency: Int,
    val rate: Double,
    val currentGap: Int,
    val maxGap: Int,
    val meanGap: Double,
    val wilsonLow: Double,
    val wilsonHigh: Double,
    val score: Double
)

data class RelationAB(
    val numA: Int,
    val numB: Int,
    val count: Int,
    val totalTransitions: Int,
    val probability: Double,
    val lift: Double
)

data class CombinationStat(
    val numbers: List<Int>,
    val count: Int,
    val lastAppearanceDate: String?
)

data class StrategyBacktestResult(
    val strategyName: String,
    val totalDrawsTested: Int,
    val matchDistribution: Map<Int, Int>, // 0 -> count, 1 -> count, 2 -> count, etc.
    val avgMatchesPerDraw: Double,
    val winRateAtLeast1: Double,
    val winRateAtLeast2: Double,
    val winRateAtLeast3: Double
)

data class RandomnessTestResult(
    val totalDraws: Int,
    val totalNumbersDrawn: Int,
    val expectedPerNumber: Double,
    val chiSquare: Double,
    val degreesOfFreedom: Int,
    val criticalValue95: Double,
    val isUniformConform: Boolean,
    val runsZScore: Double,
    val runsTestConform: Boolean,
    val evenPercentage: Double,
    val oddPercentage: Double,
    val decileDistribution: Map<String, Int>
)

object LotoEngine {

    const val TOTAL_NUMBERS = 90
    const val NUMBERS_PER_DRAW = 5
    const val Z_95 = 1.96

    /**
     * Calcule l'intervalle de Wilson à 95% pour un nombre d'occurrences k sur n tirages.
     */
    fun wilsonInterval(k: Int, n: Int, z: Double = Z_95): Pair<Double, Double> {
        if (n <= 0) return Pair(0.0, 0.0)
        val p = k.toDouble() / n
        val z2 = z * z
        val denominator = 1.0 + z2 / n
        val center = (p + z2 / (2 * n)) / denominator
        val margin = (z * sqrt((p * (1.0 - p) / n) + (z2 / (4.0 * n * n)))) / denominator
        val low = max(0.0, center - margin)
        val high = min(1.0, center + margin)
        return Pair(low, high)
    }

    /**
     * Analyse complète des 90 numéros sur l'historique fourni (ou sous-période)
     */
    fun computeNumberStats(draws: List<Draw>, recentWindow: Int = 30): List<NumberStats> {
        val n = draws.size
        if (n == 0) return emptyList()

        val recentDraws = draws.takeLast(recentWindow)
        val recentN = recentDraws.size

        // Tableau pour stocker pour chaque numéro 1..90 les indices de tirage (0-based)
        val appearances = Array(TOTAL_NUMBERS + 1) { mutableListOf<Int>() }

        for (i in draws.indices) {
            for (num in draws[i].numbers) {
                if (num in 1..TOTAL_NUMBERS) {
                    appearances[num].add(i)
                }
            }
        }

        val result = mutableListOf<NumberStats>()

        for (num in 1..TOTAL_NUMBERS) {
            val appList = appearances[num]
            val freq = appList.size
            val rate = freq.toDouble() / n

            // Retard actuel (nombre de tirages depuis la dernière sortie)
            val currentGap = if (appList.isEmpty()) n else (n - 1) - appList.last()

            // Écart maximal et moyen
            var maxGap = 0
            var sumGaps = 0
            if (appList.isEmpty()) {
                maxGap = n
                sumGaps = n
            } else {
                var prev = -1
                for (pos in appList) {
                    val gap = pos - prev - 1
                    if (gap > maxGap) maxGap = gap
                    sumGaps += gap
                    prev = pos
                }
                // Écart final jusqu'à la fin
                val tailGap = (n - 1) - appList.last()
                if (tailGap > maxGap) maxGap = tailGap
                sumGaps += tailGap
            }
            val meanGap = if (freq > 0) sumGaps.toDouble() / (freq + 1) else n.toDouble()

            // Wilson 95%
            val (wLow, wHigh) = wilsonInterval(freq, n)

            // Fréquence récente dans les derniers `recentWindow` tirages
            val recentFreq = recentDraws.count { it.numbers.contains(num) }
            val recentRate = if (recentN > 0) recentFreq.toDouble() / recentN else 0.0

            // Indice statistique composite (Momentum + Fréquence globale + Retard normalisé)
            // L'espérance théorique par tirage est 5/90 = 0.0555
            val expectedRate = NUMBERS_PER_DRAW.toDouble() / TOTAL_NUMBERS
            val momentumRatio = recentRate / expectedRate
            val globalRatio = rate / expectedRate
            val gapRatio = min(3.0, currentGap.toDouble() / max(1.0, meanGap))

            // Score centré autour de 50 (entre 0 et 100)
            val rawScore = 50.0 + (momentumRatio - 1.0) * 20.0 + (globalRatio - 1.0) * 15.0 + (gapRatio - 1.0) * 10.0
            val clampedScore = max(5.0, min(99.0, rawScore))

            result.add(
                NumberStats(
                    number = num,
                    frequency = freq,
                    rate = rate,
                    currentGap = currentGap,
                    maxGap = maxGap,
                    meanGap = meanGap,
                    wilsonLow = wLow,
                    wilsonHigh = wHigh,
                    score = clampedScore
                )
            )
        }

        return result
    }

    /**
     * Relations A → B (Suivi conditionnel : quand A sort au tirage t, quels B sortent au tirage t+1)
     */
    fun computeRelationsAB(draws: List<Draw>, targetNumA: Int): List<RelationAB> {
        val n = draws.size
        if (n < 2 || targetNumA !in 1..TOTAL_NUMBERS) return emptyList()

        val occurrencesB = IntArray(TOTAL_NUMBERS + 1)
        var transitionsCount = 0

        for (i in 0 until n - 1) {
            if (draws[i].numbers.contains(targetNumA)) {
                transitionsCount++
                val nextDrawNumbers = draws[i + 1].numbers
                for (b in nextDrawNumbers) {
                    if (b != targetNumA && b in 1..TOTAL_NUMBERS) {
                        occurrencesB[b]++
                    }
                }
            }
        }

        if (transitionsCount == 0) return emptyList()

        // Probabilité globale de chaque numéro
        val globalCounts = IntArray(TOTAL_NUMBERS + 1)
        for (d in draws) {
            for (num in d.numbers) {
                if (num in 1..TOTAL_NUMBERS) globalCounts[num]++
            }
        }

        val relations = mutableListOf<RelationAB>()
        for (b in 1..TOTAL_NUMBERS) {
            if (b == targetNumA) continue
            val count = occurrencesB[b]
            val prob = count.toDouble() / transitionsCount
            val baseProb = globalCounts[b].toDouble() / n
            val lift = if (baseProb > 0) prob / baseProb else 0.0

            relations.add(
                RelationAB(
                    numA = targetNumA,
                    numB = b,
                    count = count,
                    totalTransitions = transitionsCount,
                    probability = prob,
                    lift = lift
                )
            )
        }

        return relations.sortedWith(compareByDescending<RelationAB> { it.count }.thenByDescending { it.lift })
    }

    /**
     * Combinaisons récurrentes (Paires, Triplets, Quartets, Quintets)
     */
    fun computeTopCombinations(draws: List<Draw>, size: Int, limit: Int = 30): List<CombinationStat> {
        if (draws.isEmpty() || size !in 2..5) return emptyList()

        val combCounts = mutableMapOf<List<Int>, Int>()
        val lastSeen = mutableMapOf<List<Int>, String>()

        for (draw in draws) {
            val sorted = draw.sortedNumbers
            val combos = generateCombinations(sorted, size)
            for (combo in combos) {
                combCounts[combo] = (combCounts[combo] ?: 0) + 1
                lastSeen[combo] = draw.date
            }
        }

        return combCounts.entries
            .asSequence()
            .map { (combo, count) ->
                CombinationStat(
                    numbers = combo,
                    count = count,
                    lastAppearanceDate = lastSeen[combo]
                )
            }
            .sortedByDescending { it.count }
            .take(limit)
            .toList()
    }

    private fun generateCombinations(list: List<Int>, k: Int): List<List<Int>> {
        val result = mutableListOf<List<Int>>()
        fun backtrack(start: Int, current: MutableList<Int>) {
            if (current.size == k) {
                result.add(ArrayList(current))
                return
            }
            for (i in start until list.size) {
                current.add(list[i])
                backtrack(i + 1, current)
                current.removeAt(current.size - 1)
            }
        }
        backtrack(0, mutableListOf())
        return result
    }

    /**
     * Moteur de Backtest Walk-Forward.
     * Compare 4 stratégies sur une fenêtre glissante W (ex: W=40).
     */
    fun runWalkForwardBacktest(
        draws: List<Draw>,
        windowSize: Int = 40
    ): List<StrategyBacktestResult> {
        val n = draws.size
        if (n <= windowSize + 5) return emptyList()

        val strategyNames = listOf(
            "Fréquence pure (Top sorties)",
            "Écart / Retard (Numéros en retard)",
            "Indice composite (Momentum)",
            "Relations A → B (Suivi dynamique)"
        )

        val distributions = List(strategyNames.size) { mutableMapOf<Int, Int>() }
        for (dist in distributions) {
            for (m in 0..5) dist[m] = 0
        }

        val totalEvaluated = n - windowSize

        for (i in windowSize until n) {
            val trainSlice = draws.subList(i - windowSize, i)
            val actualDraw = draws[i].numbers.toSet()
            val prevDraw = draws[i - 1].numbers

            // Stratégie 1 : Fréquence pure
            val freqCounts = IntArray(TOTAL_NUMBERS + 1)
            for (d in trainSlice) {
                for (num in d.numbers) freqCounts[num]++
            }
            val strat1Pred = (1..TOTAL_NUMBERS).sortedByDescending { freqCounts[it] }.take(5).toSet()

            // Stratégie 2 : Retards maximaux
            val lastSeenIndex = IntArray(TOTAL_NUMBERS + 1) { -1 }
            for ((idx, d) in trainSlice.withIndex()) {
                for (num in d.numbers) lastSeenIndex[num] = idx
            }
            val strat2Pred = (1..TOTAL_NUMBERS).sortedBy { lastSeenIndex[it] }.take(5).toSet()

            // Stratégie 3 : Indice composite
            val stats = computeNumberStats(trainSlice, recentWindow = min(20, windowSize / 2))
            val strat3Pred = stats.sortedByDescending { it.score }.take(5).map { it.number }.toSet()

            // Stratégie 4 : Relations A -> B d'après le tirage précédent
            val followerScores = IntArray(TOTAL_NUMBERS + 1)
            for (leader in prevDraw) {
                for (dIdx in 0 until trainSlice.size - 1) {
                    if (trainSlice[dIdx].numbers.contains(leader)) {
                        for (nextNum in trainSlice[dIdx + 1].numbers) {
                            followerScores[nextNum]++
                        }
                    }
                }
            }
            val strat4Pred = (1..TOTAL_NUMBERS).sortedByDescending { followerScores[it] }.take(5).toSet()

            val preds = listOf(strat1Pred, strat2Pred, strat3Pred, strat4Pred)
            for (s in preds.indices) {
                val matches = preds[s].intersect(actualDraw).size
                distributions[s][matches] = (distributions[s][matches] ?: 0) + 1
            }
        }

        val results = mutableListOf<StrategyBacktestResult>()
        for (s in strategyNames.indices) {
            val dist = distributions[s]
            var sumMatches = 0
            for ((m, count) in dist) {
                sumMatches += m * count
            }
            val avg = if (totalEvaluated > 0) sumMatches.toDouble() / totalEvaluated else 0.0
            val atLeast1 = if (totalEvaluated > 0) ((totalEvaluated - (dist[0] ?: 0)).toDouble() / totalEvaluated) * 100.0 else 0.0
            val atLeast2 = if (totalEvaluated > 0) (((dist[2] ?: 0) + (dist[3] ?: 0) + (dist[4] ?: 0) + (dist[5] ?: 0)).toDouble() / totalEvaluated) * 100.0 else 0.0
            val atLeast3 = if (totalEvaluated > 0) (((dist[3] ?: 0) + (dist[4] ?: 0) + (dist[5] ?: 0)).toDouble() / totalEvaluated) * 100.0 else 0.0

            results.add(
                StrategyBacktestResult(
                    strategyName = strategyNames[s],
                    totalDrawsTested = totalEvaluated,
                    matchDistribution = dist,
                    avgMatchesPerDraw = avg,
                    winRateAtLeast1 = atLeast1,
                    winRateAtLeast2 = atLeast2,
                    winRateAtLeast3 = atLeast3
                )
            )
        }

        return results
    }

    /**
     * Tests d'Aléa & Graine (Chi-carré et Runs Test de Wald-Wolfowitz)
     */
    fun computeRandomnessTests(draws: List<Draw>): RandomnessTestResult {
        val n = draws.size
        val totalNumbersDrawn = n * NUMBERS_PER_DRAW
        val expected = totalNumbersDrawn.toDouble() / TOTAL_NUMBERS

        val counts = IntArray(TOTAL_NUMBERS + 1)
        var totalEven = 0
        var totalOdd = 0
        val decileCounts = LinkedHashMap<String, Int>()

        for (dec in 0..8) {
            val start = dec * 10 + 1
            val end = (dec + 1) * 10
            decileCounts["$start-$end"] = 0
        }

        for (draw in draws) {
            for (num in draw.numbers) {
                if (num in 1..TOTAL_NUMBERS) {
                    counts[num]++
                    if (num % 2 == 0) totalEven++ else totalOdd++
                    val decIndex = (num - 1) / 10
                    val key = "${decIndex * 10 + 1}-${(decIndex + 1) * 10}"
                    decileCounts[key] = (decileCounts[key] ?: 0) + 1
                }
            }
        }

        // Calcul du Chi-deux
        var chiSquare = 0.0
        for (num in 1..TOTAL_NUMBERS) {
            val diff = counts[num] - expected
            chiSquare += (diff * diff) / expected
        }

        val df = TOTAL_NUMBERS - 1 // 89 degrés de liberté
        val critical95 = 112.0 // Valeur critique chi²(89) à p=0.05
        val isUniformConform = chiSquare <= critical95

        // Wald-Wolfowitz Runs Test sur la parité (majorité paire vs majorité impaire)
        val paritySeq = draws.map { d -> d.evenCount >= 3 }
        var n1 = 0 // Pairs dominants
        var n2 = 0 // Impairs dominants
        var runs = 1

        for (i in paritySeq.indices) {
            if (paritySeq[i]) n1++ else n2++
            if (i > 0 && paritySeq[i] != paritySeq[i - 1]) {
                runs++
            }
        }

        val expectedRuns = if (n1 + n2 > 0) ((2.0 * n1 * n2) / (n1 + n2)) + 1.0 else 1.0
        val denomVar = (n1 + n2).toDouble() * (n1 + n2).toDouble() * (n1 + n2 - 1).toDouble()
        val varRuns = if (denomVar > 0) {
            (2.0 * n1 * n2 * (2.0 * n1 * n2 - n1 - n2)) / denomVar
        } else 1.0
        val sdRuns = sqrt(max(0.0001, varRuns))
        val zScore = (runs - expectedRuns) / sdRuns
        val runsConform = abs(zScore) <= Z_95

        val evenPct = if (totalNumbersDrawn > 0) (totalEven.toDouble() / totalNumbersDrawn) * 100.0 else 50.0
        val oddPct = if (totalNumbersDrawn > 0) (totalOdd.toDouble() / totalNumbersDrawn) * 100.0 else 50.0

        return RandomnessTestResult(
            totalDraws = n,
            totalNumbersDrawn = totalNumbersDrawn,
            expectedPerNumber = expected,
            chiSquare = chiSquare,
            degreesOfFreedom = df,
            criticalValue95 = critical95,
            isUniformConform = isUniformConform,
            runsZScore = zScore,
            runsTestConform = runsConform,
            evenPercentage = evenPct,
            oddPercentage = oddPct,
            decileDistribution = decileCounts
        )
    }

    /**
     * Générateur de grilles optimisées pour le jeu
     */
    fun generateSmartGrids(
        draws: List<Draw>,
        count: Int = 5,
        strategy: String = "Équilibrée"
    ): List<List<Int>> {
        val stats = computeNumberStats(draws)
        val sortedByScore = stats.sortedByDescending { it.score }
        val sortedByDelay = stats.sortedByDescending { it.currentGap }
        val sortedByFreq = stats.sortedByDescending { it.frequency }

        val grids = mutableListOf<List<Int>>()
        val random = java.util.Random(System.currentTimeMillis())

        for (g in 0 until count) {
            val gridSet = mutableSetOf<Int>()
            when (strategy) {
                "Plein Momentum" -> {
                    // Prendre parmi le top 20 des meilleurs scores
                    val pool = sortedByScore.take(20).map { it.number }.shuffled(random)
                    gridSet.addAll(pool.take(5))
                }
                "Chasseurs de Retard" -> {
                    // Prendre parmi les numéros les plus en retard
                    val pool = sortedByDelay.take(20).map { it.number }.shuffled(random)
                    gridSet.addAll(pool.take(5))
                }
                else -> {
                    // Équilibrée : 2 chauds + 2 froids (en retard) + 1 selon momentum
                    val hots = sortedByFreq.take(15).map { it.number }.shuffled(random).take(2)
                    val delays = sortedByDelay.take(15).map { it.number }.shuffled(random).take(2)
                    val momentum = sortedByScore.take(25).map { it.number }.shuffled(random).take(1)

                    gridSet.addAll(hots)
                    gridSet.addAll(delays)
                    gridSet.addAll(momentum)

                    // Compléter si doublons
                    var poolIdx = 0
                    while (gridSet.size < 5) {
                        gridSet.add(sortedByScore[poolIdx % sortedByScore.size].number)
                        poolIdx++
                    }
                }
            }
            grids.add(gridSet.toList().sorted())
        }

        return grids
    }
}
