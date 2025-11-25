package dev.pegasus.nextgensdk.utils.base.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import dev.pegasus.nextgensdk.di.DIComponent

abstract class BaseActivity<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentActivity<T>(bindingFactory) {
    protected val diComponent by lazy { DIComponent() }
}