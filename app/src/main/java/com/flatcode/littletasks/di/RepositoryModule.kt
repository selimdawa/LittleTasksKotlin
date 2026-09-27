@file:Suppress("unused")

package com.flatcode.littletasks.di

import com.flatcode.littletasks.repository.CategoryRepository
import com.flatcode.littletasks.repository.CategoryRepositoryImpl
import com.flatcode.littletasks.repository.ObjectRepository
import com.flatcode.littletasks.repository.ObjectRepositoryImpl
import com.flatcode.littletasks.repository.PlanRepository
import com.flatcode.littletasks.repository.PlanRepositoryImpl
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.repository.TaskRepositoryImpl
import com.flatcode.littletasks.repository.UserRepository
import com.flatcode.littletasks.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPlanRepository(
        planRepositoryImpl: PlanRepositoryImpl
    ): PlanRepository

    @Binds
    @Singleton
    abstract fun bindObjectRepository(
        objectRepositoryImpl: ObjectRepositoryImpl
    ): ObjectRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}