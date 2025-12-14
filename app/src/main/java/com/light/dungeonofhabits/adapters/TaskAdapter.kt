package com.light.dungeonofhabits.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.light.dungeonofhabits.R
import com.light.dungeonofhabits.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val tasks: List<Task>,
    private val onDoneClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val difficulty: TextView = itemView.findViewById(R.id.taskDifficulty)
        val deadline: TextView = itemView.findViewById(R.id.taskDeadline)
        val doneBtn: ImageButton = itemView.findViewById(R.id.markDoneBtn)
        val editBtn: ImageButton = itemView.findViewById(R.id.editBtn)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.difficulty.text = "Difficulty: ${task.difficulty.capitalize(Locale.ROOT)}"
        holder.deadline.visibility = if (task.deadline != null) View.VISIBLE else View.GONE
        holder.deadline.text = task.deadline?.let {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            "Due: ${sdf.format(it)}"
        }
        if (task.completed) {
            holder.doneBtn.isEnabled = false
            holder.doneBtn.alpha = 0.5f
        } else {
            holder.doneBtn.isEnabled = true
            holder.doneBtn.alpha = 1.0f
        }

        holder.doneBtn.setOnClickListener { view ->
            val fadeOut = AnimationUtils.loadAnimation(view.context, R.anim.fade_out)
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    onDoneClick(task)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            holder.itemView.startAnimation(fadeOut)
        }

        holder.editBtn.setOnClickListener { onEditClick(task) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(task) }
    }
}
