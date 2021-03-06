package com.zaen.testly.settings.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import com.zaen.testly.R
import com.zaen.testly.base.fragments.BaseFragment

/**
 * Created by zaen on 3/1/18.
 */
class DeveloperSettingsMainFragment : BaseFragment() {
    lateinit var toolbar : Toolbar
    private var mListener: FragmentClickListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentClickListener){
            mListener = context
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?  {
        layoutRes = R.layout.fragment_settings_developer_main
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }



    interface FragmentClickListener{
        fun onFragmentCalled(newFragment: BaseFragment, title:String)
    }

}