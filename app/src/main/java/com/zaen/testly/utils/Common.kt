package com.zaen.testly.utils

import java.util.*

class Common {
    fun allNotNull(vararg objects: Any?) :Boolean {
        for (o: Any? in objects){
            if (o != null){
                return false
            }
        }
        return true
    }
}