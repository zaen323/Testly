package com.zaen.testly.auth.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zaen.testly.R
import com.zaen.testly.auth.SignupUserinfo
import com.zaen.testly.utils.InformUtils
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.form_signup_userinfo.*
import kotterknife.bindView
import java.util.*

class SignupActivity : AuthActivity(), SignupUserinfo.ExceptionHandler{
    companion object {
        const val AUTH_EMAIL = 1
        const val AUTH_GOOGLE = 2
        const val AUTH_FACEBOOK = 3
        const val AUTH_TWITTER = 4
        const val AUTH_GITHUB = 5
    }
    val greeting: TextView by bindView(R.id.signup_greeting)

    var authMethod : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutRes = R.layout.activity_signup
        super.onCreate(savedInstanceState)
        request = RC_SIGN_UP

        // Greetings
        val greetingsList = resources.getStringArray(R.array.signup_greetings)
        greeting.text = greetingsList[Random().nextInt(greetingsList.size)]
        userinfo = SignupUserinfo(this,
                input_username,edit_username,
                input_fullname_last,edit_fullname_last,
                input_fullname_first,edit_fullname_first,
                spinner_signup_school,spinner_signup_grade,spinner_signup_class,
                error_signup_school_required,error_signup_grade_required,error_signup_class_required
        )
                .build()
    }


    fun onSocialButtonSelected(view:View){
        signup_emailPassword.visibility = View.GONE
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,resources.getDimension(R.dimen.medium_spacing).toInt(),0,0)
        signup_form_info.layoutParams = params
        btn_mail.setBorderWidth(0)
        btn_google.setBorderWidth(0)
        btn_facebook.setBorderWidth(0)
        btn_twitter.setBorderWidth(0)
        btn_github.setBorderWidth(0)
        when(view.id){
            btn_mail.id -> {
                signup_emailPassword.visibility = View.VISIBLE
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0,0,0,0)
                signup_form_info.layoutParams = params
                btn_mail.setBorderWidth(5)
                authMethod = AUTH_EMAIL
            }
            btn_google.id -> {
                btn_google.setBorderWidth(5)
                authMethod = AUTH_GOOGLE
            }
            btn_facebook.id -> {
                btn_facebook.setBorderWidth(5)
                authMethod = AUTH_FACEBOOK

            }
            btn_twitter.id -> {
                btn_twitter.setBorderWidth(5)
                authMethod = AUTH_TWITTER

            }
            btn_github.id -> {
                firebaseAuth?.notImplementedBuilder?.build()?.show()
                //btn_github.setBorderWidth(5)
                //authMethod = AUTH_GITHUB
            }
        }

    }
    fun onSignUp(view:View){
        when(authMethod){
            null -> {
                InformUtils(this).snackyWarning("Please tap one of the round buttons to select sign up method.")
            }
            AUTH_EMAIL -> {
                userinfo!!.checkInfoField()
                emailAuth(input_email,input_password)
            }
            AUTH_GOOGLE -> {
                if (userinfo!!.checkInfoField()) {
                    googleAuth()
                }
            }
            AUTH_FACEBOOK -> {
                if (userinfo!!.checkInfoField()) {
                    facebookAuth()
                }
            }
            AUTH_TWITTER -> {
                if (userinfo!!.checkInfoField()) {
                    twitterAuth()
                }
            }
            AUTH_GITHUB -> {
                githubAuth() // not implemented YET
//                if (checkInfoField()) {
//                    checkInfoField()
//                    githubAuth()
//                }
            }
        }
    }
    override fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    override fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    override fun onException(error: String, e: Exception){
        InformUtils(this).snackyException(error,e)
    }

}
