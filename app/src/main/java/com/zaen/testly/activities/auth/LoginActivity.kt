package com.zaen.testly.activities.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.zaen.testly.R
import java.util.*


class LoginActivity : Auth() {
    companion object {
        val TAG = "LoginActivity"
    }

    @BindView(R.id.login_greeting)
    lateinit var greeting: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        val greetingsList = resources.getStringArray(R.array.login_greetings)
        greeting.setText(greetingsList[Random().nextInt(greetingsList.size)])

    }

    override fun onStart() {
        super.onStart()
        val accountGoogle = GoogleSignIn.getLastSignedInAccount(this)
        val accountFirebase = mAuth?.currentUser
        if ((accountGoogle != null)or(accountFirebase != null)){
            setResult(RESULT_OK, intent)
            finish()
        }
    }
    fun onForgotPassword(view: View){
        // Place holder & backdoor
        onSuccess()
    }
    fun onCreateAccount(view:View){
        setResult(RC_SIGN_UP,intent)
        finish()
    }
    fun onGoogleAuth(view:View){
        googleAuth()
    }
    fun onFacebookAuth(view:View){
        facebookAuth()
    }
    fun onTwitterAuth(view:View){
        twitterAuth()
    }
    fun onGithubAuth (view:View){
        githubAuth()
    }
    fun onEmailAuth (view:View){
        emailAuth()
    }

    override fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    override fun isPasswordValid(password: String): Boolean {
        return true
    }



}
