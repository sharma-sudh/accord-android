package com.sudh.accord.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromSyncState(state: SyncState): String = state.name

    @TypeConverter
    fun toSyncState(value: String): SyncState = SyncState.valueOf(value)
}