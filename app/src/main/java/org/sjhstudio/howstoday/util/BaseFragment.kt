package org.sjhstudio.howstoday.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext

open class BaseFragment: Fragment(), View.OnClickListener, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Main

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {}
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            BaseFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    override fun onClick(v: View?) {}

}