package com.kana_tutor.sqlltdemo
// SQLite Database for Android - Full Course
// same as course but in kotlin.
// https://www.youtube.com/watch?v=312RhjfetP8
// goal is to setup sqllite db, query, add, and
// delete items.

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.kana_tutor.sqlltdemo.databinding.ActivityMainBinding

data class CustomerModel (
    val id:Int,
    val name:String,
    val age:Int,
    val isActive:Boolean,
        )
class DbHelper(context: Context,
               name:String,
               factory: SQLiteDatabase.CursorFactory?,
               version:Int
) : SQLiteOpenHelper(context, name, factory, version) {
    val CUST_TABLE = "CUSTOMER_TABLE"
    val COL_NAME = "COLUMN_CUSTOMER_NAME"
    val COL_AGE = "COLUMN_CUSTOMER_AGE"
    val COL_ACTIVE = "COLUMN_CUSTOMER_ACTIVER"
    val COL_ID  = "ID"

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        val createSqlTable = """CREATE TABLE $CUST_TABLE (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NAME TEXT,
            $COL_AGE INT,
            $COL_ACTIVE BOOL
            )"""
        db!!.execSQL(createSqlTable)
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     *
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun addCustomer(customerModel: CustomerModel) : Boolean {
        val cv = ContentValues()
        with (customerModel) {
            cv.put(COL_NAME, name)
            cv.put(COL_AGE, age)
            cv.put(COL_ACTIVE, isActive)
        }
        val rv = writableDatabase
            .insert(CUST_TABLE, null, cv)
        return rv != -1L
    }
    fun getAllCustomers() : MutableList<CustomerModel> {
        val queryString = """SELECT * FROM $CUST_TABLE;"""
        val cursor = readableDatabase.rawQuery(queryString,null)
        val rv = (0..(cursor.count - 1)).map{
            cursor.moveToPosition(it)
            CustomerModel(cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                if (cursor.getInt(3) == 1) true else false)
        }.toMutableList()
        cursor.close()
        close() // the database.
        return rv
    }

    constructor(context: Context) :
            this(context, "customer.sqlite", null, 1)

}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with (binding) {
            customerNameEt.setOnClickListener { l ->
                customerAddBtn.isEnabled = customerNameEt.text.length > 0
                        && customerAgeEt.text.length > 0
                Toast.makeText(this@MainActivity,
                        "customer name:" + (l as EditText).text,
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerAgeEt.setOnClickListener { l ->
                customerAddBtn.isEnabled = customerNameEt.text.length > 0
                        && customerAgeEt.text.length > 0
                Toast.makeText(this@MainActivity,
                        "customer age:" + (l as EditText).text,
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerActiveSw.setOnClickListener { l ->
                val sw = l as SwitchCompat
                Toast.makeText(this@MainActivity,
                        "customer active:" + sw.isActivated,
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerShowAllBtn.setOnClickListener {
                val allCustomers = DbHelper(this@MainActivity).getAllCustomers()
                Toast.makeText(this@MainActivity,
                        "customer show all clicked\n$allCustomers",
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerAddBtn.setOnClickListener {
                val cm = CustomerModel(-1,
                    customerNameEt.text.toString(),
                    customerAgeEt.text.toString().toInt(),
                    customerActiveSw.isChecked
                )
                val rv = DbHelper(this@MainActivity).addCustomer(cm)
                Toast.makeText(this@MainActivity,
                        """customer add clicked: 
                            |$cm:
                            |${if(rv)"Success" else "Fail"}
                            |""".trimMargin("|"),
                        Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
}