package com.example.builddaily.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = "https://csLMz6xoxb21c6A94lyIYg.b6c2SJyZ.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_csLMz6xoxb21c6A94lyIYg_b6c2SJyZ"
    
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
