/**
 * Moteur Statistique et Mathématique Loto Niger (Node.js)
 * Réutilise fidèlement les calculs statistiques de l'application.
 */

const TOTAL_NUMBERS = 90;
const NUMBERS_PER_DRAW = 5;
const Z_95 = 1.96;

function wilsonInterval(k, n, z = Z_95) {
    if (n <= 0) return [0.0, 0.0];
    const p = k / n;
    const z2 = z * z;
    const denominator = 1.0 + z2 / n;
    const center = (p + z2 / (2 * n)) / denominator;
    const margin = (z * Math.sqrt((p * (1.0 - p) / n) + (z2 / (4.0 * n * n)))) / denominator;
    const low = Math.max(0.0, center - margin);
    const high = Math.min(1.0, center + margin);
    return [low, high];
}

function computeNumberStats(draws, recentWindow = 30) {
    const n = draws.length;
    if (n === 0) return [];

    const recentDraws = draws.slice(Math.max(0, n - recentWindow));
    const recentN = recentDraws.length;

    const appearances = Array.from({ length: TOTAL_NUMBERS + 1 }, () => []);

    for (let i = 0; i < draws.length; i++) {
        for (const num of draws[i].numbers) {
            if (num >= 1 && num <= TOTAL_NUMBERS) {
                appearances[num].push(i);
            }
        }
    }

    const result = [];
    const expectedRate = NUMBERS_PER_DRAW / TOTAL_NUMBERS;

    for (let num = 1; num <= TOTAL_NUMBERS; num++) {
        const appList = appearances[num];
        const freq = appList.length;
        const rate = freq / n;

        const currentGap = appList.length === 0 ? n : (n - 1) - appList[appList.length - 1];

        let maxGap = 0;
        let sumGaps = 0;
        if (appList.length === 0) {
            maxGap = n;
            sumGaps = n;
        } else {
            let prev = -1;
            for (const pos of appList) {
                const gap = pos - prev - 1;
                if (gap > maxGap) maxGap = gap;
                sumGaps += gap;
                prev = pos;
            }
            const tailGap = (n - 1) - appList[appList.length - 1];
            if (tailGap > maxGap) maxGap = tailGap;
            sumGaps += tailGap;
        }
        const meanGap = freq > 0 ? sumGaps / (freq + 1) : n;

        const [wLow, wHigh] = wilsonInterval(freq, n);

        const recentFreq = recentDraws.filter(d => d.numbers.includes(num)).length;
        const recentRate = recentN > 0 ? recentFreq / recentN : 0.0;

        const momentumRatio = recentRate / expectedRate;
        const globalRatio = rate / expectedRate;
        const gapRatio = Math.min(3.0, currentGap / Math.max(1.0, meanGap));

        const rawScore = 50.0 + (momentumRatio - 1.0) * 20.0 + (globalRatio - 1.0) * 15.0 + (gapRatio - 1.0) * 10.0;
        const clampedScore = Math.max(5.0, Math.min(99.0, rawScore));

        result.push({
            number: num,
            frequency: freq,
            rate: rate,
            currentGap: currentGap,
            maxGap: maxGap,
            meanGap: meanGap,
            wilsonLow: wLow,
            wilsonHigh: wHigh,
            score: clampedScore
        });
    }

    return result;
}

