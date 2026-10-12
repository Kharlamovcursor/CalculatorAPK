package ru.hdlabs.electrosmeta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.EstimateDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.dao.TemplateDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateCategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity

@Database(
    entities = [
        CategoryEntity::class,
        ServiceEntity::class,
        EstimateEntity::class,
        EstimateLineEntity::class,
        EstimateTemplateEntity::class,
        EstimateTemplateCategoryEntity::class,
        EstimateTemplateLineEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServiceDao
    abstract fun estimateDao(): EstimateDao
    abstract fun templateDao(): TemplateDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE estimates ADD COLUMN workTitle TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS estimate_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        type TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS estimate_template_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateId INTEGER NOT NULL,
                        categoryKey INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(templateId) REFERENCES estimate_templates(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_estimate_template_categories_templateId ON estimate_template_categories(templateId)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_estimate_template_categories_templateId_categoryKey ON estimate_template_categories(templateId, categoryKey)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS estimate_template_lines (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateId INTEGER NOT NULL,
                        categoryKey INTEGER NOT NULL,
                        catalogServiceId INTEGER,
                        name TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        unitPriceKopecks INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(templateId) REFERENCES estimate_templates(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_estimate_template_lines_templateId ON estimate_template_lines(templateId)")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electrosmeta.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
        }
    }
}
