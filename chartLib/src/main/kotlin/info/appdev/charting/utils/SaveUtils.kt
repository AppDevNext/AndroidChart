package info.appdev.charting.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object SaveUtils {

    /**
     * Saves the current chart state with the given name to the given path on
     * the sdcard leaving the path empty "" will put the saved file directly on
     * the SD card chart is saved as a PNG image, example:
     * saveToPath("myfilename", "foldername1/foldername2");
     *
     * @param pathOnSD e.g. "folder1/folder2/folder3"
     * @return returns true on success, false on error
     */
    fun saveToPath(title: String?, pathOnSD: String?, chartBitmap: Bitmap): Boolean {
        val stream: OutputStream?
        try {
            stream = FileOutputStream(Environment.getExternalStorageDirectory().path + pathOnSD + "/" + title + ".png")

            /*
			 * Write bitmap to file using JPEG or PNG and 40% quality hint for JPEG.
			 */
            chartBitmap.compress(CompressFormat.PNG, 40, stream)

            stream.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Saves the current state of the chart to the gallery as an image type. The
     * compression must be set for JPEG only. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName        e.g. "my_image"
     * @param subFolderPath   e.g. "ChartPics"
     * @param fileDescription e.g. "Chart details"
     * @param format          e.g. Bitmap.CompressFormat.PNG
     * @param quality         e.g. 50, min = 0, max = 100
     * @return returns true if saving was successful, false if not
     */
    fun saveToGallery(fileName: String, subFolderPath: String?, fileDescription: String?, format: CompressFormat, quality: Int, chartBitmap: Bitmap, context: Context): Boolean {
        // restrain quality
        var fileName = fileName
        var quality = quality
        if (quality !in 0..100) {
            quality = 50
        }

        val currentTime = System.currentTimeMillis()

        val extBaseDir = Environment.getExternalStorageDirectory()
        val file = File(extBaseDir.absolutePath + "/DCIM/" + subFolderPath)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false
            }
        }

        val mimeType: String?
        when (format) {
            CompressFormat.PNG -> {
                mimeType = "image/png"
                if (!fileName.endsWith(".png")) {
                    fileName += ".png"
                }
            }

            CompressFormat.WEBP -> {
                mimeType = "image/webp"
                if (!fileName.endsWith(".webp")) {
                    fileName += ".webp"
                }
            }

            CompressFormat.JPEG -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) {
                    fileName += ".jpg"
                }
            }

            else -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) {
                    fileName += ".jpg"
                }
            }
        }

        val filePath = file.absolutePath + "/" + fileName
        val out: FileOutputStream?
        try {
            out = FileOutputStream(filePath)

            chartBitmap.compress(format, quality, out)

            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()

            return false
        }

        val size = File(filePath).length()

        val values = ContentValues(8)

        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.DATE_ADDED, currentTime)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        values.put(MediaStore.Images.Media.DESCRIPTION, fileDescription)
        values.put(MediaStore.Images.Media.ORIENTATION, 0)
        values.put(MediaStore.Images.Media.DATA, filePath)
        values.put(MediaStore.Images.Media.SIZE, size)

        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null
    }

}
