package com.familypedia.view.dashboard.character.post

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.familypedia.R
import com.familypedia.databinding.ActivityPostDetailBinding
import com.familypedia.network.PostData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.POST_ID
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.home.adapter.ImageViewPagerAdapter
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.viewmodels.CharacterViewModel
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity(), SuccessFulListener, FamilyPediaClickListener {
    private var characterViewModel: CharacterViewModel? = null
    private var postId: String = ""
    private var userId = ""
    private var from = ""
    private lateinit var binding: ActivityPostDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeContrl()
    }

    private fun initializeContrl() {
        userId = Utility.getPreferencesString(this, Constants.USER_ID)
        postId = intent.extras?.getString(POST_ID) ?: ""
        from = intent.extras?.getString(FROM) ?: ""
        initViewModel()
        listener()

        if (from == NOTIFICATION)
            binding?.toolbarPostDetail?.tvToolbarTitle?.text = getString(R.string.notification_detail)
        else
            binding?.toolbarPostDetail?.tvToolbarTitle?.text = getString(R.string.post_detail)

    }

    private fun listener() {
        binding?.somethingWrong?.clSomthingWentWrong?.familyPediaClickListener(this)
        binding?.toolbarPostDetail?.ivBack?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.somethingWrong?.clSomthingWentWrong -> hitPostDetailAPI()
            binding?.toolbarPostDetail?.ivBack -> onBackPressed()
        }
    }

    private fun setData(postData: PostData) {
        binding?.clChild?.showView()
        if (postData.postedBy?._id == userId || postData.character?.userId == userId) {
            binding?.ivEditCharacter?.showView()
            binding?.ivDeleteCharacter?.showView()
        } else {
            binding?.ivEditCharacter?.hideView()
            binding?.ivDeleteCharacter?.hideView()
        }
        binding?.tvPostBy?.setTextOnTextView(postData.postedBy?.name, "")
        binding?.tvPostBy?.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.FROM, Constants.FROM_VIEW_USER)
            bundle.putString(Constants.USER_ID, postData.postedBy?._id)
            ViewProfileActivity.open(this, bundle)

        }

        binding?.ivCharacterImage?.loadImagesWithGlide(
            Constants.IMAGE_URL + postData.character?.profile_pic,
            false
        )
        if (postData.eventType != Constants.MULTIPLE_DAY_EVENT) {
            binding?.tvTo?.hideView()
            binding?.tvToDate?.hideView()
            binding?.tvFrom?.text = getString(R.string.date_)
        } else {
            binding?.tvTo?.showView()
            binding?.tvToDate?.showView()
            binding?.tvFrom?.text = getString(R.string.from)
        }
        binding?.tvDescription?.setTextOnTextView(postData.description, "")
        binding?.tvCharacterName?.setTextOnTextView(postData.character?.name, "")
        binding?.tvFromDate?.setTextOnTextView(postData.startingDate, "")
        binding?.tvToDate?.setTextOnTextView(postData.endingDate, "")
        binding?.tvLocation?.setTextOnTextView(postData.location, "")

        val imagesSize = postData.attachPhotos?.size
        if (postData.attachPhotos == null) {
            binding?.postsViewPager?.hideView()
            binding?.indicatorView?.hideView()
        } else {
            if (imagesSize == 0) {
                binding?.postsViewPager?.hideView()
                binding?.indicatorView?.hideView()
            } else {
                binding?.postsViewPager?.showView()
                binding.indicatorView?.showView()
            }
        }


        binding?.llProfile?.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.USER_NAME, postData.character?.name)
            bundle.putString(Constants.USER_ID, postData.character?._id)
            bundle.putString(Constants.PROFILE_PIC, postData.character?.profile_pic)
            bundle.putSerializable(
                Constants.PERMITTED_USERS,
                postData.character
            ) //ask userId in , creater in character object
            AboutCharacterActivity.open(this, bundle)
        }

        postData.attachPhotos?.let {
            if (it.isEmpty()) {
                binding?.postsViewPager?.hideView()
                binding?.indicatorView?.hideView()
                return
            }
            binding?.postsViewPager?.adapter =
                ImageViewPagerAdapter(true, it) { img, currPosition ->
                    DialogPhotoPreview(this, img, it, currPosition).show()
                }
            binding?.postsViewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            val currentPageIndex = 0
            binding?.postsViewPager?.currentItem = currentPageIndex

            binding?.indicatorView?.apply {
                setSliderColor(
                    ContextCompat.getColor(this@PostDetailActivity, R.color.color_grey),
                    ContextCompat.getColor(this@PostDetailActivity, R.color.color_blue_light)
                )
                setSliderWidth(resources.getDimension(R.dimen.dp_10))
                setSliderHeight(resources.getDimension(R.dimen.dp_5))
                setSlideMode(IndicatorSlideMode.WORM)
                setIndicatorStyle(IndicatorStyle.CIRCLE)
                binding?.postsViewPager?.let { it1 -> setupWithViewPager(it1) }
            }
        }

        ////////////
        /*postData.attachPhotos?.let {
            //1 Image
            //visibility GONE =>  iv2 , llCol2
            //visibility ON => iv1
            if (imagesSize == 1) {
                iv2.hideView()
                llCol2.hideView()
                iv1.showView()

                iv1.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![0])

                listener(1, postData)
            }

            //2Image
            //Gone: iv2, llRowLast
            //ON, iv1,iv3
            else if (imagesSize == 2) {
                iv2.hideView()
                llRowLast.hideView()

                iv1.showView()
                iv3.showView()

                iv1.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![0])
                iv3.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![1])
                listener(2, postData)
            }

            //3 Image
            //GONE: iv2, iv4
            //ON, iv1, iv3, iv5
            else if (imagesSize == 3) {
                iv2.hideView()
                iv4.hideView()
                iv1.showView()
                iv3.showView()
                iv5.showView()


                iv1.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![0])
                iv3.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![1])
                iv5.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![2])
                listener(3, postData)
            }
            //4 Images
            //GONE iv4

            else if (imagesSize == 4) {
                iv4.hideView()
                iv1.showView()
                iv2.showView()
                iv3.showView()
                iv5.showView()

                iv1.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![0])
                iv2.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![1])
                iv3.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![2])
                iv5.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![3])
                listener(4, postData)
            }

            //5 Images
            else if (imagesSize == 5) {
                iv1.showView()
                iv2.showView()
                iv3.showView()
                iv4.showView()
                iv5.showView()
                iv1.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![0])
                iv2.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![1])
                iv3.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![2])
                iv4.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![3])
                iv5.loadPlaceholderImagesWithGlide(Constants.IMAGE_URL + postData.attachPhotos!![4])
                //listener(postData)
                listener(5, postData)
            }

        }*/

    }

    /* private fun listener(size: Int, postData: PostData) {
         if (size == 1) {
             iv1?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData?.attachPhotos?.get(0) ?: "",postData?.attachPhotos!!).show()
             }
         } else if (size == 2) {
             iv1?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData?.attachPhotos?.get(0) ?: "",postData?.attachPhotos!!).show()
             }
             iv3?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(1) ?: "",postData?.attachPhotos!!).show()
             }
         } else if (size == 3) {
             iv1?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData?.attachPhotos?.get(0) ?: "",postData?.attachPhotos!!).show()
             }
             iv3?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(1) ?: "",postData?.attachPhotos!!).show()
             }
             iv5?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(2) ?: "",postData?.attachPhotos!!).show()
             }
         } else if (size == 4) {
             iv1?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData?.attachPhotos?.get(0) ?: "",postData?.attachPhotos!!).show()
             }
             iv2?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(1) ?: "",postData?.attachPhotos!!).show()
             }
             iv3?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(2) ?: "",postData?.attachPhotos!!).show()
             }
             iv5?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(3) ?: "",postData?.attachPhotos!!).show()
             }
         } else if (size == 5) {
             iv1?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData?.attachPhotos?.get(0) ?: "",postData?.attachPhotos!!).show()
             }
             iv2?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(1) ?: "",postData?.attachPhotos!!).show()
             }
             iv3?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(2) ?: "",postData?.attachPhotos!!).show()
             }
             iv4?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(3) ?: "",postData?.attachPhotos!!).show()
             }
             iv5?.setSafeOnClickListener {
                 DialogPhotoPreview(this, postData.attachPhotos?.get(4) ?: "",postData?.attachPhotos!!).show()
             }
         }
     }*/

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        responseData()
        manageDataLoadingStatus()
        onGettingException()
        onGettingError()
        hitPostDetailAPI()
    }

    private fun hitPostDetailAPI() {
        if (postId.isNotEmpty())
            characterViewModel?.getPostDetail(this, postId)
    }

    private fun responseData() {
        characterViewModel?.createUpdatePostResult?.observe(
            this
        ) { characterResponse ->
            if (characterResponse?.data != null) {
                setData(characterResponse.data)
            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this) {
            if (it.message == getString(R.string.no_internet)) {
                manageSomethingWentWrong(ErrorType.NO_INTERNET)
            } else {
                //clParent?.showStringSnackbarError(it.message)
                toast(it.message)
                onBackPressed()
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
            manageShimmer(false)
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(this) { errorData ->
            if (errorData.status == Constants.STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(this, errorData.message)
                return@observe
            } else if (errorData.status == Constants.STATUS_USER_DELETED) {
                SuccessfulDialog(
                    this, Constants.FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                //clParent?.showStringSnackbarError(errorData.message)
                toast(errorData.message)
                manageShimmer(false)
                onBackPressed()
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            binding?.progressLoading?.showView()
        } else {
            binding?.progressLoading?.hideView()
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        binding?.somethingWrong?.clSomthingWentWrong?.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_data_available), "")
        } else if (errorType == ErrorType.NO_INTERNET) { //no internet
            binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
        } else {
            binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.something_went_wrong), "")
        }
    }

    override fun onUpdateSuccessfully(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    override fun onDismiss(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    private fun performAccountDeleted() {
        lifecycleScope.launch {
            Utility.clearAllPreferences(this@PostDetailActivity)
            AuthViewsHolderActivity.open(this@PostDetailActivity, null)
            finishAffinity()
        }
    }


    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, PostDetailActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}