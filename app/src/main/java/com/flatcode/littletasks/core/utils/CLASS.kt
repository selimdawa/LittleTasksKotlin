package com.flatcode.littletasks.core.utils

import com.flatcode.littletasks.ui.auth.AuthActivity
import com.flatcode.littletasks.ui.auth.ForgetPasswordActivity
import com.flatcode.littletasks.ui.auth.LoginActivity
import com.flatcode.littletasks.ui.auth.RegisterActivity
import com.flatcode.littletasks.ui.category.CategoriesActivity
import com.flatcode.littletasks.ui.category.CategoryAddActivity
import com.flatcode.littletasks.ui.category.CategoryEditActivity
import com.flatcode.littletasks.ui.category.CategoryTasksActivity
import com.flatcode.littletasks.ui.main.MainActivity
import com.flatcode.littletasks.ui.main.SplashActivity
import com.flatcode.littletasks.ui.objects.ObjectAddActivity
import com.flatcode.littletasks.ui.objects.ObjectEditActivity
import com.flatcode.littletasks.ui.objects.ObjectsActivity
import com.flatcode.littletasks.ui.objects.ObjectsPlanActivity
import com.flatcode.littletasks.ui.objects.ObjectsToPlanActivity
import com.flatcode.littletasks.ui.plan.PlanAddActivity
import com.flatcode.littletasks.ui.plan.PlanEditActivity
import com.flatcode.littletasks.ui.plan.PlansActivity
import com.flatcode.littletasks.ui.profile.FavoritesActivity
import com.flatcode.littletasks.ui.profile.ProfileActivity
import com.flatcode.littletasks.ui.profile.ProfileEditActivity
import com.flatcode.littletasks.ui.settings.PrivacyPolicyActivity
import com.flatcode.littletasks.ui.task.TaskAddActivity
import com.flatcode.littletasks.ui.task.TaskEditActivity

object CLASS {
    val MAIN: Class<*> = MainActivity::class.java
    val SPLASH: Class<*> = SplashActivity::class.java
    val AUTH: Class<*> = AuthActivity::class.java
    val LOGIN: Class<*> = LoginActivity::class.java
    val REGISTER: Class<*> = RegisterActivity::class.java
    val CATEGORY_ADD: Class<*> = CategoryAddActivity::class.java
    val OBJECTS_PLAN: Class<*> = ObjectsPlanActivity::class.java
    val PLANS: Class<*> = PlansActivity::class.java
    val CATEGORY_EDIT: Class<*> = CategoryEditActivity::class.java
    val CATEGORY_TASKS: Class<*> = CategoryTasksActivity::class.java
    val PLAN_ADD: Class<*> = PlanAddActivity::class.java
    val PLAN_EDIT: Class<*> = PlanEditActivity::class.java
    val OBJECT_ADD: Class<*> = ObjectAddActivity::class.java
    val OBJECT_EDIT: Class<*> = ObjectEditActivity::class.java
    val OBJECTS: Class<*> = ObjectsActivity::class.java
    val TASK_ADD: Class<*> = TaskAddActivity::class.java
    val TASK_EDIT: Class<*> = TaskEditActivity::class.java
    val PROFILE: Class<*> = ProfileActivity::class.java
    val PROFILE_EDIT: Class<*> = ProfileEditActivity::class.java
    val FAVORITES: Class<*> = FavoritesActivity::class.java
    val FORGET_PASSWORD: Class<*> = ForgetPasswordActivity::class.java
    val CATEGORIES: Class<*> = CategoriesActivity::class.java
    val PRIVACY_POLICY: Class<*> = PrivacyPolicyActivity::class.java
    val OBJECT_TO_PLAN: Class<*> = ObjectsToPlanActivity::class.java
}