package com.familypedia.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.familypedia.R
import com.google.android.material.snackbar.Snackbar


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    //imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.showView() {
    visibility = View.VISIBLE
}

fun View.hideView() {
    visibility = View.GONE
}

fun View.invisibleView() {
    visibility = View.INVISIBLE
}


fun View.showStringSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.view.setBackgroundColor(snackbar.context.resources.getColor(R.color.color_blue))
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun View.showStringSnackbarError(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.view.setBackgroundColor(snackbar.context.resources.getColor(R.color.color_error))
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}


fun View.showSnackbar(message: Int) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.view.setBackgroundColor(Color.parseColor("#6200EE"))
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.isInternetAvailable(): Boolean {
    var result = false
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    connectivityManager?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        } else {

        }
    }
    return result
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun View.familyPediaClickListener(listener: FamilyPediaClickListener) {
    val safeClickListener = SafeClickListener {
        listener.onViewClick(it)
    }
    setOnClickListener(safeClickListener)
}

interface FamilyPediaClickListener {
    fun onViewClick(view: View)


}

fun AppCompatTextView.setTextOnTextView(text: String?, defaultText: String) {
    if (text != null && !text.isEmpty() && !text.equals("null", ignoreCase = true)) {
        this.text = text
    } else {
        this.text = defaultText
    }
}

fun AppCompatEditText.setTextOnEditText(text: String?, defaultText: String) {
    if (text != null && !text.isEmpty() && !text.equals("null", ignoreCase = true)) {
        this.setText(text)
    } else {
        this.setText(defaultText)
    }
}

fun ImageView.loadImagesWithGlide(url: String?, profileImage: Boolean) {
    try {

            if (profileImage) {
                Glide.with(this)
                    .load(url)
                    //.centerCrop()
                    .error(R.mipmap.ic_profile_pic)
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_profile_pic)
                    .into(this)
            } else {
                Glide.with(this)
                    .load(url)
                    .error(R.mipmap.ic_profile_pic)
                    .placeholder(R.mipmap.ic_profile_pic)
                    /*.error(R.drawable.ic_item_placeholder)
                    .placeholder(R.drawable.ic_item_placeholder)*/
                    .into(this)
            }
    } catch (e: Exception) {
        e.printStackTrace()

    }
}

fun ImageView.loadPlaceholderImagesWithGlide(url: String?) {
    try {
        if (!(this.context as Activity).isFinishing)
            Glide.with(this)
                .load(url)
                .error(R.drawable.ic_item_placeholder)
                .placeholder(R.drawable.ic_item_placeholder)
                .into(this)
    } catch (e: Exception) {

    }
}

