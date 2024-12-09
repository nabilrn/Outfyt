package com.example.outfyt.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.databinding.FragmentScheduleBinding
import com.example.outfyt.R

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scheduleViewModel.events.observe(viewLifecycleOwner, Observer { events ->
            binding.progressBar.visibility = View.GONE
            if (events != null) {
                calendarAdapter = CalendarAdapter(events) { scheduleId ->
                    val action = ScheduleFragmentDirections.actionNavigationDashboardToNavigationRecommendationResult(scheduleId)
                    findNavController().navigate(action)
                }
                recyclerView.adapter = calendarAdapter
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_events_found), Toast.LENGTH_SHORT).show()
            }
        })

        scheduleViewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        })

        scheduleViewModel.fetchCalendarData()
        observeViewModel()
    }

    private fun observeViewModel() {
        scheduleViewModel.uploadStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        scheduleViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}