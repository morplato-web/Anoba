package com.neval.anoba.common.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            if (screenClass != null) {
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logButtonClick(buttonName: String) {
        val bundle = Bundle().apply {
            putString("button_name", buttonName)
        }
        firebaseAnalytics.logEvent("button_click", bundle)
    }

    fun logEvent(eventName: String, params: (Bundle.() -> Unit)? = null) {
        val bundle = params?.let {
            Bundle().apply(it)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
