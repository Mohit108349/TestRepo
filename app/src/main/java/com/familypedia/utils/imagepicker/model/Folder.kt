package com.familypedia.utils.imagepicker.model


class Folder(var folderName: String) {
    var images: MutableList<Image> = mutableListOf()
}