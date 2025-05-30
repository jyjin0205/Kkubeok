package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Index
import androidx.room.OnConflictStrategy

@Entity(
    tableName = "sleep_log",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sleep_date"], unique = true)]

)
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val sleep_log_id: Int = 0,
    val user_id: String,
    val sleep_date: String,
    val sleep_start: Long?,
    val sleep_end: Long?
)

@Dao
interface SleepLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLog)

    @Query("SELECT * FROM sleep_log WHERE user_id = :userId")
    suspend fun getSleepLogsByUser(userId: String): List<SleepLog>

    @Query("SELECT * FROM sleep_log WHERE user_id = :userId AND sleep_date = :sleepDate")
    suspend fun getSleepLogsByUserAndDate(userId: String, sleepDate: String): SleepLog?

    @Query("DELETE FROM sleep_log")
    suspend fun deleteAll()
}
