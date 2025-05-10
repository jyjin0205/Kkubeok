package com.example.kkubeok.database
import androidx.room.RoomDatabase
import androidx.room.Database

@Database(
    entities = [UserInfo::class, TrainingData::class, Detected::class, Meal::class, SleepLog::class, Schedule::class, RawData::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserInfoDao
    abstract fun trainingDataDao(): TrainingDataDao
    abstract fun detectedDao(): DetectedDao
    abstract fun mealDao(): MealDao
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun rawDataDao(): RawDataDao
}
