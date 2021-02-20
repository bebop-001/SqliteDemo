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
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.kana_tutor.sqlltdemo.R
import com.kana_tutor.sqlltdemo.databinding.SearchWindowBinding
import java.lang.Exception

class SearchWindow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle : Int = 0
) : RelativeLayout(context, attrs, defStyle) {
    private var searchOnClickListener : ((view:View, textIn:String) -> Unit)? = null

    private fun hideKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE)
            as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    val binding: SearchWindowBinding
    val searchET:EditText
    val searchClearBTN:ImageButton
    val searchSearchBTN:ImageButton
    // Resources using "flag" type can be multiple types.
    // in our case, hint and text can be an int resource id or a string.
    // determine what we have and return the value.  Would use getType
    // but that requires API 21 or better.
    enum class ResType{INT, TEXT, UNDEF}
    fun TypedArray.getResType(value:Int):Pair<ResType, Any?> =
        when {
            getText(value) != null ->
                ResType.TEXT to getText(value)
            getResourceId(value, -1) != -1 ->
                ResType.INT to getResourceId(value, -1)
            else -> ResType.UNDEF to null
        }

    init {
        Log.d("SearchWindow", "$context:$attrs")
        View.inflate(context, R.layout.search_window, this)
        binding = SearchWindowBinding.inflate(
            LayoutInflater.from(context)
        )
        addView(binding.root)
        searchET = binding.searchET
        searchClearBTN = binding.searchClearBTN
        searchSearchBTN = binding.searchSearchBTN

        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(
                    it, R.styleable.SearchWindow, 0, 0
                )
            with(searchET) {
                run {
                    val (type, value) = typedArray.getResType(R.styleable.SearchWindow_android_hint)
                    if (type == ResType.INT) setHint(value as Int)
                    else if (type == ResType.TEXT) setHint(value as String)
                    else (setHint(R.string.search_window))
                }
                run {
                    val (type, value) = typedArray.getResType(R.styleable.SearchWindow_android_text)
                    if (type == ResType.INT) setText(value as Int)
                    else if (type == ResType.TEXT) setText(value as String)
                }
                try {
                    val colorId = ContextCompat.getColor(
                        context, typedArray.getResourceId(
                            R.styleable.SearchWindow_android_textColor,
                            currentTextColor
                        )
                    )
                    setTextColor(colorId)
                } catch (e: Exception) {
                    // if text color wasn't set, the window hasn't been
                    // given a default text color and we get an exception.
                    Log.d("SearchWindow", """
                        |window id ${"0x%08x".format(searchET.id)}:
                        |set color failed: $e
                        |""".trimMargin("|")
                    )
                }
                val textSizePixels = typedArray.getDimensionPixelSize(
                    R.styleable.SearchWindow_android_textSize,
                    (searchET.textSize + 0.5).toInt()
                )
                setTextSize(COMPLEX_UNIT_PX, textSizePixels.toFloat())
                val ems = typedArray.getInt(
                    R.styleable.SearchWindow_android_ems, -1
                )
                if (ems > 0)
                    setEms(ems)
            }
            typedArray.recycle()
        }
        searchET.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val textIn = v.text.toString()
                    searchOnClickListener?.invoke(this, textIn)
                    hideKeyboard()
                    Log.d("SearchWindow", "text in:${v.text.toString()}")
                    true
                }
                else -> {
                    false
                }
            }
        }
        searchClearBTN.setOnClickListener(
            {searchET.setText("")}
        )
        searchSearchBTN.setOnClickListener(fun (_) {
            val textIn = searchET.text.toString()
            if (text.length > 0) {
                hideKeyboard()
                searchOnClickListener?.invoke(this, textIn)
            }
        })
    }
    @Suppress("unused")
    var search_btn_visibility : Int
        get() = searchClearBTN.visibility
        set(vis) { searchSearchBTN.visibility = vis }
    var text : String
        get() : String  = searchET.text.toString()
        set(str) { searchET.setText(str) }
    fun setSearchOnClick(listener: ((View, String) -> Unit)) {
        searchOnClickListener = listener
    }
}
