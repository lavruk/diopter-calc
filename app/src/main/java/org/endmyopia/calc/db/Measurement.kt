package org.endmyopia.calc.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author denisk
 * @since 2019-07-09.
 */
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: Long,
    val distance: Double,
    val calibrationCoeff: Double = 0.0
)