function computeRelationsAB(draws, targetNumA) {
    const n = draws.length;
    if (n < 2 || targetNumA < 1 || targetNumA > TOTAL_NUMBERS) return [];

    const occurrencesB = new Array(TOTAL_NUMBERS + 1).fill(0);
    let transitionsCount = 0;

    for (let i = 0; i < n - 1; i++) {
        if (draws[i].numbers.includes(targetNumA)) {
            transitionsCount++;
            for (const b of draws[i + 1].numbers) {
                if (b !== targetNumA && b >= 1 && b <= TOTAL_NUMBERS) {
                    occurrencesB[b]++;
                }
            }
        }
    }

    if (transitionsCount === 0) return [];

    const globalCounts = new Array(TOTAL_NUMBERS + 1).fill(0);
    for (const d of draws) {
        for (const num of d.numbers) {
            if (num >= 1 && num <= TOTAL_NUMBERS) globalCounts[num]++;
        }
    }

    const relations = [];
    for (let b = 1; b <= TOTAL_NUMBERS; b++) {
        if (b === targetNumA) continue;
        const count = occurrencesB[b];
        const prob = count / transitionsCount;
        const baseProb = globalCounts[b] / n;
        const lift = baseProb > 0 ? prob / baseProb : 0.0;

        relations.push({
            numA: targetNumA,
            numB: b,
            count: count,
            totalTransitions: transitionsCount,
            probability: prob,
            lift: lift
        });
    }

    return relations.sort((a, b) => b.count - a.count || b.lift - a.lift);
}

function generateCombinations(arr, k) {
    const result = [];
    function backtrack(start, current) {
        if (current.length === k) {
            result.push([...current]);
            return;
        }
        for (let i = start; i < arr.length; i++) {
            current.push(arr[i]);
            backtrack(i + 1, current);
            current.pop();
        }
    }
    backtrack(0, []);
    return result;
}

function computeTopCombinations(draws, size, limit = 50) {
    if (!draws || draws.length === 0 || size < 2 || size > 5) return [];

    const combCounts = new Map();
    const lastSeen = new Map();

    for (const draw of draws) {
        const sorted = [...draw.numbers].sort((a, b) => a - b);
        const combos = generateCombinations(sorted, size);
        for (const combo of combos) {
            const key = combo.join('-');
            combCounts.set(key, (combCounts.get(key) || 0) + 1);
            lastSeen.set(key, draw.date);
        }
    }

    const list = [];
    for (const [key, count] of combCounts.entries()) {
        const numbers = key.split('-').map(Number);
        list.push({
            numbers,
            count,
            lastAppearanceDate: lastSeen.get(key)
        });
    }

    return list.sort((a, b) => b.count - a.count).slice(0, limit);
}

