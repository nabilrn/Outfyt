package com.example.outfyt

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.outfyt.databinding.ActivityMainBinding
import com.example.outfyt.utils.ReminderReceiver
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.navigation_login ->
                        hideBottomNavigationAndTopAppBar()
                    R.id.navigation_form, R.id.navigation_result ->
                        hideBottomNavigation()
                    R.id.navigation_gemini_chat ->
                        hideBottomNavigationAndTopAppBar()
                    R.id.navigation_recommendation_result ->
                        hideBottomNavigation()
                    else -> showBottomNavigationAndTopAppBar()
                }
            }

            binding.navView.let { bottomNav ->
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.navigation_home,
                        R.id.navigation_dashboard,
                        R.id.navigation_notifications,
                        R.id.navigation_account,
                        R.id.navigation_saved_recommendation
                    )
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                bottomNav.setupWithNavController(navController)
            }

            setDailyReminder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun hideBottomNavigation() {
        binding.navView.visibility = View.GONE
    }

    private fun hideBottomNavigationAndTopAppBar() {
        binding.navView.visibility = View.GONE
        supportActionBar?.hide()
    }

    private fun showBottomNavigationAndTopAppBar() {
        binding.navView.visibility = View.VISIBLE
        supportActionBar?.show()
    }

    private fun setDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}