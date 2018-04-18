package com.zaen.testly.data

import android.net.Uri
import com.google.firebase.firestore.FieldValue


data class DevChatMessageData(val message:String, val sender: DevChatUserData, val createdAt: FieldValue)
data class DevChatUserData(val userId: String, val displayName:String, val profileUrl: Uri?)