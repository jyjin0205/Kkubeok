package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(
    tableName = "detected",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Detected(
    @PrimaryKey(autoGenerate = true) val detected_id: Int = 0,
    val user_id: String,
    val calendar_date: String,
    val action_name: String?,
    val start_time: Long?,
    val end_time: Long?,
    val direction: String?
)

@Dao
interface DetectedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detected: Detected)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detectedList: List<Detected>)

    @Query("SELECT * FROM detected WHERE user_id = :userId")
    suspend fun getDetectedByUser(userId: String): List<Detected>

    @Query("DELETE FROM detected")
    suspend fun deleteAll()

}
