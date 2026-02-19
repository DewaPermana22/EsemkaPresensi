package com.example.dewapermana_smkn8jember

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.util.foreignKeyCheck
import com.example.dewapermana_smkn8jember.databinding.ActivityProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityProfileBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window)
		val token = checkedToken(this@ProfileActivity)
			if (token != null){
				CoroutineScope(Dispatchers.Main).launch {
					val respon = handlerAPI("https://lks.naar.my.id/api/Auth/me", "GET", token = token, typeRes = JSONArray::class.java )
					if (respon != null) {
						for (i in 0 until respon.length()){
							val response = respon.getJSONObject(i)
							val namauser = response?.getString("nama")
							val emailuser = response?.getString("email")
							val namaImage = response?.getString("profilPict")
							val kodeReveral = response?.getString("code")
							binding.namePegawaiIzin.setText(namauser)
							binding.statusIzin.setText(emailuser)
							binding.HariTanggal.setText(kodeReveral)
							setImage(namaImage, binding.fotoProfile)
						}
					}

				}
			}
	}
}