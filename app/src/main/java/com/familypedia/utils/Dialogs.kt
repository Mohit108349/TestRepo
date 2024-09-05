package com.familypedia.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.familypedia.R
import com.familypedia.databinding.DeleteSuccessPopupBinding
import com.familypedia.databinding.DialogCameraGalleryBinding
import com.familypedia.databinding.DialogConfirmationAltBinding
import com.familypedia.databinding.DialogConfirmationBinding
import com.familypedia.databinding.DialogDeleteBinding
import com.familypedia.databinding.DialogPhoneNumberVerifiedBinding
import com.familypedia.databinding.DialogPhotoPreviewBinding
import com.familypedia.databinding.DialogResetPasswordBinding
import com.familypedia.databinding.LayoutDialogSuccessBinding
import com.familypedia.network.LOGOUT
import com.familypedia.network.User
import com.familypedia.utils.Constants.ADD_NEW_CHARACTER
import com.familypedia.utils.Constants.FROM_DELETE_POST
import com.familypedia.utils.Constants.FROM_FORGOT_PASSWORD
import com.familypedia.utils.Constants.FROM_PERMISSION
import com.familypedia.utils.Constants.FROM_SIGNUP
import com.familypedia.utils.Constants.FROM_SIGNUP_CONFLICT
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.view.dashboard.home.adapter.ImageViewPagerAdapter
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

interface SuccessFulListener {
    fun onUpdateSuccessfully(from: String)
    fun onDismiss(from: String)
}

interface SuccessFulListenerAddCharacter {
    fun onUpdateSuccessfully()
    fun onDialogDismiss()
}

interface AuthDialogListener {
    fun onAuthDialogResendLinkClick(from: String)
    fun onAuthDialogDismiss(from: String)
}

interface RestrictionListener {
    fun onOkClick()
}

interface ConfirmationDialogListener {
    fun onYes()
    fun onNo()
}

interface UnfriendConfirmationDialogListener {
    fun onUnfriend()
    fun onDecline()
}

interface ConfirmationDeleteDialogListener {
    fun onYes(exception: DeleteAccounException)
    fun onNo()
}

interface DeleteSuccessDialogListener {
    fun onOK()
}

interface CameraGalleryListener {
    fun onCameraClicked()
    fun onGalleryClicked()
    fun onRemoveImageClicked()
}


class SuccessfulDialog(
    context: Context,
    val from: String,
    private val listener: SuccessFulListener,
    val title: String,
    val description: String,
) : android.app.Dialog(context) {

    private lateinit var binding: LayoutDialogSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutDialogSuccessBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        binding.tvTitlePhoneVerified.text = title
        binding.tvDescription.text = description
        this.setCancelable(false)

        if (from == ADD_NEW_CHARACTER) {
            binding.btnClose.text = context.getString(R.string.post_something_about_biography)
        }

        binding.btnClose.setSafeOnClickListener {
            listener.onUpdateSuccessfully(from)
            dismiss()
        }

        this.setOnDismissListener {
            listener.onDismiss(from)
        }
    }
}

class AuthDialog(
    val mContext: Context,
    val from: String,
    private val listener: AuthDialogListener,
    val title: String,
    val description: String,
) : android.app.Dialog(mContext) {

    private lateinit var binding: LayoutDialogSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutDialogSuccessBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Use View Binding to reference views
        binding.tvTitlePhoneVerified.text = title
        binding.tvDescription.text = description
        this.setCancelable(false)

        var countDownTimer: CountDownTimer? = null
        var counter = 0
        binding.ivDone.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.ic_email_verification))

        if (from == FROM_SIGNUP || from == FROM_FORGOT_PASSWORD) {
            binding.tvLinkNotReceived.showView()
            binding.tvTimer.showView()
            binding.tvLinkNotReceived.text = if (from == FROM_SIGNUP) {
                mContext.getString(R.string.email_verification_link_not_received)
            } else {
                mContext.getString(R.string.password_reset_not_received)
            }

            binding.tvTimer.text = mContext.getString(R.string.resend_in_60_sec)
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTimer.text = String.format(
                        mContext.getString(R.string.resend_in_sec),
                        (millisUntilFinished / 1000).toString()
                    )
                }

                override fun onFinish() {
                    binding.tvTimer.hideView()
                    binding.tvClickToResend.showView()
                }
            }
            countDownTimer.start()
        } else if (from == FROM_SIGNUP_CONFLICT) {
            // Handle the case for SIGNUP_CONFLICT if needed
        }

        binding.tvClickToResend?.setSafeOnClickListener {
            if (from == FROM_SIGNUP || from == FROM_FORGOT_PASSWORD) {
                counter++
                if (counter >= 2) {
                    binding.tvLinkNotReceivedMsg.showView()
                    binding.tvSupportEmail.showView()
                }
                listener.onAuthDialogResendLinkClick(from)
                binding.tvClickToResend.hideView()
                binding.tvLinkNotReceived.showView()
                binding.tvTimer.showView()
                countDownTimer?.start()
            } else {
                dismiss()
                listener.onAuthDialogResendLinkClick(from)
            }
        }

        binding.tvSupportEmail?.setSafeOnClickListener {
            Utility.mailTo(mContext)
        }

        binding.btnClose?.setSafeOnClickListener {
            listener.onAuthDialogDismiss(from)
            dismiss()
        }

        this.setOnDismissListener {
            listener.onAuthDialogDismiss(from)
        }
    }
}

