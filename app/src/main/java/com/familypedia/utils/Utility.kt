package com.familypedia.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.os.SystemClock
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.*
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.familypedia.FamilyPediaApplication
import com.familypedia.R
import com.familypedia.network.PostData
import com.familypedia.network.User
import com.familypedia.utils.Constants.FROM_AUTH
import com.familypedia.utils.Constants.LANGUAGE_CODE_ENGLISH
import com.familypedia.utils.Constants.LANGUAGE_CODE_SPANISH
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REMOVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_ACCEPTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_REJECTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_CREATE_BIOGRAPHY
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_DELETE_BIOGRAPHY
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_REJECTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_NEW_POST_CREATED
import com.familypedia.utils.listeners.BioListeners
import com.familypedia.utils.listeners.PostsListener
import com.familypedia.utils.listeners.UserListener
import com.familypedia.viewmodels.CharacterViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object Utility {
    private var mLastClickTime: Long = 0
    var MOBILE_NUMBER = "^\\d{9,14}$"

    private fun avoidDoubleClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

    }

    var BLOCK_SPACE_ =
        InputFilter { source, start, end, dest, dstart, dend ->
            var keepOriginal = true
            val sb = StringBuilder(end - start)
            var type2 = 0
            for (index in start until end) {
                val type = Character.getType(source[index])

                /*if (index>0) {
                    type2 = Character.getType(source[index - 1])
                }*/

                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    /* if (type2 ==Character.SPACE_SEPARATOR.toInt()){
                         return@InputFilter "a"
                     }*/
                    return@InputFilter ""
                }


                val c = source[index]
                if (isCharAllowedSpace(c)) sb.append(c) else keepOriginal = false
            }
            if (keepOriginal) null else {
                if (source is Spanned) {
                    val sp = SpannableString(sb)
                    TextUtils.copySpansFrom(source, start, sb.length, null, sp, 0)
                    sp
                } else {
                    sb
                }
            }
        }

    private fun isCharAllowedSpace(c: Char): Boolean {
        return Character.isLetterOrDigit(c) || isSpecialCharEmail(c)
    }

    private fun isSpecialCharEmail(c: Char): Boolean {
        val specialChar = "@#&.-_"
        return specialChar.contains(c.toString())
    }


    var BLOCK_SPACE =
        InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {

                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }


    fun isLogin(context: Context): Boolean {
        return !getPreferencesString(context, Constants.USER_ID).equals("")
    }

