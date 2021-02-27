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

@file:Suppress("PropertyName", "unused", "PrivatePropertyName")

package com.kana_tutor.sqlltdemo
// SQLite Database for Android - Full Course
// same as course but in kotlin.
// https://www.youtube.com/watch?v=312RhjfetP8
// goal is to setup sqlite db, query, add, and
// delete items.

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.doAfterTextChanged
import com.kana_tutor.sqlltdemo.databinding.ActivityMainBinding
import com.kana_tutor.utils.copyFileFromAssets
import com.kana_tutor.utils.searchwindow.SearchWindow
import java.io.File
import java.lang.StringBuilder


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
    private val CUST_TABLE = "CUSTOMER_TABLE"
    private val COL_NAME = "NAME"
    private val COL_AGE = "AGE"
    private val COL_ACTIVE = "ACTIVE"
    private val COL_ID = "ID"

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        try {
            val createSqlTable =
                """CREATE TABLE IF NOT EXISTS $CUST_TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_AGE INT,
                $COL_ACTIVE BOOL
                )"""
            db!!.execSQL(createSqlTable)
        }
        catch (e: SQLiteException) {
            val asList = e.message?.split("\n")?.toList()
                ?: listOf("unknown")
            val errors = asList.filter { "^(Error Code|Caused By)".toRegex(RegexOption.IGNORE_CASE).find(it) != null }
            throw SQLiteException("""SQLiteOpenHelper:onCreate:
                |${errors.joinToString("\n")}
                |""".trimMargin("|"))
        }
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

    fun addCustomer(customerModel: CustomerModel): Boolean {
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

    fun getAllCustomers(): MutableList<CustomerModel> {
        val queryString = """SELECT * FROM $CUST_TABLE;"""
        val cursor = readableDatabase.rawQuery(queryString, null)
        val rv = (0 until cursor.count).map {
            cursor.moveToPosition(it)
            CustomerModel(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getInt(3) == 1
            )
        }.toMutableList()
        cursor.close()
        return rv
    }
    private fun getCustomersFromSql(sqlQuery:String) : MutableList<CustomerModel> {
        val rv: MutableList<CustomerModel>
        val cursor = readableDatabase.rawQuery(sqlQuery, null)
        rv = (0 until cursor.count).map {
            cursor.moveToPosition(it)
            CustomerModel(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getInt(3) == 1
            )
        }.toMutableList()
        return rv
    }
    fun getCustomersSimpleSearch(customerName: String
    ) : MutableList<CustomerModel> {
        val sqlQuery = """
            SELECT * from CUSTOMER_TABLE
            WHERE NAME LIKE '%$customerName%';""".trimIndent()
        return getCustomersFromSql(sqlQuery)
    }
    fun getCustomers(sqlRegex:String) : MutableList<CustomerModel> {
        val sqlQuery = """
            SELECT * from CUSTOMER_TABLE
            WHERE $sqlRegex;
        """.trimIndent()

        return getCustomersFromSql(sqlQuery)
    }
    fun getCustomer(name: String, age: Int) : CustomerModel? {
        val queryString = """SELECT * FROM $CUST_TABLE
            |WHERE $COL_NAME = '$name' AND $COL_AGE = $age;
            |""".trimMargin()
        val cursor = readableDatabase.rawQuery(queryString, null)
        val matches = mutableListOf<CustomerModel>()
        for(i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            matches.add(
                CustomerModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3) == 1
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
    private
    fun deleteCustomer(customer: CustomerModel) =
        deleteCustomer(customer.name, customer.age)
    @SuppressLint("Recycle")
    fun deleteCustomer(name: String, age: Int) : Boolean {
        val db = this.writableDatabase
        val queryString = """DELETE FROM $CUST_TABLE
            |WHERE $COL_NAME = '$name' 
            |AND $COL_AGE = $age;
            |""".trimMargin()
        val nRecords = db.rawQuery("SELECT * FROM $CUST_TABLE;", null).count
        db.rawQuery(queryString, null).count
        val nRecords2 = db.rawQuery("SELECT * FROM $CUST_TABLE;", null).count
        Log.d("DbHelper", "deleteCustomer:${nRecords - nRecords2} records deleted.")
        db.close()
        return nRecords != nRecords2
    }

    constructor(context: Context) :
            this(context, "customer.sqlite", null, 1)

}

fun Activity.webViewAlert (webView : WebView, title:String) {
    androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle(title)
        .setView(webView)
        .setNegativeButton(R.string.done,
            {dialogInterface, _ -> dialogInterface.dismiss()})
        .setCancelable(false)
        .show()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        fun List<CustomerModel>.updateListView() {
            binding.customerLv.adapter = ArrayAdapter(
                this@MainActivity.applicationContext,
                R.layout.tv,
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
                ageString.isNotEmpty() && name.isNotEmpty()
                    && DbHelper(this@MainActivity.applicationContext)
                    .getCustomer(name, ageString.toInt()) != null
            binding.customerAddBtn.isEnabled =
                ageString.isNotEmpty() && name.isNotEmpty()
                    && DbHelper(this@MainActivity.applicationContext)
                    .getCustomer(name, ageString.toInt()) == null
        }
        var searchWindow : SearchWindow? = null
        var currentSearchType = 0
        val SIMPLE_SEARCH = 0
        val SQL_SEARCH = 1
        fun setSearchWinHint() =
            searchWindow?.searchET?.setHint(
                if (currentSearchType == SQL_SEARCH) "Long press for help"
                else "Simple Name Search"
            )

        val prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        // String that determines search is simple or sql.  Set at startup in onCreate
        // from user prefs.  Changed by user with customer_search_type_spinner spinner.
        class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                Log.d("Spinner Activity", "onItemSelected:%d:0x%08x".format(pos, id))
                val newSelection = pos
                prefs.edit().putInt("currentSearchType", newSelection).apply()
                currentSearchType = newSelection
                setSearchWinHint()
                Log.d("spinner update", "spinner val: $newSelection")
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("Spinner Activity", "onNothingSelected")
                // Another interface callback
            }
        }

        fun showSearchHelp() : Boolean {
            val webView = WebView(this)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    Log.i("WebView", "Attempting to load URL: $url")
                    // if external link, start with a browser.  Should only hit
                    // from the about google play link.
                    if (url.startsWith("https://") || url.startsWith("http://")) {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }
                    else
                        view.loadUrl(url)
                    return true
                }
            }
            webView.loadUrl("file:///android_asset/html/SqlRegexHelp.html")
            webViewAlert(webView, "SQL Search Help")
            return true
        }
        val appHome = File(filesDir, "../")
        val databaseDir = File(appHome, "/databases")
        if (prefs.getLong("firstRunTimestamp", 0L) == 0L) {
            copyFileFromAssets(
                "db/customer.sqlite",
                "$databaseDir/customer.sqlite",
                true)
            prefs.edit()
                .putLong("firstRunTimestamp", System.currentTimeMillis())
                .commit()
            Log.d("onCreate", "copyFileFromAssets complete")
        }
        setContentView(binding.root)
        currentSearchType = prefs.getInt("currentSearchType", 0)
        Log.d("startup", "spinner val: $currentSearchType")
        run {
            Log.d("onCreate", "Start db")
            val db = DbHelper(this@MainActivity.applicationContext)
            db.getAllCustomers().updateListView()
            db.close()
        }
        enableButtons()
        with(binding) {
            // Search window callbacks.
            searchWindow = customerSearch
            customerSearch.setSearchOnClick{view : View, textIn : String ->
                Log.d("name search", "setSearchOnClick text = \"$textIn")
                var selectedCustomers : MutableList<CustomerModel>? = null
                try {
                    val db = DbHelper(this@MainActivity.applicationContext)
                    val sqlSearch = currentSearchType == SQL_SEARCH
                    selectedCustomers =
                        if (sqlSearch) db.getCustomers(textIn)
                        else db.getCustomersSimpleSearch(textIn)
                    db.close()
                }
                catch (e: SQLiteException) {
                    val errorsList = e.message?.split("\n")!!.toList()
                    var i = 0
                    val sqlError = StringBuilder("SqlError: ")
                    while (i < errorsList.size && !errorsList[i].startsWith("#")) {
                        sqlError.append(errorsList[i]).append("\n")
                        i++
                    }
                    Toast.makeText(
                        this@MainActivity.applicationContext,
                        sqlError.toString(), Toast.LENGTH_LONG)
                        .show()
                }

                selectedCustomers?.updateListView()
            }
            customerSearch.searchET.setOnLongClickListener {
                Log.d("onlongclick", "clicked")
                showSearchHelp()
                true
            }
            customerSearch.setSearchOnTouch { view: View, textIn: String ->
                Log.d("name search", "setSearchOnTouch text = \"$textIn")
            }
            val spinAdapter = ArrayAdapter<String>(
                this@MainActivity.applicationContext, R.layout.tv,
                resources.getStringArray(R.array.search_types)
            )
            spinAdapter.setDropDownViewResource(R.layout.tv)
            customerSearchTypeSpinner.adapter = spinAdapter
            customerSearchTypeSpinner.onItemSelectedListener =
                SpinnerActivity()
            // Set the initial value shown in the selector HERE!.
            // This must be after adapter is set.
            customerSearchTypeSpinner.setSelection(currentSearchType)
            setSearchWinHint()
            // name/age edit text to listeners so they check input on each
            // key touch and update enable/disable for buttons.
            customerNameEt.doAfterTextChanged  { enableButtons() }
            customerAgeEt.doAfterTextChanged   { enableButtons() }
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
            customerLv.setOnItemClickListener { parent, _, position, _ ->
                val customer = parent.getItemAtPosition(position) as CustomerModel
                customerNameEt.setText(customer.name)
                customerAgeEt.setText(customer.age.toString())
                customerActiveSw.isChecked = customer.isActive
                enableButtons()
                hideKeyboard()
            }
        }
    }
}
