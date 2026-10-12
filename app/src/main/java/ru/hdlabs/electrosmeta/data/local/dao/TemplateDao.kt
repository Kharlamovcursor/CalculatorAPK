package ru.hdlabs.electrosmeta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateCategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity

data class TemplateWithContent(
    val template: EstimateTemplateEntity,
    val categories: List<EstimateTemplateCategoryEntity>,
    val lines: List<EstimateTemplateLineEntity>,
)

@Dao
interface TemplateDao {
    @Query("SELECT * FROM estimate_templates ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<EstimateTemplateEntity>>

    @Query("SELECT * FROM estimate_templates WHERE id = :id")
    suspend fun getById(id: Long): EstimateTemplateEntity?

    @Query("SELECT * FROM estimate_template_categories WHERE templateId = :templateId ORDER BY sortOrder ASC")
    suspend fun getCategories(templateId: Long): List<EstimateTemplateCategoryEntity>

    @Query("SELECT * FROM estimate_template_lines WHERE templateId = :templateId ORDER BY sortOrder ASC")
    suspend fun getLines(templateId: Long): List<EstimateTemplateLineEntity>

    @Transaction
    suspend fun getWithContent(id: Long): TemplateWithContent? {
        val template = getById(id) ?: return null
        return TemplateWithContent(
            template = template,
            categories = getCategories(id),
            lines = getLines(id),
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(entity: EstimateTemplateEntity): Long

    @Update
    suspend fun updateTemplate(entity: EstimateTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(entity: EstimateTemplateCategoryEntity): Long

    @Update
    suspend fun updateCategory(entity: EstimateTemplateCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(entity: EstimateTemplateLineEntity): Long

    @Update
    suspend fun updateLine(entity: EstimateTemplateLineEntity)

    @Query("DELETE FROM estimate_template_lines WHERE id = :lineId")
    suspend fun deleteLine(lineId: Long)

    @Query("DELETE FROM estimate_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM estimate_templates")
    suspend fun nextTemplateSortOrder(): Int

    @Query("SELECT * FROM estimate_templates")
    suspend fun getAllTemplatesOnce(): List<EstimateTemplateEntity>

    @Query("SELECT * FROM estimate_template_categories")
    suspend fun getAllTemplateCategoriesOnce(): List<EstimateTemplateCategoryEntity>

    @Query("SELECT * FROM estimate_template_lines")
    suspend fun getAllTemplateLinesOnce(): List<EstimateTemplateLineEntity>

    @Query("DELETE FROM estimate_template_lines")
    suspend fun deleteAllLines()

    @Query("DELETE FROM estimate_template_categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM estimate_templates")
    suspend fun deleteAllTemplates()
}
