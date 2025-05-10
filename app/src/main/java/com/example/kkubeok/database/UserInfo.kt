package com.example.kkubeok.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Database
import androidx.room.RoomDatabase

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey val user_id: String
)

@Dao
interface UserInfoDao {
    @Insert
    suspend fun insert(user: UserInfo)

    @Query("SELECT * FROM user_info WHERE user_id = :id")
    suspend fun getUser(id: String): UserInfo?
}