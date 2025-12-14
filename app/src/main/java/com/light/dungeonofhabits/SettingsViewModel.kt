package com.light.dungeonofhabits

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.light.dungeonofhabits.utils.Constants

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE)

    private val _dailyReminderEnabled = MutableLiveData<Boolean>()
    val dailyReminderEnabled: LiveData<Boolean> = _dailyReminderEnabled

    private val _reminderTimeHour = MutableLiveData<Int>()
    val reminderTimeHour: LiveData<Int> = _reminderTimeHour

    private val _reminderTimeMinute = MutableLiveData<Int>()
    val reminderTimeMinute: LiveData<Int> = _reminderTimeMinute

    init {
        _dailyReminderEnabled.value = prefs.getBoolean(Constants.DAILY_REMINDER_ENABLED, true)
        _reminderTimeHour.value = prefs.getInt(Constants.REMINDER_TIME_HOUR, 21) // Default 9 PM
        _reminderTimeMinute.value = prefs.getInt(Constants.REMINDER_TIME_MINUTE, 0)
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        _dailyReminderEnabled.value = enabled
        prefs.edit().putBoolean(Constants.DAILY_REMINDER_ENABLED, enabled).apply()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderTimeHour.value = hour
        _reminderTimeMinute.value = minute
        prefs.edit()
            .putInt(Constants.REMINDER_TIME_HOUR, hour)
            .putInt(Constants.REMINDER_TIME_MINUTE, minute)
            .apply()
    }
}