class SuccessfulDialogAddCharacter(
    context: Context,
    val from: String,
    private val listener: SuccessFulListenerAddCharacter,
    val title: String,
    val description: String,
) : AppCompatDialog(context) {

    private lateinit var binding: LayoutDialogSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutDialogSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        binding.tvTitlePhoneVerified.text = title
        binding.tvDescription.text = description

        if (from == ADD_NEW_CHARACTER) {
            binding.btnClose.text = context.getString(R.string.post_something_about_character)
        }

        binding.btnClose.setSafeOnClickListener {
            listener.onUpdateSuccessfully()
            dismiss()
        }

        setOnDismissListener {
            listener.onDialogDismiss()
        }
    }
}

class RestrictionDialog(
    context: Context,
    private val listener: RestrictionListener,
    val title: String,
    val description: String,
) : AppCompatDialog(context) {

    private lateinit var binding: LayoutDialogSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutDialogSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        binding.tvTitlePhoneVerified.text = title
        binding.ivDone.hideView() // Ensure you have a `hideView()` extension function or method
        binding.tvDescription.text = description

        binding.btnClose.setSafeOnClickListener {
            listener.onOkClick()
        }

        setCancelable(false)
    }
}

class ConfirmationDialog(
    context: Context,
    private val listener: ConfirmationDialogListener,
    val title: String,
    val description: String,
    val from: String,
) : android.app.Dialog(context) {

    private lateinit var binding: DialogConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        binding.tvTitleConfirmation.text = title
        binding.tvDescriptionConfirmation.text = description
        when (from) {
            FROM_PERMISSION -> {
                binding.ivLogo.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_remove_friend))
            }
            FROM_DELETE_POST -> {
                binding.ivLogo.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_remove_post))
            }
            LOGOUT -> {
                binding.ivLogo.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_logout_confirmation))
            }
        }
        binding.btnYes.setSafeOnClickListener {
            listener.onYes()
            dismiss()
        }
        binding.btnNo.setSafeOnClickListener {
            listener.onNo()
            dismiss()
        }
    }
}

class BlockConfirmationDialog(
    context: Context,
    private val listener: ConfirmationDialogListener,
    val title: String,
    val description: String,
    val user: User?,
) : android.app.Dialog(context) {

    private lateinit var binding: DialogConfirmationAltBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogConfirmationAltBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        binding.tvTitleConfirmation2.text = "$description ${user?.name} ?"

        binding.btnYes2.setSafeOnClickListener {
            listener.onYes()
            dismiss()
        }
        binding.btnNo2.setSafeOnClickListener {
            listener.onNo()
            dismiss()
        }
    }
}

class ReportPostDialog(
    context: Context,
    private val listener: ConfirmationDialogListener,
    val title: String,
    val description: String,
) : android.app.Dialog(context) {

    private lateinit var binding: DialogConfirmationAltBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogConfirmationAltBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Use View Binding to access the views
        binding.tvTitleConfirmation2.text = description

        binding.btnYes2.setSafeOnClickListener {
            listener.onYes()
            dismiss()
        }
        binding.btnNo2.setSafeOnClickListener {
            listener.onNo()
            dismiss()
        }
    }
}

class UnfriendConfirmationDialog(
    context: Context,
    private val listener: UnfriendConfirmationDialogListener,
    val title: String,
    val description: String,
    val user: User?,
) : android.app.Dialog(context) {

    private lateinit var binding: DialogConfirmationAltBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogConfirmationAltBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Use View Binding to access the views
        binding.tvTitleConfirmation2.text = description

        binding.btnYes2.setSafeOnClickListener {
            listener.onUnfriend()
            dismiss()
        }
        binding.btnNo2.setSafeOnClickListener {
            listener.onDecline()
            dismiss()
        }
    }
}


interface DeleteAccounException {
    fun onExceptionOccured()
}


