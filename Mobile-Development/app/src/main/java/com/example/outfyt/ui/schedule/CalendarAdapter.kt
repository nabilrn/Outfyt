// CalendarAdapter.kt
package com.example.outfyt.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.R
import com.example.outfyt.data.remote.response.EventsItem

class CalendarAdapter(
    private val events: List<EventsItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.tvEventName)
        val eventKind: TextView = view.findViewById(R.id.tvEventKind)
        val eventLocation: TextView = view.findViewById(R.id.tvEventLocation)
        val eventStart: TextView = view.findViewById(R.id.tvEventStart)
        val eventEnd: TextView = view.findViewById(R.id.tvEventEnd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kalender, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.summary
        holder.eventKind.text = event.kind
        holder.eventLocation.text = event.location
        holder.eventStart.text = event.start.dateTime
        holder.eventEnd.text = event.end.dateTime

        holder.itemView.setOnClickListener {
            onItemClick(event.iCalUID)
        }
    }

    override fun getItemCount(): Int = events.size
}