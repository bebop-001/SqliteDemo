package com.kana_tutor.sqlltdemo
// SQLite Database for Android - Full Course
// same as course but in kotlin.
// https://www.youtube.com/watch?v=312RhjfetP8
// goal is to setup sqllite db, query, add, and
// delete items.

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doAfterTextChanged
import com.kana_tutor.sqlltdemo.databinding.ActivityMainBinding


data class CustomerModel(
    val id: Int,
    val name: String,
    val age: Int,
    val isActive: Boolean,
)
class DbHelper(
    context: Context,
    name: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
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
     * Called when the database needs to be upgraded. Use this method
     * to drop tables, add tables, or do anything else needed to
     * upgrade to the new schema version.
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     * This method executes within a transaction.  If an exception is
     * thrown, all changes are automatically rolled back.
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
        with(customerModel) {
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
        val cursor = readableDatabase.rawQuery(queryString, null)
        val rv = (0..(cursor.count - 1)).map{
            cursor.moveToPosition(it)
            CustomerModel(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                if (cursor.getInt(3) == 1) true else false
            )
        }.toMutableList()
        cursor.close()
        return rv
    }
    fun getCustomer(name: String, age: Int) : CustomerModel? {
        val queryString = """SELECT * FROM $CUST_TABLE
            |WHERE $COL_NAME = '$name' AND $COL_AGE = $age;
            |""".trimMargin()
        val cursor = readableDatabase.rawQuery(queryString, null)
        val matches = mutableListOf<CustomerModel>()
        for(i in 0..cursor.count - 1) {
            cursor.moveToPosition(i)
            matches.add(
                CustomerModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    if (cursor.getInt(3) == 1) true else false
                )
            )
        }
        cursor.close()
        if (matches.size > 1)
            Log.d(
                "DbHelper",
                """getCustomer: multiple matches for \"$name:$age\"
                    |  Found:$matches
                """.trimMargin("|")
            )
        return if (matches.size == 1) matches[0] else null
    }
    fun deleteCustomer(customer: CustomerModel) =
        deleteCustomer(customer.name, customer.age)
    fun deleteCustomer(name: String, age: Int) : Boolean {
        val db = this.writableDatabase
        val queryString = """DELETE FROM $CUST_TABLE
            |WHERE $COL_NAME = '$name' 
            |AND $COL_AGE = $age;
            |""".trimMargin()
        val nRecords = db.rawQuery("SELECT * FROM $CUST_TABLE;", null).count
        val x = db.rawQuery(queryString, null).count
        val nRecords2 = db.rawQuery("SELECT * FROM $CUST_TABLE;", null).count
        Log.d("DbHelper", "deleteCustomer:${nRecords - nRecords2} records deleted.")
        db.close()
        return nRecords != nRecords2
    }

    constructor(context: Context) :
            this(context, "customer.sqlite", null, 1)

}

fun hideSoftKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(
        Activity.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(
        activity.currentFocus!!.windowToken, 0
    )
}


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        fun List<CustomerModel>.updateListView() {
            binding.customerLv.adapter = ArrayAdapter<CustomerModel>(
                this@MainActivity.applicationContext,
                android.R.layout.simple_list_item_1,
                this
            )
        }

        fun hideKeyboard() {
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(
                currentFocus!!.windowToken, 0
            )
        }
        fun enableButtons() {
            val name = binding.customerNameEt.text.toString()
            val ageString = binding.customerAgeEt.text.toString()
            binding.customerDeleteBtn.isEnabled =
                ageString.length > 0 && name.length > 0
                    && DbHelper(this@MainActivity.applicationContext)
                    .getCustomer(name, ageString.toInt()) != null
            binding.customerAddBtn.isEnabled =
                ageString.length > 0 && name.length > 0
                    && DbHelper(this@MainActivity.applicationContext)
                    .getCustomer(name, ageString.toInt()) == null
        }
        run {
            val db = DbHelper(this@MainActivity.applicationContext)
            db.getAllCustomers().updateListView()
            db.close()
        }
        setContentView(binding.root)
        enableButtons()
        with(binding) {
            // name/age edit text to listeners so they check input on each
            // key touch and update enable/disable for buttons.
            customerNameEt.doAfterTextChanged { l ->
                Log.d("xxx", "$l:\"${customerNameEt.text}\"")
                enableButtons()
            }
            customerAgeEt.doAfterTextChanged { l ->
                Log.d("xxx", "$l:\"${customerAgeEt.text}\"")
                enableButtons()
            }
            // not really necessary since we just read state of
            // switch in customerAdd.
            customerActiveSw.setOnClickListener { l ->
                val sw = l as SwitchCompat
                Toast.makeText(
                    this@MainActivity.applicationContext,
                    "customer active:" + sw.isActivated,
                    Toast.LENGTH_SHORT
                ).show()
            }
            customerShowAllBtn.setOnClickListener {
                val db = DbHelper(this@MainActivity.applicationContext)
                val allCustomers = db.getAllCustomers()
                db.close()
                allCustomers.updateListView()
            }
            customerAddBtn.setOnClickListener {
                val db = DbHelper(this@MainActivity.applicationContext)
                val name = customerNameEt.text.toString()
                val age = customerAgeEt.text.toString().toInt()
                if (db.getCustomer(name, age) != null)
                    Toast.makeText(
                        this@MainActivity,
                        "Customer is already in database.",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    val cm = CustomerModel(-1, name, age, customerActiveSw.isChecked)
                    if (db.addCustomer(cm)) {
                        db.getAllCustomers().updateListView()
                        binding.customerNameEt.setText("")
                        binding.customerAgeEt.setText("")
                    }
                    else Log.d("customerAdd", "Failed to add:$cm")
                }
                DbHelper(this@MainActivity.applicationContext)
                    .getAllCustomers()
                    .updateListView()
                db.close()
                hideKeyboard()
            }
            customerDeleteBtn.setOnClickListener {
                val db = DbHelper(this@MainActivity.applicationContext)
                if (db.deleteCustomer(
                        customerNameEt.text.toString(),
                        customerAgeEt.text.toString().toInt()
                    )
                ) {
                    customerNameEt.setText("")
                    customerAgeEt.setText("")
                }
                db.getAllCustomers().updateListView()
                enableButtons()
                hideKeyboard()
            }
            customerLv.setOnItemClickListener { parent, view, position, id ->
                val customer = parent.getItemAtPosition(position) as CustomerModel
                customerNameEt.setText(customer.name)
                customerAgeEt.setText(customer.age.toString())
                enableButtons()
                hideKeyboard()
            }
        }
    }
}