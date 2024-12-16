package com.exam.dcgstoryapp.view.pass

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.exam.dcgstoryapp.R

class PasswordEditText : AppCompatEditText {

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
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePassword(input: CharSequence?) {
        error = if (input.isNullOrEmpty()) {
            context.getString(R.string.error_password_empty)
        } else if (input.length < 8) {
            context.getString(R.string.error_password_too_short)
        } else {
            null
        }
    }
}
