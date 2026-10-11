package ru.hdlabs.electrosmeta.data.local

import androidx.room.TypeConverter
import ru.hdlabs.electrosmeta.data.local.EstimateStatus
import ru.hdlabs.electrosmeta.data.local.EstimateType

class Converters {
    @TypeConverter
    fun fromEstimateType(value: EstimateType): String = value.name

    @TypeConverter
    fun toEstimateType(value: String): EstimateType = EstimateType.valueOf(value)

    @TypeConverter
    fun fromEstimateStatus(value: EstimateStatus): String = value.name

    @TypeConverter
    fun toEstimateStatus(value: String): EstimateStatus = EstimateStatus.valueOf(value)
}
