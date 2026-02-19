package com.example.dewapermana_smkn8jember

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dewapermana_smkn8jember.databinding.ActivityAbsenBinding

class AbsenActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityAbsenBinding.inflate(layoutInflater)
		setContentView(binding.root)

	}
}