package com.example.dewapermana_smkn8jember

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dewapermana_smkn8jember.databinding.ActivityDetailAbsenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class DetailAbsenAct : AppCompatActivity() {
	private lateinit var binding : ActivityDetailAbsenBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityDetailAbsenBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val idabsen = intent.getIntExtra("id_absen", 0)
		Log.d("Debug", "Id Absen : $idabsen")
		val token = checkedToken(this@DetailAbsenAct)
		if (idabsen != 0) {
			CoroutineScope(Dispatchers.Main).launch {
				val data = handlerAPI("https://lks.naar.my.id/api/Absensi/get/detailby/$idabsen", "GET", token = token, typeRes = JSONObject::class.java)
				Log.d("Response Detail Absen : ", "response : $data")
					val nama = data?.getString("nama").toString()
					val status = data?.getString("status")
					val hari = data?.getString("tanggal")
					val selfie = data?.getString("fotoMasuk")
					val selfiePulang = data?.getString("fotoKeluar")
					if (!selfie.isNullOrEmpty() && !selfiePulang.isNullOrEmpty()) {
						setImage(selfie, binding.buktiFoto)
						setImage(selfiePulang, binding.buktiFotoPulang)
					} else{
						binding.buktiFoto.setImageResource(R.drawable.empty_image_absen)
						binding.buktiFotoPulang.setImageResource(R.drawable.empty_image_absen)
					}
					binding.namePegawaiIzin.setText(nama)
					binding.HariTanggal.setText(hari)
					binding.statusIzin.setText(status)
			}
		}

		binding.buttonBack.setOnClickListener {
			val intent = Intent(this@DetailAbsenAct, RiwayatAbsenAct::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
			finish()
		}
	}
}