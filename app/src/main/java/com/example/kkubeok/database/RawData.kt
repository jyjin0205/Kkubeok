package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Entity(
    tableName = "raw_data",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RawData(
    @PrimaryKey(autoGenerate = true) val file_id: Int = 0,
    val user_id: String,
    val file_path: String
)

@Dao
interface RawDataDao {
    @Insert
    suspend fun insert(data: RawData)

    @Query("SELECT * FROM raw_data WHERE user_id = :userId")
    suspend fun getRawDataByUser(userId: String): List<RawData>

    @Query("SELECT file_path FROM raw_data WHERE user_id = :userId")
    suspend fun getRawDataFilePathByUser(userId: String): List<String>
}
