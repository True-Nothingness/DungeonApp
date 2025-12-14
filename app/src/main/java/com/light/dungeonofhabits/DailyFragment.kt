package com.light.dungeonofhabits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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

class DailyFragment : Fragment() {

    private lateinit var dailyRecyclerView: RecyclerView
    private lateinit var dailyAdapter: TaskAdapter
    private var dailyList = mutableListOf<Task>()
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daily, container, false)
        dailyRecyclerView = view.findViewById(R.id.dailyRecyclerView)
        dailyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        dailyAdapter = TaskAdapter(dailyList, ::onDailyDoneClicked, ::onEditClicked, ::onDeleteClicked)
        dailyRecyclerView.adapter = dailyAdapter

        loadDailies()
        return view
    }


    private fun loadDailies() {
        ApiClient.apiService.getDailies().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    dailyList.clear()
                    response.body()?.let { dailyList.addAll(it) }
                    dailyAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Failed to load dailies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onDailyDoneClicked(task: Task) {
        ApiClient.apiService.toggleTask(task.id).enqueue(object : Callback<ToggleTaskResponse> {
            override fun onResponse(call: Call<ToggleTaskResponse>, response: Response<ToggleTaskResponse>) {
                if (response.isSuccessful) {
                    loadDailies()
                    Toast.makeText(requireContext(), "Daily marked as done!", Toast.LENGTH_SHORT).show()
                    viewModel.getProfile()
                } else {
                    Toast.makeText(requireContext(), "Failed to update daily", Toast.LENGTH_SHORT).show()
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
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Daily")
            .setMessage("Are you sure you want to delete \"${task.title}\"?")
            .setPositiveButton("Yes") { _, _ ->
                ApiClient.apiService.deleteTask(task.id).enqueue(object : Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                        if (response.isSuccessful) {
                            loadDailies()
                            Toast.makeText(requireContext(), "Daily deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete daily", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadDailies()
        Log.d("DailyFragment", "Fragment visible, loading dailies...")
    }
}
