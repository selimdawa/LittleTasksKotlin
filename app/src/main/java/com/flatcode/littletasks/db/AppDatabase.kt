package com.flatcode.littletasks.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.FavoriteEntity
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.model.Setting
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.model.User

@Database(
    entities = [Category::class, Task::class, Plan::class, User::class, Setting::class, TaskItem::class, FavoriteEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun tasksDao(): TasksDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun userDao(): UserDao
    abstract fun settingDao(): SettingDao
    abstract fun taskItemDao(): TaskItemDao
}