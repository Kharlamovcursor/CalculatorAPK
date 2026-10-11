package ru.hdlabs.electrosmeta.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.hdlabs.electrosmeta.data.local.EstimateStatus
import ru.hdlabs.electrosmeta.data.local.EstimateType

@Entity(tableName = "estimates")
data class EstimateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: EstimateType,
    val status: EstimateStatus,
    val clientName: String?,
    val clientAddress: String?,
    val discountPercent: Double,
    val discountAmountKopecks: Long,
    val extraFeeKopecks: Long,
    val complexityMultiplier: Double,
    val taxPercent: Double,
    val roundToWholeRubles: Boolean,
    val comment: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
