package proj.ksks.arknights.arknights_calc

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.Image
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.KITKAT)
private fun yuv420888ToNv21(image: Image): ByteArray {
    val width = image.width
    val height = image.height
    val ySize = width * height
    val uvSize = width * height / 4

    val nv21 = ByteArray(ySize + uvSize * 2)

    val yBuffer: ByteBuffer = image.planes[0].buffer // Y
    val uBuffer: ByteBuffer = image.planes[1].buffer // U
    val vBuffer: ByteBuffer = image.planes[2].buffer // V

    val yStride = image.planes[0].rowStride
    val uvStride = image.planes[1].rowStride
    val uvPixelStride = image.planes[1].pixelStride

    var pos = 0

    if (yStride == width) { // copy Y
        yBuffer.get(nv21, 0, ySize)
        pos += ySize
    } else {
        for (row in 0 until height) {
            yBuffer[nv21, pos, width]
            pos += width
            if (row < height - 1) {
                yBuffer.position(yBuffer.position() + yStride - width)
            }
        }
    }

    if (uvStride == width && uvPixelStride == 2) { // copy U and V
        uBuffer.get(nv21, ySize, uvSize)
        vBuffer.get(nv21, ySize + uvSize, uvSize)
    } else {
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                nv21[pos++] = vBuffer.get(col * uvPixelStride)
                nv21[pos++] = uBuffer.get(col * uvPixelStride)
            }
            uBuffer.position(uBuffer.position() + uvStride - width / 2)
            vBuffer.position(vBuffer.position() + uvStride - width / 2)
        }
    }

    return nv21
}

@TargetApi(Build.VERSION_CODES.KITKAT)
fun imageToBitmap(image: Image): Bitmap? {
    try {
        if (image.format == ImageFormat.YUV_420_888) {
            val nv21 = yuv420888ToNv21(image)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, image.width, image.height),
                100,
                out
            )
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } else {
            val width = image.width
            val height = image.height
            val planes = image.planes
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride: Int = planes[0].pixelStride
            val rowStride: Int = planes[0].rowStride

            val bitmap = Bitmap.createBitmap(
                rowStride / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )
            if (buffer.capacity() < bitmap.allocationByteCount) {
                throw RuntimeException("Buffer not large enough for pixels")
            }
            bitmap.copyPixelsFromBuffer(buffer)
            return Bitmap.createBitmap(bitmap, 0, 0, width, height) // Crop to remove padding
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null;
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String = "image_${System.currentTimeMillis()}") {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyApp") // Optional: define your custom folder
    }

    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        val outputStream: OutputStream? = contentResolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
}

fun ocrBitmap(bitmap: Bitmap, callback: OnSuccessListener<Text>) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    recognizer.process(image)
        .addOnSuccessListener(callback)
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}