function runWalkForwardBacktest(draws, windowSize = 40) {
    const n = draws.length;
    if (n <= windowSize + 5) return [];

    const strategyNames = [
        "Fréquence pure (Top sorties)",
        "Écart / Retard (Numéros en retard)",
        "Indice composite (Momentum)",
        "Relations A → B (Suivi dynamique)"
    ];

    const distributions = strategyNames.map(() => ({ 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }));
    const totalEvaluated = n - windowSize;

    for (let i = windowSize; i < n; i++) {
        const trainSlice = draws.slice(i - windowSize, i);
        const actualDraw = new Set(draws[i].numbers);
        const prevDraw = draws[i - 1].numbers;

        // Stratégie 1 : Fréquence pure
        const freqCounts = new Array(TOTAL_NUMBERS + 1).fill(0);
        for (const d of trainSlice) {
            for (const num of d.numbers) freqCounts[num]++;
        }
        const strat1Pred = new Set(
            Array.from({ length: TOTAL_NUMBERS }, (_, idx) => idx + 1)
                .sort((a, b) => freqCounts[b] - freqCounts[a])
                .slice(0, 5)
        );

        // Stratégie 2 : Retards
        const lastSeenIndex = new Array(TOTAL_NUMBERS + 1).fill(-1);
        trainSlice.forEach((d, idx) => {
            d.numbers.forEach(num => lastSeenIndex[num] = idx);
        });
        const strat2Pred = new Set(
            Array.from({ length: TOTAL_NUMBERS }, (_, idx) => idx + 1)
                .sort((a, b) => lastSeenIndex[a] - lastSeenIndex[b])
                .slice(0, 5)
        );

        // Stratégie 3 : Indice composite
        const stats = computeNumberStats(trainSlice, Math.min(20, Math.floor(windowSize / 2)));
        const strat3Pred = new Set(
            [...stats].sort((a, b) => b.score - a.score).slice(0, 5).map(s => s.number)
        );

        // Stratégie 4 : Relations A -> B
        const followerScores = new Array(TOTAL_NUMBERS + 1).fill(0);
        for (const leader of prevDraw) {
            for (let dIdx = 0; dIdx < trainSlice.length - 1; dIdx++) {
                if (trainSlice[dIdx].numbers.includes(leader)) {
                    for (const nextNum of trainSlice[dIdx + 1].numbers) {
                        followerScores[nextNum]++;
                    }
                }
            }
        }
        const strat4Pred = new Set(
            Array.from({ length: TOTAL_NUMBERS }, (_, idx) => idx + 1)
                .sort((a, b) => followerScores[b] - followerScores[a])
                .slice(0, 5)
        );

        const preds = [strat1Pred, strat2Pred, strat3Pred, strat4Pred];
        for (let s = 0; s < preds.length; s++) {
            let matches = 0;
            preds[s].forEach(num => {
                if (actualDraw.has(num)) matches++;
            });
            distributions[s][matches] = (distributions[s][matches] || 0) + 1;
        }
    }

    return strategyNames.map((name, s) => {
        const dist = distributions[s];
        let sumMatches = 0;
        for (let m = 0; m <= 5; m++) {
            sumMatches += m * (dist[m] || 0);
        }
        const avg = totalEvaluated > 0 ? sumMatches / totalEvaluated : 0.0;
        const atLeast1 = totalEvaluated > 0 ? ((totalEvaluated - (dist[0] || 0)) / totalEvaluated) * 100.0 : 0.0;
        const atLeast2 = totalEvaluated > 0 ? (((dist[2] || 0) + (dist[3] || 0) + (dist[4] || 0) + (dist[5] || 0)) / totalEvaluated) * 100.0 : 0.0;
        const atLeast3 = totalEvaluated > 0 ? (((dist[3] || 0) + (dist[4] || 0) + (dist[5] || 0)) / totalEvaluated) * 100.0 : 0.0;

        return {
            strategyName: name,
            totalDrawsTested: totalEvaluated,
            matchDistribution: dist,
            avgMatchesPerDraw: avg,
            winRateAtLeast1: atLeast1,
            winRateAtLeast2: atLeast2,
            winRateAtLeast3: atLeast3
        };
    });
}

function generateGrids(draws, count = 5, strategy = "MOMENTUM") {
    const stats = computeNumberStats(draws, 30);
    const sortedByScore = [...stats].sort((a, b) => b.score - a.score);
    const sortedByFreq = [...stats].sort((a, b) => b.frequency - a.frequency);
    const sortedByGap = [...stats].sort((a, b) => b.currentGap - a.currentGap);

    const grids = [];
    for (let g = 0; g < count; g++) {
        let pool = [];
        if (strategy === "MOMENTUM") {
            // Mix top score et équilibre
            pool = sortedByScore.slice(0, 20).map(s => s.number);
        } else if (strategy === "FREQUENCE") {
            pool = sortedByFreq.slice(0, 20).map(s => s.number);
        } else if (strategy === "RETARD") {
            pool = sortedByGap.slice(0, 20).map(s => s.number);
        } else {
            // Équilibré : 2 chauds, 2 en retard, 1 relation
            const hot = sortedByScore.slice(0, 10).map(s => s.number);
            const cold = sortedByGap.slice(0, 10).map(s => s.number);
            pool = [...new Set([...hot, ...cold])];
        }

        // Tirage aléatoire sans remise de 5 boules parmi le pool
        const shuffled = [...pool].sort(() => Math.random() - 0.5);
        const grid = shuffled.slice(0, 5).sort((a, b) => a - b);
        grids.push({
            gridIndex: g + 1,
            numbers: grid,
            strategy
        });
    }
    return grids;
}

module.exports = {
    TOTAL_NUMBERS,
    NUMBERS_PER_DRAW,
    wilsonInterval,
    computeNumberStats,
    computeRelationsAB,
    computeTopCombinations,
    runWalkForwardBacktest,
    generateGrids
};
