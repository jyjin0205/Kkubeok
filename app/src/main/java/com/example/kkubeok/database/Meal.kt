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
    tableName = "meal",
    primaryKeys = ["user_id", "meal_type", "meal_date"],
    indices = [Index(value = ["user_id", "meal_type", "meal_date"], unique = true)],
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
    val user_id: String,
    val meal_type: String,
    val meal_date: String,
    val meal_time: Long?
)

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal)

    @Query("SELECT * FROM meal WHERE user_id = :userId")
    suspend fun getMealsByUser(userId: String): List<Meal>

    @Query("SELECT * FROM meal WHERE user_id = :userId AND meal_date = :mealDate")
    suspend fun getMealsByUserAndDate(userId: String, mealDate: String): List<Meal>

    @Query("DELETE FROM meal")
    suspend fun deleteAll()
}
