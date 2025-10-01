package com.songajae.amgi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.songajae.amgi.databinding.ActivityMainBinding
import com.songajae.amgi.util.AppStringProvider

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppStringProvider.init(applicationContext)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        setSupportActionBar(vb.toolbar)
    }
    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host).navigateUp() || super.onSupportNavigateUp()
}
