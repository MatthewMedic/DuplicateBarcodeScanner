package com.darksphere.duplicatescanner.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BarcodeRepository @Inject constructor(
    private val barcodeDao: BarcodeDao
) {
    fun getAllLists(): Flow<List<BarcodeList>> = barcodeDao.getAllLists()

    fun getBarcodesForList(listId: Long): Flow<List<Barcode>> = barcodeDao.getBarcodesForList(listId)

    suspend fun createList(name: String): Long {
        return barcodeDao.insertList(BarcodeList(name = name))
    }

    suspend fun deleteList(listId: Long) {
        barcodeDao.deleteBarcodesForList(listId)
        barcodeDao.deleteList(listId)
    }

    suspend fun addBarcode(listId: Long, value: String): Result<Long> {
        return if (barcodeDao.isDuplicate(listId, value)) {
            Result.failure(Exception("Duplicate barcode"))
        } else {
            Result.success(barcodeDao.insertBarcode(Barcode(listId = listId, value = value)))
        }
    }
} 