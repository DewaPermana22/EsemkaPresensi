package com.example.dewapermana_smkn8jember

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.dewapermana_smkn8jember.databinding.ActivityLoginScrBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executor

class LoginScr : AppCompatActivity() {
	private lateinit var biometricPrompt : BiometricPrompt
	private lateinit var promptInfo : BiometricPrompt.PromptInfo
	private lateinit var executor : Executor
	private lateinit var txtEmail : String
	private lateinit var txtUsername : String
	private lateinit var txtPassword : String
	private lateinit var txtKode : String
	private lateinit var logBiometric : String
	private var logGagal : Int	= 0
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityLoginScrBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window)
		executor = ContextCompat.getMainExecutor(this)

		//deleteToken(this) //FA63CF2

		//Check Session //DAE6E3B
		//testinguser@email.com
		//bismilllah
		val token = checkedToken(this)
		if (token != null){
			startActivity(Intent(this@LoginScr, MainActivity::class.java))
			finish()
		} else {
			Toast.makeText(this@LoginScr,"Please Login to best experience!", Toast.LENGTH_SHORT).show()
		}


		// Inisialisasi komponen
		val checkBox = binding.checkBox

		binding.buttonLogin?.setOnClickListener {
			txtEmail = binding.etEmailLogin?.text.toString()
			txtPassword = binding.etPw?.text.toString()
			txtKode = binding.etKodeReferalLogin?.text.toString()
			authIsSupport()
		}

		binding.buttonToRegister?.setOnClickListener { pindah(this@LoginScr, RegisterActivity::class.java) }

		when (checkBox?.isChecked) {
			true -> {
				binding.buttonLogin?.apply {
					visibility = View.VISIBLE
					isEnabled = true
				}
			}
			false,null -> {
				binding.buttonLogin?.apply {
					visibility = View.VISIBLE
					isEnabled = false
				}
			}
		} // Check Jika User tidak meng centang Privacy Policiy maka di disable

		checkBox?.setOnCheckedChangeListener { _, isChecked ->
			when (isChecked) {
				true -> {
					binding.buttonLogin?.apply {
						visibility = View.VISIBLE
						isEnabled = true
					}
				}

				false, null -> {
					binding.buttonLogin?.apply {
						visibility = View.VISIBLE
						isEnabled = false
					}
				}
			}
		} // Jika user setuju lanjut ke logika selanjutnya
	}

	private fun login(logBiometric : String){
		CoroutineScope(Dispatchers.IO).launch {
			val url = URL("https://lks.naar.my.id/api/Auth/Login").openConnection() as HttpURLConnection
			url.setRequestProperty("Content-Type", "application/json")
			url.requestMethod = "POST"
			url.doOutput = true
			val req = JSONObject().apply {
				put("email", txtEmail)
				put("password", txtPassword)
				put("kodereveral", txtKode)
				put("biometrics", logBiometric)
			}
			url.outputStream.write(req.toString().toByteArray())
			val resCode = url.responseCode
			if (resCode == HttpURLConnection.HTTP_OK){
				val response = InputStreamReader(url.inputStream).readText()
				Log.d("Response Api : ", "Response Code : $resCode")
				savedToken(response)
				Log.d("Response Api : ", "Response Token : $response")
			} else {

			}
		}
	}

	fun generateValue() : String{
		return UUID.randomUUID().toString()
	}

	private fun showDialogPrompt(){
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Autentikasi Sidik Jari")
			.setSubtitle("Gunakan sidik jari untuk verifikasi")
			.setNegativeButtonText("Batal")
			.build()
		biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){

			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				logBiometric = generateValue()
				login(logBiometric)
				val db = SqlLite(this@LoginScr)

				if (db.verifyUser(txtEmail, logBiometric)) {
					login(logBiometric)
					Toast.makeText(this@LoginScr, "Verifikasi sidik jari berhasil!", Toast.LENGTH_SHORT).show()
					logGagal = 0
					startActivity(Intent(this@LoginScr, MainActivity::class.java))
					finish()
				}/* else {
					Toast.makeText(this@LoginScr, "Sidik jari tidak cocok dengan data yang terdaftar", Toast.LENGTH_SHORT).show()
				}*/

				Toast.makeText(this@LoginScr, "Berhasil Merekam Sidik Jari!", Toast.LENGTH_SHORT).show()
				logGagal = 0
				startActivity(Intent(this@LoginScr, MainActivity::class.java))
			}

			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				logGagal++
				Toast.makeText(this@LoginScr, "Gagal Merekam Sidik Jari!", Toast.LENGTH_SHORT).show()

				if (logGagal >= 3){
					Toast.makeText(this@LoginScr, "Autentikasi ditolak. Silakan coba lagi nanti.", Toast.LENGTH_SHORT).show()
				}
			}
		})
		biometricPrompt.authenticate(promptInfo)
	}

	private fun authIsSupport(){
		val biometric = BiometricManager.from(this)
		when(biometric.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)){
			BiometricManager.BIOMETRIC_SUCCESS -> {
				showDialogPrompt()
			}
			BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
				Toast.makeText(this, "Perangkat tidak memiliki sensor biometric", Toast.LENGTH_SHORT).show()
			}
			BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
				Toast.makeText(this, "Sensor biometric tidak tersedia", Toast.LENGTH_SHORT).show()
			}
			BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
				Toast.makeText(this, "Belum ada sidik jari yang terdaftar", Toast.LENGTH_SHORT).show()
			}
			else -> {
				Toast.makeText(this, "Perangkat tidak mendukung biometric", Toast.LENGTH_SHORT).show()
			}
		}
	}
	private fun savedToken(read: String) {
		val sharedPref = getSharedPreferences("SessionUser", MODE_PRIVATE)
		sharedPref.edit().putString("token_session", read).apply()
		Log.d("Token", "Saved Token: $read")
	}
}