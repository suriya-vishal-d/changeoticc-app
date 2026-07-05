package com.app.re.util

object Constants {
    const val BACKEND_URL = "https://resume-editor-backend-production.up.railway.app"
    const val GITHUB_AUTH_URL = "$BACKEND_URL/oauth2/authorization/github"
    const val AUTH_SUCCESS_URL = "$BACKEND_URL/auth/success"
    const val AUTH_APP_REDIRECT_URI = "com.app.re://auth/success"
}
