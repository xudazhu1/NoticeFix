package com.xeasy.noticefix.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.UriCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File

class RequestAccessAppDataDir : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, input: String): Intent {


//        val dirUri = createAppDataDirUri(input)
        val dirUri = Uri.fromFile(File("/data/data/com.xeasy.noticefix/shared_prefs"))

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        val documentFile = DocumentFile.fromTreeUri(context.applicationContext, dirUri)!!
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.uri)

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }

}