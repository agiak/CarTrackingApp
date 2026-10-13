package com.agcoding.cartrackingapp.cloud.domain.model

/**
 * Every failure the account and sync flows can show the user.
 *
 * Deliberately small: the UI has to say something useful in Greek and English
 * for each of these, so the list is the set of distinct *messages*, not the set
 * of distinct causes.
 */
enum class CloudError {
    /** Wrong email or password, and no temporary password matched either. */
    INVALID_CREDENTIALS,
    USER_EXISTS,
    WEAK_PASSWORD,

    /** The new password is the same as the current one. */
    SAME_PASSWORD,

    /** The reset code was created but the email could not be delivered. */
    EMAIL_SEND_FAILED,
    INVALID_EMAIL,

    /** Too many attempts; Supabase is rate limiting. */
    RATE_LIMITED,

    /** The refresh token was rejected. The user has to sign in again. */
    SESSION_EXPIRED,
    NO_CONNECTION,

    /** Sign-out was asked for while local changes had not been uploaded. */
    UNSYNCED_CHANGES,
    UNKNOWN,
}

class CloudException(
    val error: CloudError,
    cause: Throwable? = null,
) : Exception(cause?.message ?: error.name, cause)
