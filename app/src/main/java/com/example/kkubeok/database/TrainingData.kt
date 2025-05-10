package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Entity(
    tableName = "training_data",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrainingData(
    @PrimaryKey(autoGenerate = true) val file_id: Int = 0,
    val user_id: String,
    val file_path: String
)

@Dao
interface TrainingDataDao {
    @Insert
    suspend fun insert(data: TrainingData)

    @Query("SELECT * FROM training_data WHERE user_id = :userId")
    suspend fun getTrainingDataByUser(userId: String): List<TrainingData>

    @Query("SELECT file_path FROM training_data WHERE user_id = :userId")
    suspend fun getTraingDataFilePathByUser(userId: String): List<String>
}
