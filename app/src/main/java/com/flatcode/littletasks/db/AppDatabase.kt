package com.flatcode.littletasks.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.model.Setting
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.model.User

@Database(
    entities = [Category::class, Task::class, Plan::class, User::class, Setting::class, TaskItem::class],
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