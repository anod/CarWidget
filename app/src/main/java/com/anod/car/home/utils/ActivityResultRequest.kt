package com.anod.car.home.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import com.anod.car.home.R

class ActivityResultRequest {
    class PickImage : ActivityResultContract<PickImage.Args, Uri?>() {
        companion object {
            private const val mimeImage = "image/*"
        }

        class Args(
                val type: String = mimeImage,
                val output: Uri?,
                val outputFormat: String? = Bitmap.CompressFormat.PNG.name
        )

        @CallSuper
        override fun createIntent(context: Context, input: Args): Intent {
            return Intent(Intent.ACTION_GET_CONTENT).apply {
                type = input.type
                if (input.output != null) {
                    putExtra(MediaStore.EXTRA_OUTPUT, input.output)
                }
                if (input.outputFormat != null) {
                    putExtra("outputFormat", input.outputFormat)
                }
            }

        }

        override fun getSynchronousResult(context: Context, input: Args): SynchronousResult<Uri?>? {
            return null
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
        }
    }

    class CreateChooser(private val titleResId: Int): ActivityResultContract<Intent, Intent?>() {
        override fun createIntent(context: Context, chooseIntent: Intent): Intent {
            return Intent.createChooser(chooseIntent, context.getString(titleResId))
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
            return if (intent == null || resultCode != Activity.RESULT_OK) null else intent
        }
    }
}