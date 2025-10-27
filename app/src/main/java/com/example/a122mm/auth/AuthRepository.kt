package com.example.a122mm.auth

import android.content.Context
import com.example.a122mm.helper.nowClientTimeString
import com.example.a122mm.helper.tzOffsetMinutesNow
import com.example.a122mm.utility.getDeviceId
import com.example.a122mm.utility.getDeviceName
import com.example.a122mm.utility.getDeviceType
import org.json.JSONObject
import retrofit2.HttpException
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
        val offset = tzOffsetMinutesNow()
        val r = authedApi.listDevices(offset)
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

//    suspend fun pingAuthNoRefresh(): Boolean = try {
//        authedApi.meNoRefresh().isSuccessful
//    } catch (_: Throwable) { false }

    suspend fun pingAuthNoRefresh(): Boolean {
        return try {
            val resp = authedApi.meNoRefresh() // Response<...>
            when {
                resp.isSuccessful -> true               // 2xx → OK
                resp.code() == 401 -> false             // 401 → invalid/expired token
                else -> throw HttpException(resp)       // other 4xx/5xx → let caller handle
            }
        } catch (e: HttpException) {
            if (e.code() == 401) false else throw e     // keep 401 as false, bubble others
        } catch (e: IOException) {
            throw e                                      // offline/timeout → let caller handle
        }
    }


    suspend fun changePassword(
        current: String,
        newPass: String,
        signOutAll: Boolean
    ): Result<Boolean /*logout*/> {
        return try {
            val resp = authedApi.changePassword(
                AuthApiService.ChangePasswordReq(
                    current_password = current,
                    new_password = newPass,
                    sign_out_all = signOutAll
                )
            )
            if (resp.isSuccessful && resp.body() != null) {
                // return whether server asked us to logout (e.g., after revoking all tokens)
                Result.success(resp.body()!!.logout == true)
            } else {
                Result.failure(Exception(extractError(resp.code(), resp.errorBody()?.string())))

//                Result.failure(
//                    Exception("HTTP ${resp.code()}: " + extractError(resp.code(), resp.errorBody()?.string()))
//                )
            }
        } catch (io: IOException) {
            Result.failure(Exception("Network error — please check your connection"))
        } catch (t: Throwable) {
            Result.failure(Exception("Unexpected error: ${t.message ?: "unknown"}"))
        }
    }

    // ============ ICON PICKER ============
    suspend fun loadIconSections(userId: Int): Result<List<AuthApiService.IconSection>> {
        return try {
            val r = authedApi.iconSections(userId)
            if (r.isSuccessful && r.body() != null) {
                Result.success(r.body()!!)
            } else {
                Result.failure(Exception(extractError(r.code(), r.errorBody()?.string())))
            }
        } catch (t: Throwable) {
            Result.failure(Exception("Failed to load icons: ${t.message ?: "unknown"}"))
        }
    }

    /** Saves current pp_link into history (server does it) and updates tm_users.pp_link. */
    suspend fun setProfilePicture(userId: Int, icon: AuthApiService.IconItem): Result<String /*newUrl*/> {
        return try {
            val r = authedApi.setProfilePicture(
                userId = userId,
                iconId = icon.icon_id,
                imgUrl = icon.img_url
            )
            if (r.isSuccessful && r.body()?.ok == true) {
                Result.success(r.body()?.pp_link ?: icon.img_url)
            } else {
                val msg = r.body()?.error ?: extractError(r.code(), r.errorBody()?.string())
                Result.failure(Exception(msg))
            }
        } catch (t: Throwable) {
            Result.failure(Exception("Failed to update profile picture: ${t.message ?: "unknown"}"))
        }
    }

//    fun getUserId(context: Context): Int {
//        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//        return prefs.getInt("user_id", 0) // 0 = not logged in / missing
//    }

    suspend fun updateUsername(userId: Int, username: String): Result<Unit> {
        return try {
            val r = authedApi.updateProfile(userId, username)
            if (r.isSuccessful && (r.body()?.ok == true)) Result.success(Unit)
            else Result.failure(Exception(r.body()?.error ?: extractError(r.code(), r.errorBody()?.string())))
        } catch (t: Throwable) {
            Result.failure(Exception("Failed to update username: ${t.message ?: "unknown"}"))
        }
    }



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



