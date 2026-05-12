package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceCardAdapter(
    private val services: List<ServiceCard>,
    private val showEditButton: Boolean = false,
    private val workerId: String = ""
) : RecyclerView.Adapter<ServiceCardAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvPriceType: TextView = view.findViewById(R.id.tvPriceType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.tvServiceName.text = service.serviceName
        holder.tvPrice.text = "₹${service.price}"
        holder.tvPriceType.text = service.priceType

        // Long press to edit
        if (showEditButton) {
            holder.itemView.setOnLongClickListener {
                val intent = Intent(holder.itemView.context, EditServiceActivity::class.java)
                intent.putExtra("serviceId", service.id)
                intent.putExtra("serviceName", service.serviceName)
                intent.putExtra("price", service.price)
                intent.putExtra("priceType", service.priceType)
                intent.putExtra("workerId", workerId)
                holder.itemView.context.startActivity(intent)
                true
            }
        }
    }

    override fun getItemCount() = services.size
}