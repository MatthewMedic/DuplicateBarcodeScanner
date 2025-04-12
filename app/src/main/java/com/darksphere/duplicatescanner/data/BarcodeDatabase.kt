package com.darksphere.duplicatescanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "barcode_lists")
data class BarcodeList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "barcodes",
    foreignKeys = [
        ForeignKey(
            entity = BarcodeList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class Barcode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface BarcodeDao {
    @Query("SELECT * FROM barcode_lists")
    fun getAllLists(): Flow<List<BarcodeList>>

    @Query("SELECT * FROM barcodes WHERE listId = :listId")
    fun getBarcodesForList(listId: Long): Flow<List<Barcode>>

    @Query("SELECT EXISTS(SELECT 1 FROM barcodes WHERE listId = :listId AND value = :value)")
    suspend fun isDuplicate(listId: Long, value: String): Boolean

    @Insert
    suspend fun insertList(list: BarcodeList): Long

    @Insert
    suspend fun insertBarcode(barcode: Barcode): Long

    @Query("DELETE FROM barcode_lists WHERE id = :listId")
    suspend fun deleteList(listId: Long)

    @Query("DELETE FROM barcodes WHERE listId = :listId")
    suspend fun deleteBarcodesForList(listId: Long)
}

@Database(entities = [BarcodeList::class, Barcode::class], version = 1)
abstract class BarcodeDatabase : RoomDatabase() {
    abstract fun barcodeDao(): BarcodeDao

    companion object {
        const val DATABASE_NAME = "barcode_database"
    }
} 