package com.kana_tutor.utils

import android.content.Context
import android.util.Log
import java.io.*

@Throws(IOException::class)
fun Context.copyFileFromAssets(nameIn:String, nameOut:String, force:Boolean = false) {
    if (File(nameOut).exists() && !force) {
        throw IOException("""copyFileFromAssets:destination file $nameOut exists.
            |Please set \"force\" to overwrite.
        """.trimMargin())
    }
    val outDirs = nameOut.trim().split("/+".toRegex()).toMutableList()
    if(outDirs.size == 1) throw IOException(
        "copyFileFromAssetes:path \"$nameOut\" must include directory"
    )
    var dest = File("/${outDirs.removeAt(0)}")
    while(outDirs.size > 0) {
        if(!dest.exists()) dest.mkdir()
        dest = File("$dest/${outDirs.removeAt(0)}")
    }
    val source: InputStream = getAssets().open(nameIn)
    val destination: OutputStream = FileOutputStream(dest)
    val buffer = ByteArray(1024)
    var destSize = 0L
    var bytesRead:Int
    do {
        bytesRead = source.read(buffer)
        if (bytesRead < 0) break
        destination.write(buffer, 0, bytesRead)
        destSize += bytesRead
    } while (bytesRead > 0)
    destination.flush()
    destination.close()
    source.close()
    Log.d("copyFileFromAssets", "wrote $destSize bytes.")
}
