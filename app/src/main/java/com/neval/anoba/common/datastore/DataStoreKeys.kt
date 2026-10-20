package com.neval.anoba.common.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object DataStoreKeys {
    val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me_key")
    val TUTORIAL_COMPLETED_KEY = booleanPreferencesKey("tutorial_completed_key")
}
