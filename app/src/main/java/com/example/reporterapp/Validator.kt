package com.example.reporterapp

import java.util.regex.Matcher
import java.util.regex.Pattern

class Validator {

    companion object {

        fun isAlphaNumeric(s: String): Boolean {
            val pattern = "^[a-zA-Z0-9- ]*$"
            return s.matches(pattern.toRegex())
        }

        fun isNotNullNotEmpty(text: String): Boolean {
            return (text.replace(" ", "").isNotEmpty() && "null" != text)
        }


        fun isValidPassword(password: String): Boolean {
            val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$"
            return password.matches(passwordPattern.toRegex())
        }

    }


}