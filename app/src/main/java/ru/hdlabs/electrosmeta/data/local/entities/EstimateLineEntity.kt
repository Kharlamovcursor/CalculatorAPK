package ru.hdlabs.electrosmeta.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estimate_lines",
    foreignKeys = [
        ForeignKey(
            entity = EstimateEntity::class,
            parentColumns = ["id"],
            childColumns = ["estimateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("estimateId")],
)
data class EstimateLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estimateId: Long,
    val catalogServiceId: Long?,
    val name: String,
    val unit: String,
    val unitPriceKopecks: Long,
    val quantity: Double,
    val note: String?,
    val sortOrder: Int,
)
