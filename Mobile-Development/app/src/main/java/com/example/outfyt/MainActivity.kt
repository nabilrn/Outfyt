package com.example.outfyt

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.outfyt.databinding.ActivityMainBinding

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
                    R.id.navigation_login -> hideBottomNavigationAndTopAppBar()
                    else -> showBottomNavigationAndTopAppBar()
                }
            }

            binding.navView.let { bottomNav ->
                val appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.navigation_home,
                        R.id.navigation_dashboard,
                        R.id.navigation_notifications,
                        R.id.navigation_account
                    )
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                bottomNav.setupWithNavController(navController)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideBottomNavigationAndTopAppBar() {
        binding.navView.visibility = View.GONE
        supportActionBar?.hide()
    }

    private fun showBottomNavigationAndTopAppBar() {
        binding.navView.visibility = View.VISIBLE
        supportActionBar?.show()
    }
}