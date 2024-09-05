package com.familypedia.utils.customGallery

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import java.io.File

class SingleMediaScanner : MediaScannerConnection.MediaScannerConnectionClient {

    private var mMs: MediaScannerConnection? = null
    private var mFile: File? = null
    private var scanListener: ScanListener? = null

    constructor(context: Context, f: File) {
        init(context, f)
    }

    constructor(context: Context, f: File, scanListener: ScanListener) {
        init(context, f)
        this.scanListener = scanListener
    }

    private fun init(context: Context, f: File) {
        mFile = f
        mMs = MediaScannerConnection(context, this)
        mMs!!.connect()
    }

    override fun onMediaScannerConnected() {
        mMs!!.scanFile(mFile!!.absolutePath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        if (scanListener != null) scanListener!!.onScanCompleted()
        mMs!!.disconnect()
    }
}