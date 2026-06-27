package com.flatcode.littletasks.Unit

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.R
import com.flatcode.littletasks.databinding.DialogAboutAppBinding
import com.flatcode.littletasks.databinding.DialogCloseAppBinding
import com.flatcode.littletasks.databinding.DialogLogoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import jp.wasabeef.glide.transformations.BlurTransformation
import java.text.MessageFormat

object VOID {
    fun IntentClear(context: Context?, c: Class<*>?) {
        val intent = Intent(context, c)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(intent)
    }

    fun Intent1(context: Context?, c: Class<*>?) {
        val intent = Intent(context, c)
        context!!.startActivity(intent)
    }

    fun IntentExtra(context: Context?, c: Class<*>?, key: String?, value: String?) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        context!!.startActivity(intent)
    }

    fun IntentExtra2(
        context: Context?, c: Class<*>?, key: String?, value: String?,
        key2: String?, value2: String?
    ) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        intent.putExtra(key2, value2)
        context!!.startActivity(intent)
    }

    fun deleteItem(database: String?, context: Context?, id: String?, name: String) {
        if (context == null || database == null || id == null) return

        val dialog: AlertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Please wait")
            .setMessage("Deleting $name ...")
            .setCancelable(false)
            .create()

        dialog.show()

        FirebaseDatabase.getInstance().getReference(database)
            .child(id)
            .removeValue()
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(
                    context,
                    "The item has been deleted successfully...",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(
                    context,
                    e.localizedMessage ?: "Error deleting item",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun GlideImage(isUser: Boolean, context: Context?, Url: String?, Image: ImageView) {
        try {
            if (Url == DATA.BASIC) {
                if (isUser) {
                    Image.setImageResource(R.drawable.basic_user)
                } else {
                    Image.setImageResource(R.drawable.basic_book)
                }
            } else {
                Glide.with(context!!).load(Url).placeholder(R.color.image_profile).into(Image)
            }
        } catch (_: Exception) {
            Image.setImageResource(R.drawable.basic_book)
        }
    }

    fun GlideBlur(isUser: Boolean, context: Context?, Url: String, Image: ImageView, level: Int) {
        try {
            if (Url == DATA.BASIC) {
                if (isUser) {
                    Image.setImageResource(R.drawable.basic_user)
                } else {
                    Image.setImageResource(R.drawable.basic_book)
                }
            } else {
                Glide.with(context!!).load(Url).placeholder(R.color.image_profile)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(level))).into(Image)
            }
        } catch (_: Exception) {
            Image.setImageResource(R.drawable.basic_book)
        }
    }

    fun closeApp(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        val binding = DialogCloseAppBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lp = WindowManager.LayoutParams().apply {
                copyFrom(window.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            window.attributes = lp
        }

        binding.yes.setOnClickListener {
            activity.finish()
        }

        binding.no.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }

    fun dialogLogout(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        val binding = DialogLogoutBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lp = WindowManager.LayoutParams().apply {
                copyFrom(window.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            window.attributes = lp
        }

        binding.yes.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(activity, CLASS.AUTH).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)

            dialog.dismiss()
        }

        binding.no.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }

    fun shareApp(context: Context?) {
        context?.let { ctx ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "share app")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Download the app now from Google Play: https://google.com{ctx.packageName}"
                )
            }
            ctx.startActivity(Intent.createChooser(shareIntent, "Choose how to share"))
        }
    }

    fun rateApp(context: Context?) {
        context?.let { ctx ->
            val packageName = ctx.packageName
            val marketUri = Uri.parse("market://details?id=$packageName")
            val webUri = Uri.parse("https://google.com")

            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
            } catch (_: ActivityNotFoundException) {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }
    }

    fun dialogAboutApp(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        val binding = DialogAboutAppBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lp = WindowManager.LayoutParams().apply {
                copyFrom(window.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            window.attributes = lp
        }

        binding.website.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEBSITE))
            activity.startActivity(intent)
        }

        binding.facebook.setOnClickListener {
            val facebookUri = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.packageManager.getPackageInfo(
                        "com.facebook.katana",
                        android.content.pm.PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    activity.packageManager.getPackageInfo("com.facebook.katana", 0)
                }
                "fb://profile/${DATA.FB_ID}"
            } catch (_: Exception) {
                "https://facebook.com{DATA.FB_ID}"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUri))
            activity.startActivity(intent)
        }

        dialog.show()
    }

    fun isFavorite(add: ImageView, TaskId: String?, UserId: String?) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(UserId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(TaskId!!).exists()) {
                    add.setImageResource(R.drawable.ic_remove)
                    add.tag = "added"
                } else {
                    add.setImageResource(R.drawable.ic___add)
                    add.tag = "add"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun checkFavorite(image: ImageView, TaskId: String?) {
        if (image.tag == "add") {
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
                .child(DATA.FirebaseUserUid)
                .child(TaskId!!).setValue(true)
        } else {
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
                .child(DATA.FirebaseUserUid)
                .child(TaskId!!).removeValue()
        }
    }

    fun isPlan(add: ImageView, ObjectId: String?, planId: String?) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(DATA.PLANS)
                .child(planId!!).child(DATA.AUTO_TASKS)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(ObjectId!!).exists()) {
                    add.setImageResource(R.drawable.ic_heart_selected)
                    add.tag = "added"
                } else {
                    add.setImageResource(R.drawable.ic_heart_unselected)
                    add.tag = "add"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun checkPlan(image: ImageView, ObjectId: String?, planId: String?) {
        if (image.tag == "add") {
            FirebaseDatabase.getInstance().reference.child(DATA.PLANS).child(planId!!)
                .child(DATA.AUTO_TASKS)
                .child(ObjectId!!).setValue(true)
        } else {
            FirebaseDatabase.getInstance().reference.child(DATA.PLANS).child(planId!!)
                .child(DATA.AUTO_TASKS)
                .child(ObjectId!!).removeValue()
        }
    }

    fun moreObject(context: Context?, item: OBJECT?) {
        val id: String = item!!.id!!
        val name: String = item.name!!

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")
        //alert dialog
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { _: DialogInterface?, which: Int ->
                //handle dialog option click
                if (which == 0) {
                    //Edit clicked ,Open new activity to edit the book info
                    IntentExtra(context, CLASS.OBJECT_EDIT, DATA.ID, id)
                } else if (which == 1) {
                    //Delete Clicked
                    dialogOptionDelete(context, DATA.OBJECTS, DATA.EMPTY + id, DATA.EMPTY + name)
                }
            }.show()
    }

    fun moreTask(context: Context?, item: Task?) {
        if (context == null || item == null) return

        val id = item.id
        val name = item.name
        val category = item.category
        val start = item.start
        val end = item.end

        val options = when {
            start == 0L && end == 0L -> arrayOf("Edit", "Delete")
            start != 0L && end == 0L -> arrayOf("Edit", "Delete", "Start Again")
            start != 0L -> arrayOf("Edit", "Delete", "Start Again", "Not End")
            else -> arrayOf()
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Choose Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(context, CLASS.TASK_EDIT).apply {
                            putExtra(DATA.TASK_ID, id)
                            putExtra(DATA.CATEGORY_ID, category)
                        }
                        context.startActivity(intent)
                    }

                    1 -> {
                        dialogOptionDelete(
                            context,
                            DATA.TASKS,
                            "${DATA.EMPTY}$id",
                            "${DATA.EMPTY}$name"
                        )
                    }

                    2 -> EditTaskStatus(context, id, startStatus = true, endStatus = false)
                    3 -> EditTaskStatus(context, id, startStatus = false, endStatus = true)
                }
            }.show()
    }

    private fun EditTaskStatus(
        context: Context?,
        taskId: String?,
        startStatus: Boolean,
        endStatus: Boolean
    ) {
        if (context == null || taskId == null) return

        val hashMap = HashMap<String, Any>()
        if (startStatus) hashMap[DATA.START] = DATA.ZERO
        if (endStatus) hashMap[DATA.END] = DATA.ZERO

        if (hashMap.isEmpty()) return

        FirebaseDatabase.getInstance().getReference(DATA.TASKS)
            .child(taskId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                if (startStatus) {
                    Toast.makeText(context, "Task started again...", Toast.LENGTH_SHORT).show()
                }
                if (endStatus) {
                    Toast.makeText(context, "Task did not End...", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "${DATA.EMPTY}${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun moreCategory(context: Context?, item: Category?) {
        val id = item!!.id
        val name = item.name
        val plan = item.plan

        val options = arrayOf("Add Task", "Edit", "Delete")
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        IntentExtra2(
                            context, CLASS.TASK_ADD, DATA.CATEGORY_ID, id, DATA.PLAN_ID, plan
                        )
                    }

                    1 -> {
                        IntentExtra2(
                            context, CLASS.CATEGORY_EDIT, DATA.CATEGORY_ID, id, DATA.PLAN_ID, plan
                        )
                    }

                    2 -> {
                        dialogOptionDelete(
                            context, DATA.CATEGORIES, DATA.EMPTY + id, DATA.EMPTY + name
                        )
                    }
                }
            }.show()
    }

    fun morePlan(context: Context?, item: Plan?) {
        val id: String = item!!.id!!
        val name: String = item.name!!

        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { _: DialogInterface?, which: Int ->
                if (which == 0) {
                    IntentExtra(context, CLASS.PLAN_EDIT, DATA.ID, id)
                } else if (which == 1) {
                    dialogOptionDelete(
                        context, DATA.PLANS, DATA.EMPTY + id, DATA.EMPTY + name
                    )
                }
            }.show()
    }

    fun dialogOptionDelete(context: Context?, database: String?, id: String?, name: String) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        val title: TextView = dialog.findViewById(R.id.title)
        title.setText(R.string.do_you_want_to_delete_the)
        val Title: String = title.text.toString()
        when (database) {
            DATA.CATEGORIES -> {
                title.text = MessageFormat.format("{0} Category?", Title)
            }

            DATA.OBJECTS -> {
                title.text = MessageFormat.format("{0} Object?", Title)
            }

            DATA.TASKS -> {
                title.text = MessageFormat.format("{0} Task?", Title)
            }

            DATA.PLANS -> {
                title.text = MessageFormat.format("{0} Plan?", Title)
            }
        }
        dialog.findViewById<View>(R.id.yes).setOnClickListener {
            dialog.dismiss()
            deleteItem(database, context, id, name)
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun CropImageSquare(activity: Activity?) {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE)
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(activity!!)
    }

    fun CropImageWide(activity: Activity?) {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE)
            .setAspectRatio(2, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(activity!!)
    }

    fun isTask(context: Context?, image: ImageView, taskId: String?) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference().child(DATA.TASKS).child(taskId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val item: Task = dataSnapshot.getValue(Task::class.java)!!
                if (item.timestamp != DATA.ZERO.toLong()) {
                    image.setImageResource(R.drawable.ic_star_unselected)
                    image.setOnClickListener {
                        FirebaseDatabase.getInstance().getReference()
                            .child(DATA.TASKS).child(taskId).child(DATA.START)
                            .setValue(System.currentTimeMillis())
                    }
                }
                if (item.start != DATA.ZERO.toLong()) {
                    image.setImageResource(R.drawable.ic_star_half)
                    image.setOnClickListener {
                        FirebaseDatabase.getInstance().getReference()
                            .child(DATA.TASKS).child(taskId).child(DATA.END)
                            .setValue(System.currentTimeMillis())
                        val points = item.points
                        val AVPoints = item.aVPoints
                        if (AVPoints != points) FirebaseDatabase.getInstance().getReference()
                            .child(DATA.TASKS).child(taskId).child(DATA.AVAILABLE_POINTS)
                            .setValue(points)
                    }
                }
                if (item.end != DATA.ZERO.toLong()) {
                    image.setImageResource(R.drawable.ic_star_selected)
                    image.setOnClickListener {
                        Toast.makeText(context, "Task Completed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun levelPoint(AVPoints: Int, initialPoint: Int): Int {
        var mutablePoint = initialPoint
        val half = mutablePoint / 2

        val thresholds = IntArray(21)
        thresholds[1] = mutablePoint * 5
        for (i in 2..20) {
            thresholds[i] = loop(thresholds[i - 1], half, i + 1)
        }

        return when {
            AVPoints <= thresholds[1] -> {
                AVPoints / mutablePoint
            }

            AVPoints <= thresholds[20] -> {
                var stepIndex = 1
                while (stepIndex < 19 && AVPoints > thresholds[stepIndex + 1]) {
                    stepIndex++
                }

                val baseLevel = 5 * stepIndex
                val remainderPoints = AVPoints - thresholds[stepIndex]
                mutablePoint += half * (stepIndex - 1)

                baseLevel + (remainderPoints / mutablePoint)
            }

            else -> 100
            //MAX 5750 - 100
        }
    }

    private fun loop(S: Int, half: Int, number: Int): Int {
        return S + half * number * half
    }

    fun getFileExtension(uri: Uri?, context: Context): String {
        val cR: ContentResolver = context.contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri!!))!!
    }
}