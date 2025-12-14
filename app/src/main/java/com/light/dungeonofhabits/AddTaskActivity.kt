package com.light.dungeonofhabits

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.databinding.ActivityAddTaskBinding
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.Task
import com.light.dungeonofhabits.models.TaskRequest
import com.light.dungeonofhabits.models.TaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private var deadlineCalendar: Calendar = Calendar.getInstance()
    private var existingTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingTask = intent.getSerializableExtra("task") as? Task
        val isEditMode = existingTask != null

        setupInitialState(isEditMode)
        setupListeners(isEditMode)
    }

    private fun setupInitialState(isEditMode: Boolean) {
        val type = intent.getStringExtra("type") ?: "task"
        if (type == "task") {
            binding.dateLayout.visibility = View.VISIBLE
            binding.reminderSwitch.visibility = View.VISIBLE
        } else {
            binding.dateLayout.visibility = View.GONE
            binding.reminderSwitch.visibility = View.GONE
        }

        if (isEditMode) {
            binding.btnAdd.text = getString(R.string.update)
            existingTask?.let { task ->
                binding.inputTitle.setText(task.title)
                when (task.difficulty) {
                    "easy" -> binding.radioDifficulty.check(R.id.radioEasy)
                    "medium" -> binding.radioDifficulty.check(R.id.radioMedium)
                    "hard" -> binding.radioDifficulty.check(R.id.radioHard)
                }
                task.deadline?.let {
                    deadlineCalendar.time = it
                    updateDeadlineInputText()
                    binding.reminderSwitch.isChecked = true
                }
            }
        }
    }

    private fun setupListeners(isEditMode: Boolean) {
        binding.dateInput.setOnClickListener { showDatePicker() }
        binding.btnAdd.setOnClickListener { saveTask(isEditMode) }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select due date")
            .setSelection(deadlineCalendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedCalendar = Calendar.getInstance().apply { timeInMillis = selection }
            deadlineCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR))
            deadlineCalendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH))
            deadlineCalendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH))
            showTimePicker()
        }
        datePicker.show(supportFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            deadlineCalendar.set(Calendar.MINUTE, minute)
            updateDeadlineInputText()
        }, deadlineCalendar.get(Calendar.HOUR_OF_DAY), deadlineCalendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    private fun updateDeadlineInputText() {
        val format = "dd-MM-yyyy HH:mm"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.dateInput.setText(sdf.format(deadlineCalendar.time))
    }

    private fun saveTask(isEditMode: Boolean) {
        val title = binding.inputTitle.text.toString().trim()
        val difficulty = when (binding.radioDifficulty.checkedRadioButtonId) {
            R.id.radioEasy -> "easy"
            R.id.radioMedium -> "medium"
            R.id.radioHard -> "hard"
            else -> null
        }
        val type = intent.getStringExtra("type") ?: "task"
        val deadline = if (type == "task" && binding.dateInput.text?.isNotEmpty() == true) deadlineCalendar else null

        if (title.isBlank() || difficulty == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TaskRequest(title, difficulty, type, deadline?.time)

        if (isEditMode) {
            ApiClient.apiService.editTask(existingTask!!.id, request).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddTaskActivity, "Task updated", Toast.LENGTH_SHORT).show()
                        handleAlarm(existingTask!!.id, title, deadline)
                        finishAndReturnToMain()
                    } else {
                        Toast.makeText(this@AddTaskActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@AddTaskActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            ApiClient.apiService.addTask(request).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddTaskActivity, "Task added successfully", Toast.LENGTH_SHORT).show()
                        val newTaskId = response.body()?.task?.id
                        if (newTaskId != null) {
                            handleAlarm(newTaskId, title, deadline)
                        }
                        finishAndReturnToMain()
                    } else {
                        Toast.makeText(this@AddTaskActivity, "Failed to add task", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@AddTaskActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun handleAlarm(taskId: String, taskTitle: String, deadline: Calendar?) {
        if (binding.reminderSwitch.isChecked && deadline != null) {
            AlarmUtils.scheduleTaskReminder(this, taskId, taskTitle, deadline)
        } else {
            AlarmUtils.cancelTaskReminder(this, taskId)
        }
    }

    private fun finishAndReturnToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
