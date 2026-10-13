package com.agcoding.cartrackingapp.cloud.data.remote

/**
 * Where the backend lives.
 *
 * Both values are public by design — the publishable key ships inside the APK
 * and anyone can read it out. It grants nothing on its own: every table has
 * row level security, so a request only ever sees the rows of the user whose
 * JWT it carries. The secret / service-role key is never in this repository;
 * it exists only inside the edge functions, as SUPABASE_SERVICE_ROLE_KEY.
 */
object SupabaseConfig {
    const val URL = "https://gqinaapwmxkvizehseag.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_5S75V29tFo9TGD5Rs_nLsA_7BcxrkCk"
}
