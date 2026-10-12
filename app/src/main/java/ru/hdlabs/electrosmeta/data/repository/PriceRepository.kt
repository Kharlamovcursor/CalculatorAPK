package ru.hdlabs.electrosmeta.data.repository

import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.seed.DefaultPriceSeed

class PriceRepository(
    private val categoryDao: CategoryDao,
    private val serviceDao: ServiceDao,
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeActive()

    fun observeAllCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeServices(categoryId: Long): Flow<List<ServiceEntity>> =
        serviceDao.observeByCategory(categoryId)

    fun observeAllServices(): Flow<List<ServiceEntity>> = serviceDao.observeActive()

    fun observeFavorites(): Flow<List<ServiceEntity>> = serviceDao.observeFavorites()

    suspend fun searchServices(query: String): List<ServiceEntity> = serviceDao.search(query)

    suspend fun upsertCategory(entity: CategoryEntity): Long {
        return if (entity.id == 0L) categoryDao.insert(entity) else {
            categoryDao.update(entity)
            entity.id
        }
    }

    suspend fun upsertService(entity: ServiceEntity): Long {
        return if (entity.id == 0L) serviceDao.insert(entity) else {
            serviceDao.update(entity)
            entity.id
        }
    }

    suspend fun archiveCategory(id: Long) = categoryDao.archive(id)

    suspend fun restoreCategory(id: Long) = categoryDao.restore(id)

    suspend fun archiveService(id: Long) = serviceDao.archive(id)

    suspend fun restoreService(id: Long) = serviceDao.restore(id)

    suspend fun toggleFavorite(id: Long, favorite: Boolean) = serviceDao.setFavorite(id, favorite)

    suspend fun getService(id: Long) = serviceDao.getById(id)

    suspend fun reorderCategories(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            val category = categoryDao.getById(id) ?: return@forEachIndexed
            if (category.sortOrder != index) {
                categoryDao.update(category.copy(sortOrder = index))
            }
        }
    }

    suspend fun reorderServices(categoryId: Long, orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            val service = serviceDao.getById(id) ?: return@forEachIndexed
            if (service.categoryId != categoryId) return@forEachIndexed
            if (service.sortOrder != index) {
                serviceDao.update(service.copy(sortOrder = index))
            }
        }
    }

    suspend fun resetToDefaults() {
        categoryDao.getActiveOnce().forEach { categoryDao.archive(it.id) }
        serviceDao.getActiveOnce().forEach { serviceDao.archive(it.id) }
        DefaultPriceSeed.seed(categoryDao, serviceDao)
    }
}
