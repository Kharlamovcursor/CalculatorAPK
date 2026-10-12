package ru.hdlabs.electrosmeta.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estimate_template_lines",
    foreignKeys = [
        ForeignKey(
            entity = EstimateTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId")],
)
data class EstimateTemplateLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val categoryKey: Long,
    val catalogServiceId: Long?,
    val name: String,
    val unit: String,
    val unitPriceKopecks: Long,
    val quantity: Double,
    val sortOrder: Int,
)
