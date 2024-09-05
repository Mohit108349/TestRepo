package com.familypedia.utils.customGallery

class Define {
    val PERMISSION_STORAGE = 28
    val PERMISSION_LOCATION= 29
    val TRANS_IMAGES_RESULT_CODE = 29
    val TAKE_A_PICK_REQUEST_CODE = 128
    val ENTER_ALBUM_REQUEST_CODE = 129
    val ENTER_DETAIL_REQUEST_CODE = 130
    val INTENT_ADD_PATH = "intent_add_path"
    val INTENT_POSITION = "intent_position"

    val SAVE_INSTANCE_ALBUM_LIST = "instance_album_list"
    val SAVE_INSTANCE_ALBUM_THUMB_LIST = "instance_album_thumb_list"

    val SAVE_INSTANCE_NEW_IMAGES = "instance_new_images"
    val SAVE_INSTANCE_SAVED_IMAGE = "instance_saved_image"

    enum class BUNDLE_NAME {
        POSITION,
        ALBUM
    }

    companion object {

        val ALBUM_REQUEST_CODE = 27

        val INTENT_PATH = "intent_path"
    }


}