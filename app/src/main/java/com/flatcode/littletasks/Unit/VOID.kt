package com.flatcode.littletasks.Unit

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.flatcode.littletasks.Model.Category
import com.flatcode.littletasks.Model.OBJECT
import com.flatcode.littletasks.Model.Plan
import com.flatcode.littletasks.Model.Task
import com.flatcode.littletasks.R
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
        val dialog = ProgressDialog(context)
        dialog.setTitle("Please wait")
        dialog.setMessage("Deleting $name ...")
        dialog.show()
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference(database!!)
        reference.child(id!!).removeValue().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(
                context, "The item has been deleted successfully...", Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Image.setImageResource(R.drawable.basic_book)
        }
    }

    fun closeApp(context: Context?, a: Activity?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_close_app)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.yes).setOnClickListener { a!!.finish() }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.cancel() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun dialogLogout(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.yes).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            IntentClear(context, CLASS.AUTH)
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.cancel() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun shareApp(context: Context?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            " Download the app now from Google Play " + " https://play.google.com/store/apps/details?id=" + context!!.packageName
        )
        context.startActivity(Intent.createChooser(shareIntent, "Choose how to share"))
    }

    fun rateApp(context: Context?) {
        val uri = Uri.parse("market://details?id=" + context!!.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
                )
            )
        }
    }

    fun dialogAboutApp(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_about_app)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.website).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                context.startActivity(websiteIntent)
            }

            val websiteIntent: Intent
                get() = Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEBSITE))
        })
        dialog.findViewById<View>(R.id.facebook).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                context.startActivity(openFacebookIntent)
            }

            val openFacebookIntent: Intent
                get() = try {
                    context.packageManager.getPackageInfo("com.facebook.katana", 0)
                    Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + DATA.FB_ID))
                } catch (e: Exception) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/" + DATA.FB_ID)
                    )
                }
        })
        dialog.show()
        dialog.window!!.attributes = lp
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
            .setItems(options) { dialog: DialogInterface?, which: Int ->
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
        val id = item!!.id
        val name = item.name
        val category = item.category
        val start = item.start
        val end = item.end
        val one = arrayOf<String?>("Edit", "Delete")
        val two = arrayOf<String?>("Edit", "Delete", "Start Again")
        val three = arrayOf<String?>("Edit", "Delete", "Start Again", "Not End")
        var options = arrayOfNulls<String>(0)
        //options to show in dialog
        if (start == 0L && end == 0L) {
            options = one
        } else if (start != 0L && end == 0L) {
            options = two
        } else if (start != 0L) {
            options = three
        }

        //alert dialog
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                //handle dialog option click
                if (which == 0) {
                    //Edit clicked ,Open new activity to edit the book info
                    IntentExtra2(
                        context, CLASS.TASK_EDIT, DATA.TASK_ID, id, DATA.CATEGORY_ID, category
                    )
                } else if (which == 1) {
                    //Delete Clicked
                    dialogOptionDelete(
                        context, DATA.TASKS, DATA.EMPTY + id, DATA.EMPTY + name
                    )
                } else if (which == 2) {
                    //Delete Clicked
                    EditTaskStatus(context, id, true, false)
                } else if (which == 3) {
                    //Delete Clicked
                    EditTaskStatus(context, id, false, true)
                }
            }.show()
    }

    private fun EditTaskStatus(
        context: Context?, taskId: String?, startStatus: Boolean, endStatus: Boolean
    ) {
        val hashMap = HashMap<String?, Any>()
        if (startStatus) hashMap[DATA.START] = DATA.ZERO
        if (endStatus) hashMap[DATA.END] = DATA.ZERO
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference(DATA.TASKS)
        ref.child(taskId!!).updateChildren(hashMap).addOnSuccessListener {
            if (startStatus) Toast.makeText(
                context, "Task started again...", Toast.LENGTH_SHORT
            ).show()
            if (endStatus) Toast.makeText(context, "Task did not End...", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(
                context,
                DATA.EMPTY + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun moreCategory(context: Context?, item: Category?) {
        val id = item!!.id
        val name = item.name
        val plan = item.plan

        //options to show in dialog
        val options = arrayOf("Add Task", "Edit", "Delete")
        //alert dialog
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                //handle dialog option click
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

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")
        //alert dialog
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                //handle dialog option click
                if (which == 0) {
                    //Edit clicked ,Open new activity to edit the book info
                    IntentExtra(context, CLASS.PLAN_EDIT, DATA.ID, id)
                } else if (which == 1) {
                    //Delete Clicked
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

    fun levelPoint(AVPoints: Int, point: Int): Int {
        var point = point
        val half = point / 2
        var level: Int
        val S1 = point * 5
        val S2 = loop(S1, half, 3)
        val S3 = loop(S2, half, 4)
        val S4 = loop(S3, half, 5)
        val S5 = loop(S4, half, 6)
        val S6 = loop(S5, half, 7)
        val S7 = loop(S6, half, 8)
        val S8 = loop(S7, half, 9)
        val S9 = loop(S8, half, 10)
        val S10 = loop(S9, half, 11)
        val S11 = loop(S10, half, 12)
        val S12 = loop(S11, half, 13)
        val S13 = loop(S12, half, 14)
        val S14 = loop(S13, half, 15)
        val S15 = loop(S14, half, 16)
        val S16 = loop(S15, half, 17)
        val S17 = loop(S16, half, 18)
        val S18 = loop(S17, half, 19)
        val S19 = loop(S18, half, 20)
        val S20 = loop(S19, half, 21)
        // 0 TO 5
        if (AVPoints <= S1) {
            level = AVPoints / point
        } else if (AVPoints <= S20) {
            val a: Int
            // 5 TO 10
            if (AVPoints <= S2) {
                a = AVPoints - S1
                level = 5
                //10 = 10+5;
                point = point + half
            } else if (AVPoints <= S3) {
                a = AVPoints - S2
                level = 10
                point = point + half * 2
            } else if (AVPoints <= S4) {
                a = AVPoints - S3
                level = 15
                point = point + half * 3
            } else if (AVPoints <= S5) {
                a = AVPoints - S4
                level = 20
                point = point + half * 4
            } else if (AVPoints <= S6) {
                a = AVPoints - S5
                level = 25
                point = point + half * 5
            } else if (AVPoints <= S7) {
                a = AVPoints - S6
                level = 30
                point = point + half * 6
            } else if (AVPoints <= S8) {
                a = AVPoints - S7
                level = 35
                point = point + half * 7
            } else if (AVPoints <= S9) {
                a = AVPoints - S8
                level = 40
                point = point + half * 8
            } else if (AVPoints <= S10) {
                a = AVPoints - S9
                level = 45
                point = point + half * 9
            } else if (AVPoints <= S11) {
                a = AVPoints - S10
                level = 50
                point = point + half * 10
            } else if (AVPoints <= S12) {
                a = AVPoints - S11
                level = 55
                point = point + half * 11
            } else if (AVPoints <= S13) {
                a = AVPoints - S12
                level = 60
                point = point + half * 12
            } else if (AVPoints <= S14) {
                a = AVPoints - S13
                level = 65
                point = point + half * 13
            } else if (AVPoints <= S15) {
                a = AVPoints - S14
                level = 70
                point = point + half * 14
            } else if (AVPoints <= S16) {
                a = AVPoints - S15
                level = 75
                point = point + half * 15
            } else if (AVPoints <= S17) {
                a = AVPoints - S16
                level = 80
                point = point + half * 16
            } else if (AVPoints <= S18) {
                a = AVPoints - S17
                level = 85
                point = point + half * 17
            } else if (AVPoints <= S19) {
                a = AVPoints - S18
                level = 90
                point = point + half * 18
            } else {
                a = AVPoints - S19
                level = 95
                point = point + half * 19
            }
            level = level + a / point
        } else {
            level = 100
        }
        return level
        //MAX 5750 - 100
    }

    private fun loop(S: Int, half: Int, number: Int): Int {
        val SA: Int
        SA = S + half * number * half
        return SA
    }

    fun Intro(context: Context?, background: ImageView, backWhite: ImageView, backDark: ImageView) {
        val sharedPreferences: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context!!)
        if (sharedPreferences.getString("color_option", "ONE") == "ONE") {
            background.setImageResource(R.drawable.background_day)
            backWhite.visibility = View.VISIBLE
            backDark.visibility = View.GONE
        } else if (sharedPreferences.getString("color_option", "NIGHT_ONE") == "NIGHT_ONE") {
            background.setImageResource(R.drawable.background_night)
            backWhite.visibility = View.GONE
            backDark.visibility = View.VISIBLE
        }
    }

    fun Logo(context: Context?, background: ImageView) {
        val sharedPreferences: SharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context!!)
        if (sharedPreferences.getString("color_option", "ONE") == "ONE") {
            background.setImageResource(R.drawable.logo)
        } else if (sharedPreferences.getString("color_option", "NIGHT_ONE") == "NIGHT_ONE") {
            background.setImageResource(R.drawable.logo_night)
        }
    }

    fun getFileExtension(uri: Uri?, context: Context): String {
        val cR: ContentResolver = context.contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri!!))!!
    }
}