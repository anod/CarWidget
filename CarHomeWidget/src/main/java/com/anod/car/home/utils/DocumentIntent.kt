// Copyright (c) 2019. Alex Gavrishev
package com.anod.car.home.utils

import android.content.Intent
import android.net.Uri

//--------------------------------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation. All rights reserved.
//--------------------------------------------------------------------------------------------------

fun Intent.forOpen(mimeTypes: Array<String>): Intent {
    action = Intent.ACTION_OPEN_DOCUMENT
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "*/*"
    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    return this
}

fun Intent.forOpenBackup(): Intent = forOpen(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*"))

fun Intent.forCreate(filename: String, data: Uri, type: String): Intent {
    action = Intent.ACTION_CREATE_DOCUMENT
    setDataAndType(data, type)
    putExtra(Intent.EXTRA_TITLE, filename)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return this
}

fun Intent.forCreateBackup(filename: String, data: Uri) = forCreate(filename, data, "application/json")