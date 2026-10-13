package com.agcoding.cartrackingapp.cloud.data.remote

import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import java.io.IOException

/**
 * Turns whatever came back from the network into one of the handful of things
 * the app can actually tell the user.
 *
 * Order matters here: [SupabaseException] *is* an [IOException], so it has to
 * be matched first or every backend error would be reported as "no internet".
 */
fun Throwable.toCloudException(): CloudException {
    if (this is CloudException) return this
    return CloudException(toCloudError(), this)
}

private fun Throwable.toCloudError(): CloudError = when (this) {
    is SupabaseException -> supabaseError()
    is IOException -> CloudError.NO_CONNECTION
    else -> CloudError.UNKNOWN
}

private fun SupabaseException.supabaseError(): CloudError {
    val code = code?.lowercase()
    return when {
        code == null -> byStatus()
        code == "invalid_credentials" || code == "invalid_grant" -> CloudError.INVALID_CREDENTIALS
        code == "user_already_exists" || code == "email_exists" -> CloudError.USER_EXISTS
        code == "weak_password" -> CloudError.WEAK_PASSWORD
        code == "same_password" -> CloudError.SAME_PASSWORD
        code == "email_send_failed" -> CloudError.EMAIL_SEND_FAILED
        code == "email_address_invalid" || code == "validation_failed" -> CloudError.INVALID_EMAIL
        // Supabase names all of its rate limits over_email_send_rate_limit,
        // over_request_rate_limit and so on.
        code.startsWith("over_") -> CloudError.RATE_LIMITED
        else -> byStatus()
    }
}

private fun SupabaseException.byStatus(): CloudError = when (status) {
    401 -> CloudError.SESSION_EXPIRED
    429 -> CloudError.RATE_LIMITED
    else -> CloudError.UNKNOWN
}
