package com.example.data

import com.example.data.db.CustomDrawDao
import com.example.data.db.CustomDrawEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ImportResult(
    val successCount: Int,
    val errorCount: Int,
    val errors: List<String>
)

class DrawRepository(
    private val customDrawDao: CustomDrawDao
) {
    /**
     * Flux réactif combinant les tirages officiels (261 tirages de base)
     * et les ajouts personnalisés de l'utilisateur stockés en base locale Room.
     */
    val allDrawsFlow: Flow<List<Draw>> = customDrawDao.getAllFlow().map { customEntities ->
        val baseDraws = HistoricalData.DRAWS
        val customDraws = customEntities.mapIndexed { index, entity ->
            entity.toDraw(baseDraws.size)
        }
        baseDraws + customDraws
    }

    /**
     * Flux uniquement pour "Mes ajouts" (tirages personnalisés saisis ou importés)
     */
    val customDrawsFlow: Flow<List<CustomDrawEntity>> = customDrawDao.getAllFlow()

    suspend fun addSingleDraw(date: String, numbers: List<Int>): Result<Long> {
        val validation = validateNumbers(numbers)
        if (validation != null) {
            return Result.failure(IllegalArgumentException(validation))
        }

        val formattedDate = date.trim().ifEmpty {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        val entity = CustomDrawEntity(
            date = formattedDate,
            num1 = numbers[0],
            num2 = numbers[1],
            num3 = numbers[2],
            num4 = numbers[3],
            num5 = numbers[4]
        )
        val id = customDrawDao.insert(entity)
        return Result.success(id)
    }

    /**
     * Importe en masse des tirages au format texte (une ligne par tirage).
     * Exemples supportés :
     *  - "46-84-67-60-48 (01/01/26)"
     *  - "46 84 67 60 48 01/01/2026"
     *  - "12, 34, 56, 78, 90"
     */
    suspend fun importMassDraws(text: String): ImportResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val entitiesToInsert = mutableListOf<CustomDrawEntity>()
        val errors = mutableListOf<String>()
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            try {
                // Recherche d'une date entre parenthèses ou en fin de ligne
                var extractedDate = todayStr
                var numbersPart = line

                val dateMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(line)
                if (dateMatcher.find()) {
                    extractedDate = dateMatcher.group(1).trim()
                    numbersPart = line.replace(dateMatcher.group(0), " ")
                } else {
                    val dateRegex = Pattern.compile("(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4})").matcher(line)
                    if (dateRegex.find()) {
                        extractedDate = dateRegex.group(1).trim()
                        numbersPart = line.replace(extractedDate, " ")
                    }
                }

                // Normaliser le format de date si AA au lieu de AAAA
                if (extractedDate.matches(Regex("\\d{1,2}/\\d{1,2}/\\d{2}"))) {
                    val parts = extractedDate.split("/")
                    extractedDate = "${parts[0]}/${parts[1]}/20${parts[2]}"
                }

                // Extraction des entiers avec séparateurs libres
                val numbers = Regex("\\d+").findAll(numbersPart)
                    .map { it.value.toInt() }
                    .toList()

                if (numbers.size < 5) {
                    errors.add("Ligne $lineNum : 5 numéros requis (trouvé ${numbers.size}) : « $line »")
                    continue
                }

                val selected5 = numbers.take(5)
                val validation = validateNumbers(selected5)
                if (validation != null) {
                    errors.add("Ligne $lineNum : $validation dans « $line »")
                    continue
                }

                val sorted = selected5.sorted()
                entitiesToInsert.add(
                    CustomDrawEntity(
                        date = extractedDate,
                        num1 = sorted[0],
                        num2 = sorted[1],
                        num3 = sorted[2],
                        num4 = sorted[3],
                        num5 = sorted[4]
                    )
                )
            } catch (e: Exception) {
                errors.add("Ligne $lineNum : Erreur de formatage (${e.message})")
            }
        }

        if (entitiesToInsert.isNotEmpty()) {
            customDrawDao.insertAll(entitiesToInsert)
        }

        return ImportResult(
            successCount = entitiesToInsert.size,
            errorCount = errors.size,
            errors = errors
        )
    }

    suspend fun deleteCustomDraw(id: Long) {
        customDrawDao.deleteById(id)
    }

    suspend fun clearCustomDraws() {
        customDrawDao.clearAll()
    }

    private fun validateNumbers(numbers: List<Int>): String? {
        if (numbers.size != 5) {
            return "Un tirage doit comporter exactement 5 numéros"
        }
        if (numbers.toSet().size != 5) {
            return "Les 5 numéros doivent être distincts"
        }
        val outOfBounds = numbers.filter { it !in 1..90 }
        if (outOfBounds.isNotEmpty()) {
            return "Les numéros doivent être compris entre 1 et 90 (invalide : $outOfBounds)"
        }
        return null
    }
}
