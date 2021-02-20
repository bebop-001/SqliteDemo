package com.kana_tutor.utils
/*
 *  Copyright (C) 2021 kana-tutor.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
