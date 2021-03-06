package com.zaen.testly.auth

import android.app.Activity
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.jaredrummler.materialspinner.MaterialSpinner
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import com.zaen.testly.R
import com.zaen.testly.TestlyFirestore
import com.zaen.testly.TestlyUser
import com.zaen.testly.data.UserData
import com.zaen.testly.utils.LogUtils
import java.util.*

/**
 * Created by zaen on 3/26/18.
 */

open class SignupUserinfo (val context: Activity,
                           val usernameInput: TextInputLayout, val usernameEdit: TextInputEditText,
                           val fullnameLastInput: TextInputLayout, val fullnameLastEdit: TextInputEditText,
                           val fullnameFirstInput: TextInputLayout, val fullnameFirstEdit: TextInputEditText,
                           val schoolSpinner: SearchableSpinner, val gradeSpinner: MaterialSpinner, val classSpinner: MaterialSpinner,
                           val errorSchool: TextView, val errorGrade: TextView, val errorClass: TextView){
    var schoolSnapList = ArrayList<DocumentSnapshot>()
    var schoolIdList = ArrayList<String>()
    var schoolNameList = ArrayList<String>()
    var gradeSnapList = ArrayList<DocumentSnapshot>()
    var gradeNameList = ArrayList<String>()
    var classSnapList = ArrayList<DocumentSnapshot>()
    var classNameList = ArrayList<String>()
    // Userinfo Store
    var usernameStr: String? = null
    var firstNameStr: String? = null
    var lastNameStr: String? = null
    var schoolNameStr: String? = null
    var schoolIdStr: String? = null
    var gradeStr: String? = null
    var classStr: String? = null
    // Register Flags
    var isUpdateProfileCompleted = false
    var isFirestoreCompleted = false
    //var isAnalyticsCompleted = false

//    val context = context
    var mExceptionHandler: ExceptionHandler? = null
    private var mSuccessListener: SuccessListener? = null

    init{
        schoolSpinner.setTitle("Select School")
        schoolSpinner.setPositiveButton("OK")
        gradeSpinner.isEnabled = false
        classSpinner.isEnabled = false
        if (context is ExceptionHandler) {
            mExceptionHandler = context
        }
        if (context is SuccessListener) {
            mSuccessListener = context
        }
    }
    fun build():SignupUserinfo{
        TestlyFirestore(this).getDocuments(FirebaseFirestore.getInstance().collection("schools"),object: TestlyFirestore.CollectionSingleListener{
            override fun handleListener(listener: ListenerRegistration?) {
            }

            override fun onDocumentsUpdate(path: Query, snapshots: QuerySnapshot?, exception: Exception?) {
                if (snapshots != null){
                    schoolSnapList.clear()
                    schoolNameList.clear()
                    for (snapshot in snapshots){
                        schoolSnapList.add(snapshot)
                        schoolIdList.add(snapshot.id)
                        schoolNameList.add(snapshot.get("name") as String)
                    }
                    val schoolSpinnerArrayAdapter = ArrayAdapter<String>(context, R.layout.view_item_spinner_schools_signup,schoolNameList)
                    schoolSpinner.adapter = schoolSpinnerArrayAdapter
                } else {
                    LogUtils.failure(this,5,"Error getting school names.", exception as Exception)

                    mExceptionHandler?.onException(
                            "An error occurred trying to get the list of schools.", exception)
                    schoolSpinner.isEnabled = false
                    gradeSpinner.isEnabled = false
                    classSpinner.isEnabled = false
                }
            }
        })

        // School Selected
        schoolSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                schoolNameStr = schoolNameList[position]
                schoolIdStr = schoolIdList[position]
                TestlyFirestore(this).getDocuments(schoolSnapList[position].reference.collection("grades"),object: TestlyFirestore.CollectionSingleListener{
                    override fun handleListener(listener: ListenerRegistration?) {
                    }

                    override fun onDocumentsUpdate(path: Query, snapshots: QuerySnapshot?, exception: Exception?) {
                        if (snapshots != null){
                            gradeSnapList.clear()
                            gradeNameList.clear()
                            for (snapshot in snapshots){
                                gradeSnapList.add(snapshot)
                                gradeNameList.add(snapshot.get("name") as String)
                            }
                            val gradeSpinnerArrayAdapter = ArrayAdapter<String>(context,R.layout.view_item_spinner_schools_signup,gradeNameList)
                            gradeSpinner.setAdapter(gradeSpinnerArrayAdapter)
                            gradeSpinner.isEnabled = true
                        } else {
                            LogUtils.failure(this, 5, "Error getting school names.", exception as Exception)
                            mExceptionHandler?.onException(
                                    "An error occurred trying to get the list of grades.",exception)
                            gradeSpinner.isEnabled = false
                            classSpinner.isEnabled = false
                        }
                    }
                })
            }
        }
        // Grade selected
        gradeSpinner.setOnItemSelectedListener { view, position, id, item ->
            gradeStr = gradeNameList[position]
            TestlyFirestore(this).getDocuments(gradeSnapList[position].reference.collection("classes"),object: TestlyFirestore.CollectionSingleListener{
                override fun handleListener(listener: ListenerRegistration?) {
                }

                override fun onDocumentsUpdate(path: Query, snapshots: QuerySnapshot?, exception: Exception?) {
                    if (snapshots != null) {
                        classSnapList.clear()
                        classNameList.clear()
                        for (snapshot in snapshots){
                            classSnapList.add(snapshot)
                            classNameList.add(snapshot.get("name") as String)
                        }
                        val classSpinnerArrayAdapter = ArrayAdapter<String>(context,R.layout.view_item_spinner_schools_signup,classNameList)
                        classSpinner.setAdapter(classSpinnerArrayAdapter)
                        classSpinner.isEnabled = true
                    } else {
                        LogUtils.failure(this, 5, "Error getting school names.", exception as Exception)
                        mExceptionHandler?.onException("An error occurred trying to get the list of classes.",exception)
                        classSpinner.isEnabled = false
                    }
                }
            })
        }
        // Class selected
        classSpinner.setOnItemSelectedListener { view, position, id, item -> classStr = classNameList[position] }
        return this
    }
    fun checkInfoField():Boolean{
        usernameInput.error = null
        fullnameFirstInput.error = null
        fullnameLastInput.error = null
        usernameStr = usernameEdit.text.toString()
        firstNameStr = fullnameFirstEdit.text.toString()
        lastNameStr = fullnameLastEdit.text.toString()
        errorSchool.visibility = View.GONE
        errorGrade.visibility = View.GONE
        errorClass.visibility = View.GONE


        var cancel = false
        var focusView: View? = null

        // For the first view to receive focusView, hence the order.

        // Check Class
        if (TextUtils.isEmpty(classStr)){
            errorClass.visibility = View.VISIBLE
            focusView = classSpinner
            cancel = true
        }

        // Check Grade
        if (TextUtils.isEmpty(gradeStr)){
            errorGrade.visibility = View.VISIBLE
            focusView = gradeSpinner
            cancel = true
        }

        // Check School
        if (TextUtils.isEmpty(schoolNameStr)){
            errorSchool.visibility = View.VISIBLE
            focusView = schoolSpinner
            cancel = true
        }

        // Check Last name
        if (TextUtils.isEmpty(lastNameStr)){
            fullnameLastInput.error = context.resources.getString(R.string.error_field_required)
            focusView = fullnameLastEdit
            cancel = true
        }
        // Check first name
        if (TextUtils.isEmpty(firstNameStr)){
            fullnameFirstInput.error = context.resources.getString(R.string.error_field_required)
            focusView = fullnameFirstEdit
            cancel = true
        }
        // Check username
        if (TextUtils.isEmpty(usernameStr)){
            usernameInput.error = context.resources.getString(R.string.error_field_required)
            focusView = usernameEdit
            cancel = true
        }
        else if (usernameStr!!.length > usernameInput.counterMaxLength){
            usernameInput.error = context.resources.getString(R.string.error_max_length_exceeded)
            focusView = usernameEdit
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
            return false
        } else {
            return true
        }
    }
    // Register userinfo
    fun registerUserInfo(){
        val user = TestlyUser(this).currentUser!!
        val db = FirebaseFirestore.getInstance()

        // Username
        val request = UserProfileChangeRequest.Builder()
                .setDisplayName(usernameStr)
                .build()
        TestlyUser(this). updateProfile(request,object: TestlyUser.ProfileUpdateListener{
            override fun onProfileUpdated(successful: Boolean, exception: Exception?) {
                if (successful){
                    onCompleteListenerForRegisterInfo(1)
                } else {
                    LogUtils.failure(this, 5, "Uploading username.", exception!!)
                    mExceptionHandler?.onException("Uploading username to the database failed.", exception)
                }
            }
        })

        // Userinfo (Username, fullname, school, grade, class)
        val userinfo = UserData(
                user.uid,
                com.zaen.testly.utils.CommonUtils.getTimestamp(),
                user.email!!,
                user.photoUrl.toString(),
                false, false, false,
                usernameStr!!,
                firstNameStr!!, lastNameStr!!,
                schoolIdStr!!, schoolNameStr!!,
                gradeStr!!, gradeStr!!,
                classStr!!, classStr!!
        )

        TestlyFirestore(this).addDocumentToCollection(db.collection("users"),user.uid,userinfo,object: TestlyFirestore.UploadToCollectionListener{
            override fun onDocumentUpload(path: Query, reference: DocumentReference?, exception: Exception?) {
                if (reference != null){
                    onCompleteListenerForRegisterInfo(2)
                } else {
                    LogUtils.failure(this, 5, "Uploading userinfo.", exception!!)
                    mExceptionHandler?.onException("Uploading userinfo to database failed.", exception!!)
                }
            }
        })

        // Analytics for school
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        mFirebaseAnalytics.setUserProperty(
                "Prop1", schoolIdStr
        )
        mFirebaseAnalytics.setUserProperty(
                "Prop2", gradeStr
        )
    }
    //
    fun onCompleteListenerForRegisterInfo(flag: Int){
        when (flag){
            1 -> { isUpdateProfileCompleted = true }
            2 -> { isFirestoreCompleted = true }
        //3 -> { isAnalyticsCompleted = true }
        }
        if (isUpdateProfileCompleted && isFirestoreCompleted){
            mSuccessListener?.onSuccess()
        }
    }

    interface ExceptionHandler{
        fun onException(error: String, e: Exception)
    }
    interface SuccessListener{
        fun onSuccess()
    }
}