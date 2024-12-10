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
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.RecommendationRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    fetchRecommendations(scheduleId)
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

    private fun fetchRecommendations(scheduleId: String) {
        val accessToken = LoginPreferences.getAccessToken(requireContext())
        if (accessToken != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.api.getRecommendation(
                        "Bearer $accessToken",
                        RecommendationRequest(scheduleId)
                    )
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val action = ScheduleFragmentDirections.actionNavigationDashboardToNavigationRecommendationResult(scheduleId)
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), "Failed to fetch recommendations", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Access Token is null", Toast.LENGTH_SHORT).show()
        }
    }
}