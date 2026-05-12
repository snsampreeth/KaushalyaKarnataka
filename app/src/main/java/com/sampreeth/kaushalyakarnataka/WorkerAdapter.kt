package com.sampreeth.kaushalyakarnataka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkerAdapter(
    private val workers: List<Worker>,
    private val onClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvArea: TextView = view.findViewById(R.id.tvArea)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]
        holder.tvName.text = worker.name
        holder.tvCategory.text = worker.category.ifEmpty { "No category" }
        holder.tvArea.text = worker.area.ifEmpty { "No area" }
        holder.tvRating.text = if (worker.rating > 0) "⭐ ${worker.rating}" else "No rating"
        holder.tvInitials.text = worker.name.firstOrNull()?.toString() ?: "?"
        holder.itemView.setOnClickListener { onClick(worker) }
    }

    override fun getItemCount() = workers.size
}