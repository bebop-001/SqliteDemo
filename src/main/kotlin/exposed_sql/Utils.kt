/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package exposed_sql

import java.io.File

// make directories as needed.  Assume we start from
// the filesystem root.
fun mkDir(dir: File): File {
    val dirs = dir.toString().split("/")
        .filter { it.isNotEmpty() }
        .toMutableList()
    var d: File? = if(dirs.isNotEmpty())
            File("/${dirs.removeAt(0)}")
        else null
    while (d != null) {
        if (!d.exists() && !d.mkdir())
            throw RuntimeException("mkDir:mkdir($d) FAILED")
        d = if(dirs.isNotEmpty())
            File(d, dirs.removeAt(0))
        else null
    }
    return dir
}

