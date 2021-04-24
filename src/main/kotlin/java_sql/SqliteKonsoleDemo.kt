/*
 * Copyright 2021 Steven Smith kana-tutor.com
 *
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
package java_sql

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class SqliteConsoleDemo {
    data class EmployeeInfo (
        val id:Int,
        var name:String,
        var age:Int,
        var address:String,
        var salary:Float
    ) {
        private val salString:String
            get() = "%.2f".format(salary)
        override fun toString() : String =
            """(ID, NAME, AGE, ADDRESS, SALARY)
                VALUES ($id, '$name', $age, '$address', $salString)
        """.trimMargin()
    }

    companion object {
        private val employeeTable = listOf(
            EmployeeInfo (1, "Paul", 32, "California", 20000.00f ),
            EmployeeInfo (2, "Allen", 25, "Texas", 15000.00f ),
            EmployeeInfo (3, "Teddy", 23, "Norway", 20000.00f ),
            EmployeeInfo (4, "Mark", 25, "Rich-Mond ", 65000.00f ),
        )

        private const val dbFileName = "test.db"
        private val dbConnection:Connection
            get() = DriverManager.getConnection("jdbc:sqlite:$dbFileName")

        private fun createTable() {
            val c = dbConnection
            val stmt = c.createStatement()
            val sql = """
                CREATE TABLE IF NOT EXISTS COMPANY (
                    ID INT PRIMARY KEY     NOT NULL,
                    NAME           TEXT    NOT NULL,
                    AGE            INT     NOT NULL,
                    ADDRESS        CHAR(50),
                    SALARY         REAL
                )""".trimMargin()
            stmt.executeUpdate(sql)
            stmt.close()
            c.close()
        }
        private fun addEmployees(table:List<EmployeeInfo>) {
            val c = dbConnection
            val stmt = c.createStatement()
            table.forEach{employee ->
                stmt.executeUpdate("INSERT INTO COMPANY $employee;")
            }
            stmt.close()
            c.close()
        }
        // I ended up fetching all the records.  Sqlite supports
        // only forward ref and no concurrency.
        private fun annualEmployeeReview() {
            val c = dbConnection
            val stmt = c.createStatement()
            val rs :ResultSet = stmt.executeQuery(
                "SELECT * FROM COMPANY"
            )
            val empTable = mutableListOf<EmployeeInfo>()
            while (rs.next()) {
                with (rs) {
                    empTable.add(
                        EmployeeInfo(
                        getInt("ID"), getString("NAME"), getInt("AGE"),
                        getString("ADDRESS"), getString("SALARY").toFloat()
                    )
                    )
                }
            }
            empTable.forEach{e->
                val sql = """UPDATE COMPANY 
                    SET SALARY = ${"%.2f".format(e.salary * 1.1f)},
                    AGE = ${e.age + 1}
                    WHERE ID = ${e.id}
                    """.trimIndent()
                stmt.executeUpdate(sql)
            }
            stmt.close()
            c.close()
            println("Everyone is a year older and got a 10% raise.")
            printResults()
        }
        private fun layoff(state:String) {
            println("Layoffs... closed the plant in $state")
            val c = dbConnection
            val stmt = c.createStatement()
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM COMPANY")
            rs.next()
            val empCntBefore = rs.getInt(1)
            val sql = """DELETE from COMPANY where ADDRESS = "$state";"""
            stmt.executeUpdate(sql)
            rs = stmt.executeQuery("SELECT COUNT(*) FROM COMPANY")
            rs.next()
            val empCntAfter = rs.getInt(1)
            println("Reduced employees by ${empCntBefore-empCntAfter}")
            stmt.close()
            c.close()
            printResults()
        }
        private fun printResults() {
            val c = dbConnection
            val stmt = c.createStatement()
            val rs = stmt.executeQuery(
                "SELECT * FROM COMPANY"
            )
            var idx = 1
            while (rs.next()) {
                with (rs) {
                    println(
                        "Record %2d) ".format(idx++) + EmployeeInfo(
                            getInt("ID"), getString("NAME"), getInt("AGE"),
                            getString("ADDRESS"), getString("SALARY").toFloat()
                        ).toString()
                    )
                }
            }
            stmt.close()
            c.close()
        }
        @JvmStatic
        fun main(args: Array<String>) {
            val theDb = File(dbFileName)
            if (theDb.exists()) {
                println("removed current db:$dbFileName")
                theDb.delete()
            }
            createTable()
            addEmployees(employeeTable)
            printResults()
            annualEmployeeReview()
            layoff("Texas")
        }
    }
}
