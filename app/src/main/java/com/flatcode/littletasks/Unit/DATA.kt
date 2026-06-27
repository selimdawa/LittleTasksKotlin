package com.flatcode.littletasks.Unit

import com.google.firebase.auth.FirebaseAuth

object DATA {

    //Database
    val USERS = "Users"
    val TASKS = "Tasks"
    val AUTO_TASKS = "AutoTasks"
    val OBJECTS = "Objects"
    val CATEGORIES = "Categories"
    val PLANS = "Plans"
    val CHOOSE_PLAN = "choosePlan"
    val POINTS = "points"
    val AVAILABLE_POINTS = "AVPoints"
    val RANK = "rank"
    val START = "start"
    val END = "end"
    val TOOLS = "Tools"
    val PRIVACY_POLICY = "privacyPolicy"
    val VERSION = "version"
    val EMAIL = "email"
    val BASIC = "basic"
    val USER_NAME = "username"
    val PROFILE_IMAGE = "profileImage"
    val TIMESTAMP = "timestamp"
    val ID = "id"
    val IMAGE = "image"
    val PUBLISHER = "publisher"
    val CATEGORY = "category"
    val TITLE = "title"
    val FAVORITES = "Favorites"
    val NAME = "name"
    val PLAN = "plan"

    //Others
    val DOT = "."
    val EMPTY = ""
    const val CURRENT_VERSION = 1
    const val SPLASH_TIME = 2000
    const val MIX_SQUARE = 500
    const val ZERO = 0
    var searchStatus = false

    //Shared
    val PROFILE_ID = "profileId"
    val CATEGORY_ID = "categoryId"
    val TASK_ID = "taskId"
    val TASK_TYPE = "taskType"
    val TASKS_ALL = "tasksAll"
    val TASKS_UN_STARTED = "tasksUnStarted"
    val TASKS_STARTED = "tasksStarted"
    val TASKS_COMPLETED = "tasksCompleted"
    val PLAN_ID = "planId"
    val COLOR_OPTION = "color_option"
    val NEW_PLAN = "newPlan"

    //Other
    val AUTH: FirebaseAuth get() = FirebaseAuth.getInstance()
    val FIREBASE_USER get() = AUTH.currentUser
    val FirebaseUserUid get() = FIREBASE_USER?.uid ?: ""
    const val WEBSITE = ""
    const val FB_ID = ""
}