package com.sudh.accord.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TaskEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AccordDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AccordDatabase? = null

        fun getInstance(context: Context): AccordDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AccordDatabase::class.java,
                    "accord.db"
                ).build().also { INSTANCE = it }
            }
    }
}