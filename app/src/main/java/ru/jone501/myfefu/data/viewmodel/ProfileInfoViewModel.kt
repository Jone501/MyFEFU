package ru.jone501.myfefu.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import ru.jone501.myfefu.domain.model.ProfileInfo
import ru.jone501.myfefu.domain.model.StudentProfileInfo
import ru.jone501.myfefu.networking.api.ApiService
import ru.jone501.myfefu.networking.token.NetworkRequestInterceptor
import java.io.IOException

class ProfileInfoViewModel(
    val sessionManager: EncryptedSessionManager,
    val apiService: ApiService
) : ViewModel() {
    private val _profileInfo: MutableStateFlow<ProfileInfo?> = MutableStateFlow(null)
    private val _studentProfileInfo: MutableStateFlow<List<StudentProfileInfo>?> = MutableStateFlow(null)
    private var _requiresLogin = MutableStateFlow(false)

    val profileInfo = _profileInfo.asStateFlow()
    val studentProfileInfo = _studentProfileInfo.asStateFlow()
    val requiresLogin = _requiresLogin.asStateFlow()

    init {
        init()
    }

    fun init() {
        _requiresLogin.value = false
        _profileInfo.value = null
        _studentProfileInfo.value = null
        loadProfileInfo()
        loadStudentProfileInfo()
    }

    fun clear() {
        viewModelScope.launch {
            sessionManager.clear()
        }
    }

    private fun loadProfileInfo() {
        viewModelScope.launch {
            _profileInfo.value = sessionManager.getProfileInfo().firstOrNull()
            if (_profileInfo.value == null) {
                try {
                    apiService.getProfile().let { response ->
                        if (response.isSuccessful) {
                            response.body()?.data.let {
                                _profileInfo.value = it
                                if (it != null) {
                                    sessionManager.setProfileInfo(it)
                                }
                            }
                        } else when (response.code()) {
                            NetworkRequestInterceptor.UNAUTHORIZED_CODE,
                            NetworkRequestInterceptor.FORBIDDEN_CODE -> {
                                _requiresLogin.value = true
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.i("ERROR", e.message, e)
                }
            }
        }
    }

    private fun loadStudentProfileInfo() {
        viewModelScope.launch {
            _studentProfileInfo.value = sessionManager.getStudentProfileInfo().firstOrNull()
            if (_studentProfileInfo.value == null) {
                try {
                    apiService.getStudentProfile().let { response ->
                        if (response.isSuccessful) {
                            response.body()?.data?.let {
                                it.forEach { studentProfileInfo ->
                                    if (sessionManager.addStudentProfileInfo(studentProfileInfo)) {
                                        _studentProfileInfo.value = sessionManager.getStudentProfileInfo().firstOrNull()
                                    }
                                }
                            }
                        } else when (response.code()) {
                            NetworkRequestInterceptor.UNAUTHORIZED_CODE,
                            NetworkRequestInterceptor.FORBIDDEN_CODE -> {
                                _requiresLogin.value = true
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.i("ERROR", e.message, e)
                }
            }
        }
    }
}