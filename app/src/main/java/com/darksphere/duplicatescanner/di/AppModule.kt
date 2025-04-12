package com.darksphere.duplicatescanner.di

import android.content.Context
import androidx.room.Room
import com.darksphere.duplicatescanner.data.BarcodeDatabase
import com.darksphere.duplicatescanner.data.BarcodeDao
import com.darksphere.duplicatescanner.data.BarcodeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BarcodeDatabase {
        return Room.databaseBuilder(
            context,
            BarcodeDatabase::class.java,
            BarcodeDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideBarcodeDao(database: BarcodeDatabase): BarcodeDao {
        return database.barcodeDao()
    }

    @Provides
    @Singleton
    fun provideBarcodeRepository(dao: BarcodeDao): BarcodeRepository {
        return BarcodeRepository(dao)
    }
} 