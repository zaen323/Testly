package com.zaen.testly

import com.google.firebase.firestore.DocumentReference

class Global(){
    companion object {
        var userinfoRef: DocumentReference? = null
        const val KEY_ACTION_INFORM_LIFECYCLE_ACTIVITY = "fragment_lifecycle"
        const val KEY_ACTION_INFORM_LIFECYCLE_FRAGMENT = "fragment_lifecycle"
    }
}