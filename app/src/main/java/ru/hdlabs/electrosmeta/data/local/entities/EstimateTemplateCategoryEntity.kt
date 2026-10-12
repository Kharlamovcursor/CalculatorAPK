package ru.hdlabs.electrosmeta.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estimate_template_categories",
    foreignKeys = [
        ForeignKey(
            entity = EstimateTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId"), Index(value = ["templateId", "categoryKey"], unique = true)],
)
data class EstimateTemplateCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val categoryKey: Long,
    val sortOrder: Int,
)
