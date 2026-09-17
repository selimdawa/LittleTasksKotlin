package com.flatcode.littletasks.utils

import com.google.firebase.auth.FirebaseAuth

object DATA {

    //Database
    const val USERS = "Users"
    const val TASKS = "Tasks"
    const val AUTO_TASKS = "AutoTasks"
    const val OBJECTS = "Objects"
    const val CATEGORIES = "Categories"
    const val PLANS = "Plans"
    const val CHOOSE_PLAN = "choosePlan"
    const val POINTS = "points"
    const val AVAILABLE_POINTS = "AVPoints"
    const val RANK = "rank"
    const val START = "start"
    const val END = "end"
    const val TOOLS = "Tools"
    const val PRIVACY_POLICY = "privacyPolicy"
    const val VERSION = "version"
    const val EMAIL = "email"
    const val BASIC = "basic"
    const val USER_NAME = "username"
    const val PROFILE_IMAGE = "profileImage"
    const val TIMESTAMP = "timestamp"
    const val ID = "id"
    const val IMAGE = "image"
    const val PUBLISHER = "publisher"
    const val CATEGORY = "category"
    const val TITLE = "title"
    const val FAVORITES = "Favorites"
    const val NAME = "name"
    const val PLAN = "plan"

    //Others
    const val DOT = "."
    const val EMPTY = ""
    const val CURRENT_VERSION = 1
    const val SPLASH_TIME = 2000
    const val MIX_SQUARE = 500
    const val ZERO = 0
    var searchStatus = false

    //Shared
    const val PROFILE_ID = "profileId"
    const val CATEGORY_ID = "categoryId"
    const val TASK_ID = "taskId"
    const val TASK_TYPE = "taskType"
    const val TASKS_ALL = "tasksAll"
    const val TASKS_UN_STARTED = "tasksUnStarted"
    const val TASKS_STARTED = "tasksStarted"
    const val TASKS_COMPLETED = "tasksCompleted"
    const val PLAN_ID = "planId"
    const val COLOR_OPTION = "color_option"
    const val NEW_PLAN = "newPlan"

    //Other
    val AUTH: FirebaseAuth get() = FirebaseAuth.getInstance()
    val FIREBASE_USER get() = AUTH.currentUser
    val firebaseUserUid get() = FIREBASE_USER?.uid ?: ""
    const val WEBSITE = ""
    const val FB_ID = ""
}