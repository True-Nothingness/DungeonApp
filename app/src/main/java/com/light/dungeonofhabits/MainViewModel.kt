package com.light.dungeonofhabits

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _profile = MutableLiveData<Profile?>()
    val profile: LiveData<Profile?> = _profile

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun getProfile() {
        ApiClient.apiService.getUserProfile().enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    _profile.postValue(response.body())
                    val prefs = getApplication<Application>().getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE)
                    val selectedCharacter = response.body()?.user?.selectedCharacter
                    prefs.edit().putString(Constants.SELECTED_CHARACTER, selectedCharacter).apply()
                } else {
                    _toastMessage.postValue("Failed to load profile")
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {
                _toastMessage.postValue("Network error: ${t.message}")
            }
        })
    }
}
