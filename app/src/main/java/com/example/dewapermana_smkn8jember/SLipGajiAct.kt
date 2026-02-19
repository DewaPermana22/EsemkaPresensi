package com.example.dewapermana_smkn8jember

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dewapermana_smkn8jember.databinding.ActivitySlipGajiBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class SLipGajiAct : AppCompatActivity() {
	private var gaji : Int = 0;
	private lateinit var buttonCairkan : Button
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivitySlipGajiBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val token = checkedToken(this)

		val tokenUser = checkedToken(this).toString()

		CoroutineScope(Dispatchers.Main).launch {
			val respon = handlerAPI("https://lks.naar.my.id/api/Auth/me", "GET", token = tokenUser, typeRes = JSONArray::class.java )
			if (respon != null) {
				for (i in 0 until respon.length()){
					val response = respon.getJSONObject(i)
					gaji = response?.getInt("id")!!
				}
			}

		}

		buttonCairkan = binding.buttonCairkan

		buttonCairkan.setOnClickListener {
			if (!buttonCairkan.isEnabled){
				Toast.makeText(this@SLipGajiAct, "Gaji Anda Sudah Dicairkan, Silahkan Menunggu gajian Berikutnya!", Toast.LENGTH_SHORT).show()
			} else {
				if (token != null) showModals(gaji,token)
			}
		}

		binding.backButton.setOnClickListener {
			val intent = Intent(this@SLipGajiAct, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
			finish()
		}

		//Fetch Rekap Absen
		CoroutineScope(Dispatchers.Main).launch {
			if (token != null){
				val res = handlerAPI("https://lks.naar.my.id/api/Absensi/Rekap/me", "GET", token = token, typeRes = JSONObject::class.java)
				binding.totalMasuk.text = res?.getInt("hadir").toString()
				binding.totalTidakMasuk.text = res?.getInt("tidakHadir").toString()
				binding.totalIzin.text = res?.getInt("izin").toString()
				binding.totalSakit.text = res?.getInt("sakit").toString()
			} else {
				Log.e("Error Token : ", "UnAuthorization User Token not Found!")
			}
		}

		//Fetch Rekap Tugas
		CoroutineScope(Dispatchers.Main).launch {
			if (token != null){
				val res = handlerAPI("https://lks.naar.my.id/api/Task/Rekap/me", "GET", token = token, typeRes = JSONObject::class.java)
				binding.ProjectUnDone.text = res?.getInt("tidakSelesai").toString()
				binding.ProjectDone.text = res?.getInt("sudahSelesai").toString()
				binding.ProjectDeadline.text = res?.getInt("terlambat").toString()
			} else {
				Log.e("Error Token : ", "UnAuthorization User Token not Found!")
			}
		}

		//Fetch Gaji
		CoroutineScope(Dispatchers.Main).launch {
			if (token != null){
				val response = handlerAPI("https://lks.naar.my.id/api/Penggajian/me", "GET", token = token, typeRes = JSONArray::class.java)
				if (response != null && response.length() > 0){
					for ( i in 0 until response.length()){
						val res = response.getJSONObject(i)
						val gajiPokok = res?.getInt("gaji_pokok")
						val bonus = res?.getInt("bonus")
						val total = res?.getInt("total")
						val pelanggaran =  res?.getInt("pelanggaran")
						binding.gajiPokokUser.text = gajiPokok?.let { formatUang(it) }
						binding.bonusUser.text = bonus?.let { formatUang(it) }
						binding.pelanggaranUser.text = pelanggaran?.let { formatUang(it) }
						binding.totalGaji.text = total?.let { formatUang(it) }
					}
				} else {
					buttonCairkan.isEnabled = false
				}
			} else {
				Log.e("Error Token : ", "UnAuthorization User Token not Found!")
			}
		}
	}

	fun showModals(id : Int, token : String){
		val dialog = Dialog(this)
		val view = LayoutInflater.from(this).inflate(R.layout.modals, null)

		val generateCode = code(8)
		view.findViewById<TextView>(R.id.code).text = generateCode
		view.findViewById<Button>(R.id.buttonSudahAmbil).setOnClickListener {
			ambilGaji(id,token)
			dialog.dismiss()
			buttonCairkan.isEnabled = false
			recreate()
		}
		dialog.setContentView(view)
		dialog.setCancelable(true)
		dialog.show()
	}

	fun code(length : Int) : String{
		val pola = ('A'..'Z') + ('1'..'9')
		return(1..length)
			.map { pola.random() }
			.joinToString("")
	}

	private fun ambilGaji(id : Int, token : String) {
		CoroutineScope(Dispatchers.Main).launch {
			val response = handlerAPI("https://lks.naar.my.id/api/Penggajian/$id", "PUT", token=token, typeRes = String::class.java)
			if (response != null) {
				Toast.makeText(this@SLipGajiAct, "Gaji Anda Sudah Di Cairkan!", Toast.LENGTH_SHORT).show()
				recreate()
			}
		}
	}

	fun formatUang(jumlah: Int): String {
		val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
		return formatRupiah.format(jumlah)
	}
}