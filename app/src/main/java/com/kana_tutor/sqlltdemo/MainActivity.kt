package com.kana_tutor.sqlltdemo
// SQLite Database for Android - Full Course
// same as course but in kotlin.
// https://www.youtube.com/watch?v=312RhjfetP8
// goal is to setup sqllite db, query, add, and
// delete items.

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.kana_tutor.sqlltdemo.databinding.ActivityMainBinding

data class CustomerModel (
    val id:Int,
    val name:String,
    val age:Int,
    val isActive:Boolean,
        )

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with (binding) {
            customerNameEt.setOnClickListener { l ->
                Toast.makeText(this@MainActivity,
                        "customer name:" + (l as EditText).text,
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerAgeEt.setOnClickListener { l ->
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
            customerShowAllBtn.setOnClickListener { l ->
                val btn = l as Button
                Toast.makeText(this@MainActivity,
                        "customer show all clicked",
                        Toast.LENGTH_SHORT
                ).show()
            }
            customerAddBtn.setOnClickListener { l ->
                val button = l as Button
                Toast.makeText(this@MainActivity,
                        "customer add clicked",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}