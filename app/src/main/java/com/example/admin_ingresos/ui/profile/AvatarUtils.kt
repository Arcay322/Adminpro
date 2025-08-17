package com.example.admin_ingresos.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AvatarUtils {
    fun saveAvatar(context: Context, uri: Uri, fileName: String = "avatar_profile.jpg"): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val dir = File(context.filesDir, "avatars")
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, fileName)
            FileOutputStream(outFile).use { out ->
                input.copyTo(out)
            }
            outFile.absolutePath
        } catch (t: Throwable) {
            null
        }
    }
}
