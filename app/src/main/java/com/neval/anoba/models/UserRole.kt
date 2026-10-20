package com.neval.anoba.models

import com.neval.anoba.R

enum class UserRole(@get:Suppress("unused") val labelResId: Int) {
    ADMIN(R.string.role_admin),
    MUHTAR(R.string.role_muhtar),
    MODERATOR(R.string.role_moderator),
    MEMBER(R.string.role_member),
    GUEST(R.string.role_guest)
}