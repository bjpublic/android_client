package online.dailyq.api

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

fun Uri.asRequestBody(cr: ContentResolver): RequestBody {
    return object : RequestBody() {
        override fun contentType(): MediaType? =
            cr.getType(this@asRequestBody)?.toMediaTypeOrNull()

        override fun contentLength(): Long = -1

        override fun writeTo(sink: BufferedSink) {
            val source = cr.openInputStream(this@asRequestBody)?.source()
            source?.use { sink.writeAll(it) }
        }
    }
}
