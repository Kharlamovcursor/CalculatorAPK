package ru.hdlabs.electrosmeta.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.hdlabs.electrosmeta.data.local.EstimateType

@Entity(tableName = "estimate_templates")
data class EstimateTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int,
    val type: EstimateType,
)
