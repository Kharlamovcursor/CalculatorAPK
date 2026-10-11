package ru.hdlabs.electrosmeta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.EstimateDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity

@Database(
    entities = [
        CategoryEntity::class,
        ServiceEntity::class,
        EstimateEntity::class,
        EstimateLineEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServiceDao
    abstract fun estimateDao(): EstimateDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electrosmeta.db",
                ).build().also { instance = it }
            }
        }
    }
}
