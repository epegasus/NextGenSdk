package dev.pegasus.nextgensdk.utils.base.fragment

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import dev.pegasus.nextgensdk.di.DIComponent

abstract class BaseFragment<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentFragment<T>(bindingFactory) {

    protected val diComponent by lazy { DIComponent() }
}