//    fun isValidEmail(target: CharSequence): Boolean {
//        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
//    }
    fun isValidEmail(email: String): Boolean {
        val regex = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return regex.matches(email) &&  !TextUtils.isEmpty(email)
    }



    fun isValidPhone(phone: String): Boolean {
        return when {
            Pattern.compile(MOBILE_NUMBER).matcher(phone).matches() -> {
                println("Mobile Valid")
                true
            }
            else -> {
                println("Mobile InValid")
                false
            }
        }
    }

    fun isValidPassword(password: String?): Boolean {
/*        val pattern: Pattern
        val PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&.])[A-Za-z\\d@\$!%*?&.]{8,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()*/
        return password?.length!! >= 8
    }

    fun jsonFromModel(model: Any): JSONObject {
        if (model is String) {
            return JSONObject(Gson().toJson(JSONObject(model)))
        }
        return JSONObject(Gson().toJson(model))
    }

    fun fromJson(jsonString: String?, type: Type): Any? {
        return Gson().fromJson(jsonString, type)
    }

    fun saveRecentSearchesCharacter(context: Context, searchKey: String) {
        val prevList = getRecentSearchesCharacter(context)
        val list = arrayListOf<String?>()
        if (prevList == null) {
            list.add(searchKey)
            saveRecentCharacter(context, list)
        } else {
            prevList.add(searchKey)
            saveRecentCharacter(context, prevList)
        }
    }

    fun saveRecentCharacter(context: Context, list: ArrayList<String?>?) {
        try {
            if (list?.size!! > 5) {
                for (index in list.indices) {
                    if (index < list.size - 5) {
                        list.removeAt(index)
                    }
                }
            }
        } catch (e: Exception) {

        }
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString("recent_searches", json)
        editor.apply()
    }

    fun getRecentSearchesCharacter(context: Context): ArrayList<String?>? {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json: String? = prefs.getString("recent_searches", null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }

    //user

    fun saveRecentSearchesUser(context: Context, searchKey: String) {
        val prevList = getRecentSearchesUser(context)
        val list = arrayListOf<String?>()
        if (prevList == null) {
            list.add(searchKey)
            saveRecentUser(context, list)
        } else {
            prevList.add(searchKey)
            saveRecentUser(context, prevList)
        }
    }

    fun saveRecentUser(context: Context, list: ArrayList<String?>?) {
        try {
            if (list?.size!! > 5) {
                for (index in list.indices) {
                    if (index < list.size - 5) {
                        list.removeAt(index)
                    }
                }
            }
        } catch (e: Exception) {

        }
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString("recent_searches_user", json)
        editor.apply()
    }

    fun getRecentSearchesUser(context: Context): ArrayList<String?>? {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json: String? = prefs.getString("recent_searches_user", null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun savePreferencesBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getPreferencesString(context: Context, key: String): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(key, "").toString()
    }


    fun getPreferencesBoolean(context: Context, key: String): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean(key, false)
    }

    fun clearAllPreferences(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        /*val preferences: SharedPreferences =
            context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.commit()*/

    }

    fun saveUserDetailsInPreferences(context: Context, user: User, from: String) {
        savePreferencesString(context, Constants.USER_NAME, user.name ?: "")
        savePreferencesString(context, Constants.COUNTRY_CODE, user.country_code ?: "")
        savePreferencesString(context, Constants.CITY, user.city ?: "")
        savePreferencesString(context, Constants.COUNTRY, user.country ?: "")
        savePreferencesString(context, Constants.PHONE_NUMBER, user.phone_number ?: "")
        savePreferencesString(context, Constants.DATE_OF_BIRTH, user.date_of_birth ?: "")
        savePreferencesString(context, Constants.GENDER, user.gender ?: "")
        savePreferencesString(context, Constants.EMAIL, user.email ?: "")
        savePreferencesString(context, Constants.PROFILE_PIC, user.profile_pic ?: "")
        savePreferencesBoolean(context, Constants.IS_FIRST_LOGIN, user.is_first_login ?: false)
        savePreferencesBoolean(context, Constants.IS_EMAIL_VERIFIED, user.email_verify ?: false)
        savePreferencesBoolean(context, Constants.IS_PHONE_VERFIED, user.isPhoneNumberVerify ?: false)

        if (from == FROM_AUTH) {
            if (user._id != null) {
                savePreferencesString(context, Constants.USER_ID, user._id!!)
            }
            savePreferencesString(context, Constants.AUTH_TOKEN, user.auth_token ?: "")
            savePreferencesBoolean(
                context,
                Constants.NOTIFICATION_STATUS,
                user.notification_status ?: false
            )
            savePreferencesBoolean(
                context,
                Constants.IS_ACCOUNT_ACTIVE,
                user.is_account_active ?: false
            )
            savePreferencesBoolean(context, Constants.IS_VERIFIED, user.is_verified ?: false)
        }

    }

    fun getDeviceUniqueId(): String = UUID.randomUUID().toString()


    fun isHasPermission2(context: Context, vararg permissions: String): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.M)
            permissions.all { singlePermission ->
                context.checkSelfPermission(singlePermission) == PackageManager.PERMISSION_GRANTED
            }
        else true
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isHasPermission(context: Context, vararg permissions: String): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            permissions.all { singlePermission ->
                context.checkSelfPermission(singlePermission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }


    fun askPermission(
        activity: Activity,
        vararg permissions: String, /*@IntRange(from = 0)*/
        requestCode: Int
    ) =
        ActivityCompat.requestPermissions(activity, permissions, requestCode)


    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        val result: String?
        val cursor = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
    fun capitalize(str: String): String {
        return str.trim().split("\\s+".toRegex())
            .map { it.capitalize() }.joinToString(" ")
    }

    fun formateDate(date: String?): String? {
        var sdf: SimpleDateFormat
        sdf =
            SimpleDateFormat("dd/MM/yyyy") //format of the date which you send as parameter(if the date is like 08-Aug-2016 then use dd-MMM-yyyy)
        var s = ""
        try {
            val dt: Date = sdf.parse(date)
            sdf = SimpleDateFormat("dd MMMM yyyy")
            s = sdf.format(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return s
    }

    fun formateDate2(date: String?): String? {
        var sdf: SimpleDateFormat
        sdf =
            SimpleDateFormat("dd/MM/yyyy") //format of the date which you send as parameter(if the date is like 08-Aug-2016 then use dd-MMM-yyyy)
        var s = ""
        try {
            val dt: Date = sdf.parse(date)
            sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            s = sdf.format(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return s
    }

    fun formateDate3(date: String?): String? {
        var sdf: SimpleDateFormat
        sdf =
            SimpleDateFormat("dd MMMM yyyy") //format of the date which you send as parameter(if the date is like 08-Aug-2016 then use dd-MMM-yyyy)
        var s = ""
        try {
            val dt: Date = sdf.parse(date)
            sdf = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            s = sdf.format(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return s
    }

    fun unAuthorizedInactiveUser(context: Context, msg: String) {
        BlockedByAdminDialog(
            context,
            context.getString(R.string.inActive),
            msg
        ).show()

    }

    fun setBolderView(view: TextView, fulltext: String, subtext: String) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE)
        val str = view.text as Spannable
        val boldSpan = StyleSpan(Typeface.BOLD)

        str.setSpan(
            boldSpan, // Span to add
            fulltext.indexOf(subtext), // Start of the span (inclusive)
            fulltext.indexOf(subtext) + subtext.length, // End of the span (exclusive)
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Do not extend the span when text add later
        )
    }



    fun scaleDown(
        realImage: Bitmap, maxImageSize: Float,
        filter: Boolean
    ): Bitmap {
        val ratio = Math.min(
            maxImageSize / realImage.width,
            maxImageSize / realImage.height
        )
        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)
/*
        val options: Options = BitmapFactory.Options()
        options.inScaled = false
        val source = BitmapFactory.decodeResource(a.getResources(), path, options)
*/

        return Bitmap.createScaledBitmap(
            realImage, width,
            height, filter
        )
    }

    fun BITMAP_RESIZER(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val ratioX = newWidth / bitmap.width.toFloat()
        val ratioY = newHeight / bitmap.height.toFloat()
        val middleX = newWidth / 2.0f
        val middleY = newHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bitmap,
            middleX - bitmap.width / 2,
            middleY - bitmap.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        return scaledBitmap
    }


    @Throws(IOException::class)
    fun rotateRequiredImage(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {

        val input = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface
        if (SDK_INT > 23)
            ei = ExifInterface(input!!)
        else
            ei = ExifInterface(selectedImage.path!!)

        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
            else -> return img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    fun saveTempBitmap(context: Context, mBitmap: Bitmap): String? {

        val outputDir = context.cacheDir

        var file: File? = null
        try {
            file = File.createTempFile("temp_post_img", ".jpg", outputDir)
            //outputFile.getAbsolutePath();
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val f3 = File(Environment.getExternalStorageDirectory().toString() + "/familyPedia/")
        if (!f3.exists()) {
            f3.mkdirs()
        }
        val outStream: OutputStream?
        //File file = new File(Environment.getExternalStorageDirectory() + "/inpaint/"+"seconds"+".png");
        try {
            outStream = FileOutputStream(file!!)
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()

            //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        //getPath( Uri.parse(file.getAbsolutePath()), context);

        return file.absolutePath//getPath( Uri.parse(file.getAbsolutePath()), context);
    }


    fun getCurrentFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            savePreferencesString(FamilyPediaApplication.instance, Constants.FCM_TOKEN, token)
            Log.d("Token", "getCurrentFCMToken: $token")
        })

    }

    fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(textView.context, color),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }
    }

    //2022-02-24T07:32:44.928Z
    fun getTimeAgo(dateString: String): String {
        try {

            var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            Log.d("Time----", TimeZone.getDefault().toString())
            val pasTime = dateFormat.parse(dateString)
            dateFormat.timeZone = TimeZone.getDefault();

            //var returnDateFormat:SimpleDateFormat?=null

            val msDiff = Calendar.getInstance().timeInMillis - pasTime.time;
            val calDiff = Calendar.getInstance()
            calDiff.timeInMillis = msDiff
            val msg =
                "${calDiff.get(Calendar.YEAR) - 1970} year, ${calDiff.get(Calendar.MONTH)} months, ${
                    calDiff.get(Calendar.DAY_OF_MONTH)
                } days"
            val years = calDiff.get(Calendar.YEAR) - 1970
            val months = calDiff.get(Calendar.MONTH)
            val days = calDiff.get(Calendar.DAY_OF_MONTH)

            if (years == 0) {
                if (months == 0) {
                    if (days <= 1) {
                        if (DateUtils.isToday(pasTime.time)) {
                            dateFormat = SimpleDateFormat("hh:mm a")
                        } else {
                            dateFormat = SimpleDateFormat("'Yesterday'")
                        }

                    } else {
                        if (days < 7) {
                            dateFormat = SimpleDateFormat("EEEE")
                        } else {
                            dateFormat = SimpleDateFormat("dd MMM")
                        }
                    }
                } else {
                    dateFormat = SimpleDateFormat("dd MMM")
                }
            } else {
                dateFormat = SimpleDateFormat("MM-dd-yyyy")
            }

            return dateFormat?.format(pasTime).toString()
        } catch (e: Exception) {

        }
        return ""
    }

    fun getNotificationType(serverNotificationType: String, context: Context): String {
        return when (serverNotificationType) {
            NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED -> context.getString(R.string.friend_request)
            NOTIFICATION_TYPE_FRIEND_REQUEST_REJECTED -> context.getString(R.string.friend_request_rejected)
            NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED -> context.getString(R.string.friend_request_accepted)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED -> context.getString(R.string.biography_request)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_ACCEPTED -> context.getString(R.string.biography_request_accepted)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_REJECTED -> context.getString(R.string.biography_request_rejected)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REMOVED -> context.getString(R.string.biography_permission_removed)
            NOTIFICATION_TYPE_NEW_POST_CREATED -> context.getString(R.string.new_post_created)
            NOTIFICATION_TYPE_CREATE_BIOGRAPHY -> context.getString(R.string.create_new_biography)
            NOTIFICATION_TYPE_DELETE_BIOGRAPHY -> context.getString(R.string.delete_biography)

            Constants.NOTIFICATION_TYPE_POST_REMOVED -> context.getString(R.string.post_removed)
            Constants.NOTIFICATION_TYPE_POST_EDITED -> context.getString(R.string.post_edited)

            else -> ""
        }
    }

    fun mailTo(context: Context) {
        try {

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this

            intent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf<String>(context.getString(R.string.support_email))
            )
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }

        } catch (e: java.lang.Exception) {
            val emailIntent = Intent(Intent.ACTION_SEND);
            emailIntent.type = "plain/text";
            context?.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            e.printStackTrace()
        }
    }

    fun changeStatusBarColor(context: AppCompatActivity, color: Int) {
        val window = context.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(context, color)
        }
    }

    fun getImageContentUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }

    private const val GIF_PATTERN = "(.+?)\\.gif$"
    fun checkGif(path: String): Boolean {
        return path.matches(GIF_PATTERN.toRegex())
    }

    fun setLocale(activity: Context) {
      //  val languageCode = LANGUAGE_CODE_SPANISH
        val languageCode = LANGUAGE_CODE_ENGLISH
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        savePreferencesString(activity, "locale", languageCode)
    }

    fun getCurrentLocale(activity: Context): String {
        val locale = getPreferencesString(activity, "locale")
        return if (!locale.isNullOrEmpty())
            locale
        else
            LANGUAGE_CODE_SPANISH

    }


    fun showDialogOK(
        context: Context,
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(false)
        dialog.setMessage(message)
            .setPositiveButton(context.getString(R.string.ok), okListener)
            .setNegativeButton(context.getString(R.string.cancel), okListener)
            .create()
            .show()
    }

    fun showPopup(
        v: View,
        context: Context,
        listerner: PostsListener,
        postData: PostData,
        userId: String
    ) {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = layoutInflater.inflate(R.layout.custom_menu_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvEdit = popupView.findViewById<TextView>(R.id.tvEdit)
        val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)
        val tvReport = popupView.findViewById<TextView>(R.id.tvReport)

        if (postData.postedBy?._id == userId) {
            tvEdit.showView()
            tvDelete.showView()
            tvReport.hideView()
        } else if ( postData.character?.userId == userId) {
            tvEdit.hideView()
            tvDelete.showView()
            tvReport.hideView()
        }else{
            tvEdit.hideView()
            tvDelete.hideView()
            tvReport.showView()
        }

        tvDelete.setOnClickListener {
            listerner.onDeleteClick(postData)
            popupWindow.dismiss()
        }
        tvEdit.setOnClickListener {
            listerner.onEditClick(postData)
            popupWindow.dismiss()
        }
        tvReport.setOnClickListener {
            Log.e("onReportClick", "reportPostAPI: ${postData._id}")
            listerner.onReportClick(postData)
            popupWindow.dismiss()
        }

        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener {}
        popupWindow.elevation = 50f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))


        val contentView = popupWindow.contentView
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        contentView.measure(
            makeDropDownMeasureSpec(popupWindow.width),
            makeDropDownMeasureSpec(popupWindow.height)
        )

        val offsetX = -popupWindow.contentView.measuredWidth + 50
        val offsetY: Int = -v.height + v.height
        popupWindow.showAsDropDown(v, offsetX, offsetY, Gravity.BOTTOM)
    }

    fun showPopupProfile(
        v: View,
        context: Context,
        listerner: UserListener,
        userId: String,
        isBlocked: Boolean
    ) {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = layoutInflater.inflate(R.layout.custom_menu_profile, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvAction = popupView.findViewById<TextView>(R.id.tvAction)
        val tvReport = popupView.findViewById<TextView>(R.id.tvReport)

        if (!isBlocked) {
            tvAction.text = context.getString(R.string.block)
            tvAction.setOnClickListener {
                listerner.blockUser(userId)
                popupWindow.dismiss()
            }
        } else {
            tvAction.text = context.getString(R.string.unblock)
            tvAction.setOnClickListener {
                listerner.unBlockUser(userId)
                popupWindow.dismiss()
            }
        }
        tvReport.setOnClickListener {
            listerner.reportUser(userId)
            popupWindow.dismiss()
        }

        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener({
        })
        popupWindow.elevation = 50f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))


        val contentView = popupWindow.contentView
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        contentView.measure(
            makeDropDownMeasureSpec(popupWindow.width),
            makeDropDownMeasureSpec(popupWindow.height)
        )

        val offsetX = -popupWindow.contentView.measuredWidth + 50
        val offsetY: Int = -v.getHeight() + v.getHeight()
        popupWindow.showAsDropDown(v, offsetX, offsetY, Gravity.BOTTOM)
    }

    fun showPopupBio(v:View,
                     context: Context,
                     characterId:String,
                     listerner: BioListeners,isMyBio:Boolean)
    {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = layoutInflater.inflate(R.layout.custom_menu_bio, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)
        val tvShare = popupView.findViewById<TextView>(R.id.tvShare)
        val tvCancel = popupView.findViewById<TextView>(R.id.tvCancel)

        if(isMyBio){
            tvDelete.showView()
            tvShare.showView()
            tvCancel.hideView()
        }else{
            tvShare.showView()
            tvDelete.hideView()
            tvCancel.hideView()
        }

        tvDelete.setOnClickListener {
            listerner.deleteBio(characterId)
            popupWindow.dismiss()
        }
        tvShare.setOnClickListener {
            listerner.shareBio(characterId)
            popupWindow.dismiss()
        }



        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener({
        })
        popupWindow.elevation = 50f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))


        val contentView = popupWindow.contentView
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        // need to measure first, because in this time PopupWindow is nit pop, width is 0.
        contentView.measure(
            makeDropDownMeasureSpec(popupWindow.width),
            makeDropDownMeasureSpec(popupWindow.height)
        )

        val offsetX = -popupWindow.contentView.measuredWidth + 50
        val offsetY: Int = -v.getHeight() + v.getHeight()
        popupWindow.showAsDropDown(v, offsetX, offsetY, Gravity.BOTTOM)

    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode: Int
        mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            View.MeasureSpec.UNSPECIFIED
        } else {
            View.MeasureSpec.EXACTLY
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }
}