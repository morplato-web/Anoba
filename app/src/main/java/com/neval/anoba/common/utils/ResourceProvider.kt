package com.neval.anoba.common.utils

import android.content.Context
import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

class ResourceProviderImpl(private val context: Context) : ResourceProvider {
    override fun getString(resId: Int): String = context.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)
}
