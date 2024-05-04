package com.example.bottomnav

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.content.Intent
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity2 : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasUsageStatsPermission()) {

            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app requires usage access settings to function. Please grant the permission.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.bottom_home -> {
                    if (!hasUsageStatsPermission()) {
                        // Show a message to the user to grant the permission
                        // You can use a Dialog, Snackbar, Toast, etc.
                    } else {
                        replaceFragment(HomeFragment())
                    }
                    true
                }
                R.id.bottom_list -> {
                    replaceFragment(ListFragment())
                    true
                }
                else -> false
            }
        }
        if (hasUsageStatsPermission()) {
            replaceFragment(HomeFragment())
        } else {
            // Show a message to the user to grant the permission
            // You can use a Dialog, Snackbar, Toast, etc.
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit()
    }
}
