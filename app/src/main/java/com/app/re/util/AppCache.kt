package com.app.re.util

import com.app.re.data.model.ParseResponse

/**
 * Process-scoped singleton to share parsed portfolio data across ViewModels
 * without re-calling the parse API.
 *
 * SetupViewModel writes here after a successful parse.
 * EditViewModel reads from here first before falling back to the network.
 */
object AppCache {

    @Volatile
    var parseResponse: ParseResponse? = null

    fun clear() {
        parseResponse = null
    }
}
