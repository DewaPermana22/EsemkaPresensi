package com.example.dewapermana_smkn8jember

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.dewapermana_smkn8jember.databinding.ActivitySplashScrBinding

class SplashScr : AppCompatActivity() {
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivitySplashScrBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window)
		Handler().postDelayed({
			startActivity(Intent(this@SplashScr, LoginScr::class.java))
			finish()
		}, 3000)
	}
}