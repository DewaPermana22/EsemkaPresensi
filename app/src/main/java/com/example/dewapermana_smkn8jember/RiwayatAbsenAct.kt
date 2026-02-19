package com.example.dewapermana_smkn8jember

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dewapermana_smkn8jember.MainActivity.adapterProgress
import com.example.dewapermana_smkn8jember.databinding.ActivityRiwayatAbsenBinding
import com.example.dewapermana_smkn8jember.databinding.ItemRcLogAbsenBinding
import com.example.dewapermana_smkn8jember.databinding.ItemRcTooBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class RiwayatAbsenAct : AppCompatActivity() {
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityRiwayatAbsenBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window)
		binding.backButton.setOnClickListener {
			val intent = Intent(this@RiwayatAbsenAct, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
			finish()
		}

		val tokenUser = checkedToken(this).toString()
		CoroutineScope(Dispatchers.Main).launch {
			val respon = handlerAPI("https://lks.naar.my.id/api/Auth/me", "GET", token = tokenUser, typeRes = JSONArray::class.java )
			for (i in 0 until respon?.length()!!){
				val response = respon.getJSONObject(i)
				val namauser = response?.getString("nama")
				val Pictuser = response?.getString("profilPict")
				binding.namaPekerja.text = namauser
				setImage(Pictuser, binding.pictProfilUser)
			}

		}

		//Fetch Riwayat Absen User
		CoroutineScope(Dispatchers.Main).launch {
			val response = handlerAPI("https://lks.naar.my.id/api/Absensi/user", "GET", token = tokenUser, typeRes = JSONArray::class.java )
			binding.rcRiwayatAbsen.layoutManager = LinearLayoutManager(this@RiwayatAbsenAct)
			binding.rcRiwayatAbsen.adapter = response?.let { RiwayatAbsen(it) }
		}
	}

	class RiwayatAbsen(val item : JSONArray) : RecyclerView.Adapter<RiwayatAbsen.ViewHolder>(){
		class ViewHolder(val binding : ItemRcLogAbsenBinding) : RecyclerView.ViewHolder(binding.root)

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val view = LayoutInflater.from(parent.context)
			val binding = ItemRcLogAbsenBinding.inflate(view, parent, false)
			return ViewHolder(binding)
		}

		override fun getItemCount(): Int = item.length()

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val data = item.getJSONObject(position)
			val statusID = data.optInt("statusId")
			if (statusID != 1) {
				holder.binding.statusAsben.text = "Tidak Hadir"
				holder.binding.imgIsAbsen.setImageResource(R.drawable.tidak_masuk)
			} else{
				holder.binding.statusAsben.text = "Hadir"
			}
			val hari = data.optString("tanggal")
			val nulDays = if (!hari.isNullOrEmpty())  "Hari : $hari" else "Hari : -"
			holder.binding.hariAbsen.text = nulDays

			val jamMasuk = data.optString("waktuMasuk")
			val jamKeluar = data.optString("waktuKeluar")
			val kondisi = if (!jamMasuk.isNullOrEmpty() || !jamKeluar.isNullOrEmpty()) "Masuk : $jamMasuk | Keluar : $jamKeluar" else	"Masuk : - | Keluar : -"
			holder.binding.dateTimeAbsen.text = kondisi
			holder.itemView.setOnClickListener {
				val context = holder.itemView.context
				val intent = Intent(context, DetailAbsenAct::class.java)
				intent.putExtra("id_absen", data.getInt("id"))
				intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
				context.startActivity(intent)

			}
		}
	}
}