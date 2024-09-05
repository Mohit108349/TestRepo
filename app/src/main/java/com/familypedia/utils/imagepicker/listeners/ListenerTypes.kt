package com.familypedia.utils.imagepicker.listeners

import com.familypedia.utils.imagepicker.model.Folder
import com.familypedia.utils.imagepicker.model.Image

typealias OnImageClickListener = (Boolean) -> Boolean
typealias OnFolderClickListener = (Folder) -> Unit
typealias OnImageSelectedListener = (List<Image>) -> Unit
