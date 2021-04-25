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

// copied from https://github.com/JetBrains/Exposed/wiki/Getting-Started
// changed driver and transaction to support sqlite and connection
// to create a db file.
package exposed_sql

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.lang.RuntimeException
import java.sql.Connection

object ExposedDslTest {
    object Cities : IntIdTable() {
        val name = varchar("name", 50)
    }

    private fun mkDir(dir: File): File {
        val dirs = dir.toString().split("/")
            .filter { it.isNotEmpty() }
            .toMutableList()
        while (dirs.isNotEmpty()) {
            val d = File(dirs.removeAt(0))
            if (!d.exists() && !d.mkdir())
                throw RuntimeException("mkDir:mkdir($d) FAILED")
        }
        return dir
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val dbDir = mkDir(File("${System.getenv("PWD")}/exposed_db"))
        val dbFile = File(dbDir, "exposed_dsl.sqlite")

        Database.connect("jdbc:sqlite:$dbFile", driver = "org.sqlite.JDBC")

        transaction(
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE,
            repetitionAttempts = 3
        ) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities)

            // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            Cities.insert {
                it[name] = "St. Petersburg"
            } get Cities.id

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Cities: ${Cities.selectAll()}")
        }
    }
}
