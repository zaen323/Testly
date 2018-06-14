package com.zaen.testly.fragments.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.zaen.testly.utils.LogUtils
import me.yokeyword.fragmentation.SupportFragment

/*
* layoutRes @onCreateView(3)
* retainInstance == true DEFAULT
* NO unbinder
* */

abstract class BaseFragment : SupportFragment(){
    protected var unbinder: Unbinder? = null
    protected var activity: AppCompatActivity? = null
    var layoutRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtils.log(this, 2,"onCreate")
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogUtils.log(this, 2,"onCreateView")
        return if (layoutRes != null) {
            val view = inflater.inflate(layoutRes!!, container, false)
            unbinder = ButterKnife.bind(this, view)
            view
        } else {
            LogUtils.failure(this,5,"No layout resource specified, not inflating.")
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.log(this, 2,"onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogUtils.log(this, 2,"onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as AppCompatActivity?
    }

    override fun onStart() {
        super.onStart()
        LogUtils.log(this, 2,"onStart")
    }

    override fun onDestroyView() {
        LogUtils.log(this, 2,"onDestroyView")
        super.onDestroyView()
        unbinder?.unbind()
    }

}