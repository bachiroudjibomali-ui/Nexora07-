package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.Draw

@Entity(tableName = "custom_draws")
data class CustomDrawEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val num1: Int,
    val num2: Int,
    val num3: Int,
    val num4: Int,
    val num5: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDraw(baseOffsetId: Int): Draw {
        return Draw(
            id = baseOffsetId + id.toInt(),
            date = date,
            numbers = listOf(num1, num2, num3, num4, num5),
            isCustom = true
        )
    }

    companion object {
        fun fromDraw(draw: Draw): CustomDrawEntity {
            val sorted = draw.sortedNumbers
            return CustomDrawEntity(
                date = draw.date,
                num1 = sorted[0],
                num2 = sorted[1],
                num3 = sorted[2],
                num4 = sorted[3],
                num5 = sorted[4]
            )
        }
    }
}
