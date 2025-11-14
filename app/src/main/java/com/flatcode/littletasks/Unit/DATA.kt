package com.flatcode.littletasks.Unit

import com.google.firebase.auth.FirebaseAuth

object DATA {
    //Database
    var USERS = "Users"
    var TASKS = "Tasks"
    var AUTO_TASKS = "AutoTasks"
    var OBJECTS = "Objects"
    var CATEGORIES = "Categories"
    var PLANS = "Plans"
    var CHOOSE_PLAN = "choosePlan"
    var POINTS = "points"
    var AVAILABLE_POINTS = "AVPoints"
    var RANK = "rank"
    var START = "start"
    var END = "end"
    var TOOLS = "Tools"
    var PRIVACY_POLICY = "privacyPolicy"
    var VERSION = "version"
    var EMAIL = "email"
    var BASIC = "basic"
    var USER_NAME = "username"
    var PROFILE_IMAGE = "profileImage"
    var TIMESTAMP = "timestamp"
    var ID = "id"
    var IMAGE = "image"
    var PUBLISHER = "publisher"
    var CATEGORY = "category"
    var TITLE = "title"
    var FAVORITES = "Favorites"
    var NAME = "name"
    var PLAN = "plan"

    //Others
    var DOT = "."
    var EMPTY = ""
    var CURRENT_VERSION = 1
    var SPLASH_TIME = 2000
    var MIX_SQUARE = 500
    var ZERO = 0
    var searchStatus = false

    //Shared
    var PROFILE_ID = "profileId"
    var CATEGORY_ID = "categoryId"
    var TASK_ID = "taskId"
    var TASK_TYPE = "taskType"
    var TASKS_ALL = "tasksAll"
    var TASKS_UN_STARTED = "tasksUnStarted"
    var TASKS_STARTED = "tasksStarted"
    var TASKS_COMPLETED = "tasksCompleted"
    var PLAN_ID = "planId"
    var COLOR_OPTION = "color_option"
    var NEW_PLAN = "newPlan"

    //Other
    val AUTH = FirebaseAuth.getInstance()
    val FIREBASE_USER = AUTH.currentUser
    val FirebaseUserUid = FIREBASE_USER!!.uid
    const val WEBSITE = ""
    const val FB_ID = ""
}