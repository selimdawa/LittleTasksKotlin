package com.flatcode.littletasks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flatcode.littletasks.data.local.dao.*
import com.flatcode.littletasks.data.model.*

@Database(
    entities = [
        Category::class,
        Task::class,
        Plan::class,
        User::class,
        Setting::class,
        TaskItem::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun planDao(): PlanDao
    abstract fun userDao(): UserDao
    abstract fun settingDao(): SettingDao
    abstract fun taskItemDao(): TaskItemDao
}
