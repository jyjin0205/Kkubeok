package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Entity(
    tableName = "meal",
    foreignKeys = [
        ForeignKey(
            entity = UserInfo::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Meal(
    @PrimaryKey(autoGenerate = true) val meal_id: Int = 0,
    val user_id: String,
    val meal_type: String?,
    val meal_time: Long?
)

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: Meal)

    @Query("SELECT * FROM meal WHERE user_id = :userId")
    suspend fun getMealsByUser(userId: String): List<Meal>
}
