package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Entity(
    tableName = "schedule",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true) val schedule_id: Int = 0,
    val user_id: String,
    val schedule_start: Long?,
    val schedule_end: Long?,
    val schedule_info: String?
)

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedule WHERE user_id = :userId")
    suspend fun getSchedulesByUser(userId: String): List<Schedule>
}
