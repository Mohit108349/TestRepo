package com.familypedia.view.dashboard.character.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.familypedia.R
import com.familypedia.databinding.ActivityCropPostImagesBinding
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView
import com.familypedia.utils.showView
import com.familypedia.view.dashboard.character.post.adapter.CropImageAdapter


class CropPostImagesActivity : AppCompatActivity() {
    private var currentPageIndex = 0
    private var selectedImagesUriList = arrayListOf<Uri>()
    private var cropAdapter: CropImageAdapter? = null
    private lateinit var binding: ActivityCropPostImagesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropPostImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initControl()
    }

    private fun setToolbar() {
        binding?.ivCrop?.setSafeOnClickListener {
          /*  binding?.CropImage.activity(selectedImagesUriList[currentPageIndex])
                .start(this)*/
        }
        binding?.toolbarAddNewPost?.ivBack?.setSafeOnClickListener {
            onBackPressed()
        }
        binding?.toolbarAddNewPost?.tvToolbarTitle?.setTextOnTextView(getString(R.string.selected_photos), "")
        binding?.toolbarAddNewPost?.tvDone?.showView()
      /*  binding?.tvDone.setSafeOnClickListener {
            val resultIntent = Intent()
            val bundle=Bundle()
            bundle.putSerializable("images", selectedImagesUriList)
            resultIntent.putExtras(bundle)
            setResult(RESULT_OK, resultIntent)
            finish()

        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                selectedImagesUriList.removeAt(currentPageIndex)
                selectedImagesUriList.add(currentPageIndex, resultUri)
                cropAdapter?.notifyDataSetChanged()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }*/
    }

    private fun initControl() {
        setToolbar()
        selectedImagesUriList = intent.extras?.getSerializable("images") as ArrayList<Uri>
        cropAdapter = CropImageAdapter(selectedImagesUriList) { img ->
        }
        binding?.viewPager?.adapter = cropAdapter
        binding?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding?.viewPager?.currentItem = currentPageIndex
        binding?.viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPageIndex = position
            }
        })
        //viewPager.isUserInputEnabled = false
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, CropPostImagesActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
}