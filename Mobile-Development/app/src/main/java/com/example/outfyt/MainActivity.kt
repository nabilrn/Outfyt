package com.example.outfyt

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.outfyt.databinding.ActivityMainBinding
import com.example.outfyt.data.local.LoginPreferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        lifecycleScope.launch {
            LoginPreferences.isLoggedIn(this@MainActivity).collect { isLoggedIn ->
                if (isLoggedIn) {
                    setupNavigation(navController)
                } else {
                    navController.navigate(R.id.navigation_login)
                    hideBottomNavigationAndTopAppBar()
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_login) {
                hideBottomNavigationAndTopAppBar()
            } else {
                showBottomNavigationAndTopAppBar()
            }
        }
    }

    private fun setupNavigation(navController: androidx.navigation.NavController) {
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_account
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
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
