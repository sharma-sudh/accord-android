package com.sudh.accord.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AccordDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AccordDatabase? = null

        // Adds the version-vector conflict resolution columns to `tasks`.
        // Existing rows default to version = 0 and no conflict snapshot,
        // which is correct: anything already synced predates the server
        // having a version to compare against, and the first PENDING_UPDATE
        // push after this migration just establishes a real baseVersion.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN conflictServerSnapshot TEXT")
            }
        }

        fun getInstance(context: Context): AccordDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AccordDatabase::class.java,
                    "accord.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}