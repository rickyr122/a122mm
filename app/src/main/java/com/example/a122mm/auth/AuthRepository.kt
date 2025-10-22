package com.example.a122mm.auth

import android.content.Context
import com.example.a122mm.helper.nowClientTimeString
import com.example.a122mm.helper.tzOffsetMinutesNow
import com.example.a122mm.utility.getDeviceId
import com.example.a122mm.utility.getDeviceName
import com.example.a122mm.utility.getDeviceType
import org.json.JSONObject
import java.io.IOException

class AuthRepository(
    private val publicApi: AuthApiService,
    private val authedApi: AuthApiService,
    private val store: TokenStore
) {
    // ⬇️ changed: added `context` and build LoginReq with device fields
    suspend fun login(context: Context, email: String, password: String): Result<Unit> {
        return try {
            val req = AuthApiService.LoginReq(
                email = email,
                password = password,
                device_id = getDeviceId(context),
                device_name = getDeviceName(),
                device_type = getDeviceType(context),
                client_time = nowClientTimeString(),
                tz_offset_minutes = tzOffsetMinutesNow()
            )

            val resp = publicApi.login(req)
            if (resp.isSuccessful && resp.body() != null) {
                val b = resp.body()!!
                store.save(b.access_token, b.refresh_token)
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractError(resp.code(), resp.errorBody()?.string())))
            }
        } catch (io: IOException) { // network down, DNS, timeouts
            Result.failure(Exception("Network error — please check your connection"))
        } catch (t: Throwable) {
            Result.failure(Exception("Unexpected error: ${t.message ?: "unknown"}"))
        }
    }

    suspend fun signup(email: String, name: String, password: String): Result<Unit> {
        return try {
            val resp = publicApi.signup(
                SignUpReq(
                    email = email,
                    name = name,
                    password = password,
                    client_time = nowClientTimeString(),
                    tz_offset_minutes = tzOffsetMinutesNow()
                )
            )
            if (resp.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractError(resp.code(), resp.errorBody()?.string())))
            }
        } catch (io: IOException) {
            Result.failure(Exception("Network error — please check your connection"))
        } catch (t: Throwable) {
            Result.failure(Exception("Unexpected error: ${t.message ?: "unknown"}"))
        }
    }


    suspend fun profile(): Result<Profile> {
        return try {
            val resp = authedApi.profile()
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!.profile)
            } else {
                Result.failure(Exception(extractError(resp.code(), resp.errorBody()?.string())))
            }
        } catch (io: IOException) {
            Result.failure(Exception("Network error — please check your connection"))
        } catch (t: Throwable) {
            Result.failure(Exception("Unexpected error: ${t.message ?: "unknown"}"))
        }
    }

    suspend fun registerDevice(
        deviceId: String,
        deviceName: String,
        deviceType: String,
        clientTime: String
    ): Result<Unit> {
        val resp = authedApi.upsertDevice(
            mapOf(
                "device_id" to deviceId,
                "device_name" to deviceName,
                "device_type" to deviceType,
                "last_active" to clientTime
            )
        )
        return if (resp.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Device upsert failed: ${resp.code()}"))
    }

    suspend fun hasSession(): Boolean = store.access()?.isNotBlank() == true
    suspend fun logout() = store.clear()

    suspend fun loadProfilePic(): Result<AuthApiService.ProfilePicRes> {
        val resp = authedApi.getProfilePic()
        return if (resp.isSuccessful && resp.body() != null) {
            Result.success(resp.body()!!)   // ✅ return full object
        } else {
            Result.failure(Exception("Failed to load profile picture"))
        }
    }

    suspend fun listDevices(): Result<List<AuthApiService.DeviceDto>> {
        val r = authedApi.listDevices()
        return if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("HTTP ${r.code()}"))
    }

    suspend fun logoutDevice(deviceId: String): Result<Unit> {
        val r = authedApi.logoutDevice(mapOf("device_id" to deviceId))
        return if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("HTTP ${r.code()}"))
    }

    suspend fun logoutOtherDevices(currentDeviceId: String): Result<Unit> {
        val r = authedApi.logoutOthers(mapOf("device_id" to currentDeviceId))
        return if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("HTTP ${r.code()}"))
    }

    suspend fun pingAuth(): Boolean {
        return try {
            val r = authedApi.me()
            r.isSuccessful
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun pingAuthNoRefresh(): Boolean = try {
        authedApi.meNoRefresh().isSuccessful
    } catch (_: Throwable) { false }



}

/** Pulls "error" from server JSON; falls back to friendly per-code message. */
private fun extractError(code: Int, rawBody: String?): String {
    // Try to parse {"error":"..."} from PHP
    if (!rawBody.isNullOrBlank()) {
        try {
            val msg = JSONObject(rawBody).optString("error").ifBlank { null }
            if (msg != null) return msg
        } catch (_: Throwable) { /* not JSON */ }
    }
    // Friendly fallbacks by status code
    return when (code) {
        400 -> "Missing or invalid input"
        401 -> "Invalid credentials"
        403 -> "Not allowed"
        404 -> "Not found"
        409 -> "Already exists"
        422 -> "Unprocessable request"
        500 -> "Server error — please try again"
        else -> "Error $code"
    }
}