class DeleteAccountDialog(
    context: Context,
    private val listener: ConfirmationDeleteDialogListener,
    val description: String,
) : android.app.Dialog(context) {

    private lateinit var binding: DialogDeleteBinding

    private val exceptionListener = object : DeleteAccounException {
        override fun onExceptionOccured() {
            binding.progressBar.hideView()
            binding.btnYesDelete.text = context.getString(R.string.yes)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Use View Binding to access the views
        binding.tvDescriptionConfirmation.text = description

        binding.btnYesDelete.setSafeOnClickListener {
            listener.onYes(exceptionListener)
            binding.progressBar.showView()
            binding.btnYesDelete.text = ""
        }
        binding.btnNo.setSafeOnClickListener {
            listener.onNo()
            dismiss()
        }
    }
}


class DeleteSuccessDialog(
    context: Context,
    private val listener: DeleteSuccessDialogListener,
    val description: String,
) : android.app.Dialog(context) {

    private lateinit var binding: DeleteSuccessPopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DeleteSuccessPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Use View Binding to access the views
        binding.tvDescription.text = description

        binding.btnClose.setSafeOnClickListener {
            listener.onOK()
            dismiss()
        }
    }
}

class ResetPasswordDialog(
    context: Context,
    val from: String,
    private val listener: SuccessFulListener,
    val title: String,
) : Dialog(context) {

    private lateinit var binding: DialogResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout with View Binding
        binding = DialogResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Set the title text
        binding.tvTitleReset.text = title

        // Set up button click listener with View Binding
        binding.btnLogin.setSafeOnClickListener {
            listener.onUpdateSuccessfully(from)
            dismiss()
        }
    }
}

class PhoneNumberVerified(
    context: Context,
    val from: String,
    private val listener: SuccessFulListener,
) : Dialog(context) {

    private lateinit var binding: DialogPhoneNumberVerifiedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout with View Binding
        binding = DialogPhoneNumberVerifiedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Set up button click listener with View Binding
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
    }
}



class CameraGalleryDialog(
    context: Context,
    private val noImage: Boolean,
    private val listener: CameraGalleryListener,
) : Dialog(context) {

    private lateinit var binding: DialogCameraGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout with View Binding
        binding = DialogCameraGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER_HORIZONTAL

        setData()
    }

    private fun setData() {
        // Set up button click listeners with View Binding
        binding.llCamera.setSafeOnClickListener {
            listener.onCameraClicked()
            dismiss()
        }

        binding.llGallery.setSafeOnClickListener {
            listener.onGalleryClicked()
            dismiss()
        }

        binding.llRemove.setSafeOnClickListener {
            listener.onRemoveImageClicked()
            dismiss()
        }

        // Handle visibility based on noImage flag
        binding.llRemove.visibility = if (noImage) View.GONE else View.VISIBLE
    }
}

class BlockedByAdminDialog(
    context: Context,
    val title: String,
    val description: String,
) : Dialog(context) {

    private lateinit var binding: LayoutDialogSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout with View Binding
        binding = LayoutDialogSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Set the title and description using View Binding
        binding.tvTitlePhoneVerified.text = title
        binding.tvDescription.text = description

        // Handle button actions with View Binding
        // Uncomment this if you want the close button to be clickable
        /*binding.btnClose.setSafeOnClickListener {
            dismiss()
        }*/

        // Hide the close button
        binding.btnClose.hideView()

        // Prevent dialog from being canceled by back button or outside touch
        this.setCancelable(false)
    }
}

class DialogPhotoPreview(
    private val mContext: Context,
    private val imageString: String,
    private val imagesList: List<String>,
    private val currentPosition: Int = 0,
) : Dialog(mContext) {

    private lateinit var binding: DialogPhotoPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Inflate the layout with View Binding
        binding = DialogPhotoPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val wlp = window?.attributes
        wlp?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wlp?.gravity = Gravity.CENTER

        // Set click listener for the close button
        binding.ivClosePreview.setSafeOnClickListener {
            this.dismiss()
        }

        // Load the image using Glide
        Glide.with(mContext)
            .load(if (imageString.startsWith("http")) imageString else IMAGE_URL + imageString)
            .error(R.drawable.ic_item_placeholder)
            .placeholder(R.drawable.ic_item_placeholder)
            .into(binding.ivPreview)

        // Setup ViewPager and Indicator
        binding.postsViewPager.adapter = ImageViewPagerAdapter(false, imagesList) { img, currPosition ->
            // Uncomment this line if you want to show a new dialog on image click
            // DialogPhotoPreview(context, img, imagesList).show()
        }
        binding.postsViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.postsViewPager.currentItem = currentPosition

        // Configure the indicator
        binding.indicatorView.apply {
            setSliderColor(
                ContextCompat.getColor(context, R.color.color_grey),
                ContextCompat.getColor(context, R.color.color_blue_light)
            )
            setSliderWidth(resources.getDimension(R.dimen.dp_10))
            setSliderHeight(resources.getDimension(R.dimen.dp_5))
           // setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setupWithViewPager(binding.postsViewPager)
        }
    }
}