package com.exam.dcgstoryapp.view.story.email

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.exam.dcgstoryapp.R
import android.util.Patterns

class EmailEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateEmail(input: CharSequence?) {
        error = if (input.isNullOrEmpty()) {
            context.getString(R.string.error_email_empty)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            context.getString(R.string.error_invalid_email)
        } else {
            null
        }
    }
}