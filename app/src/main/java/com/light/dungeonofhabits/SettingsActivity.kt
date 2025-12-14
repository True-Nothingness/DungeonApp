package com.light.dungeonofhabits

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.light.dungeonofhabits.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            viewModel.setDailyReminderEnabled(true)
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            Toast.makeText(this, "Notifications are required for reminders.", Toast.LENGTH_SHORT).show()
            binding.dailyReminderSwitch.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.dailyReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                askForNotificationPermission()
            } else {
                viewModel.setDailyReminderEnabled(false)
            }
        }

        binding.dailyReminderTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun setupObservers() {
        viewModel.dailyReminderEnabled.observe(this) { isEnabled ->
            binding.dailyReminderSwitch.isChecked = isEnabled
            binding.dailyReminderTime.isEnabled = isEnabled
            updateAlarm(isEnabled, viewModel.reminderTimeHour.value, viewModel.reminderTimeMinute.value)
        }

        viewModel.reminderTimeHour.observe(this) { updateReminderTimeText() }
        viewModel.reminderTimeMinute.observe(this) { updateReminderTimeText() }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    viewModel.setDailyReminderEnabled(true)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission.
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, permission is granted by default
            viewModel.setDailyReminderEnabled(true)
        }
    }

    private fun updateReminderTimeText() {
        val hour = viewModel.reminderTimeHour.value ?: 21
        val minute = viewModel.reminderTimeMinute.value ?: 0
        binding.dailyReminderTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    private fun showTimePickerDialog() {
        val currentHour = viewModel.reminderTimeHour.value ?: 21
        val currentMinute = viewModel.reminderTimeMinute.value ?: 0

        TimePickerDialog(this, { _, hourOfDay, minute ->
            viewModel.setReminderTime(hourOfDay, minute)
            if (viewModel.dailyReminderEnabled.value == true) {
                updateAlarm(true, hourOfDay, minute)
            }
        }, currentHour, currentMinute, true).show()
    }

    private fun updateAlarm(isEnabled: Boolean, hour: Int?, minute: Int?) {
        if (isEnabled && hour != null && minute != null) {
            AlarmUtils.scheduleDailyNotification(this, hour, minute)
        } else {
            AlarmUtils.cancelNotificationAlarm(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
