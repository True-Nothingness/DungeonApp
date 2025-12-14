package com.light.dungeonofhabits

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.adapters.TaskAdapter
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.Task
import com.light.dungeonofhabits.models.ToggleTaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TaskFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private var taskList = mutableListOf<Task>()
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView)
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(taskList, ::onTaskDoneClicked, ::onEditClicked, ::onDeleteClicked)
        taskRecyclerView.adapter = taskAdapter

        loadTasks()
        return view
    }

    private fun loadTasks() {
        ApiClient.apiService.getTasks().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    taskList.clear()
                    response.body()?.let { taskList.addAll(it) }
                    taskAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onTaskDoneClicked(task: Task) {
        ApiClient.apiService.toggleTask(task.id).enqueue(object : Callback<ToggleTaskResponse> {
            override fun onResponse(call: Call<ToggleTaskResponse>, response: Response<ToggleTaskResponse>) {
                if (response.isSuccessful) {
                    loadTasks()
                    Toast.makeText(requireContext(), "Task marked as done!", Toast.LENGTH_SHORT).show()
                    viewModel.getProfile()
                } else {
                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ToggleTaskResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun onEditClicked(task: Task) {
        val intent = Intent(context, AddTaskActivity::class.java)
        intent.putExtra("type", task.type)
        intent.putExtra("task", task)
        startActivity(intent)
    }
    private fun onDeleteClicked(task: Task) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete \"${task.title}\"?")
        builder.setPositiveButton("Yes") { _, _ ->
            ApiClient.apiService.deleteTask(task.id).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        loadTasks()
                        Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
