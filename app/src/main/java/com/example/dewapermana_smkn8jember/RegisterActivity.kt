package com.example.dewapermana_smkn8jember

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.dewapermana_smkn8jember.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executor

class RegisterActivity : AppCompatActivity() {
	private lateinit var biometricPrompt : BiometricPrompt
	private lateinit var promptInfo : BiometricPrompt.PromptInfo
	private lateinit var executor : Executor
	private lateinit var txtEmail : String
	private lateinit var txtUsername : String
	private lateinit var txtPassword : String
	private lateinit var txtKode : String
	private lateinit var logBiometric : String
	private var logGagal : Int	= 0
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityRegisterBinding.inflate(layoutInflater)
		setContentView(binding.root)

		executor = ContextCompat.getMainExecutor(this)


		binding.buttonToLogin.setOnClickListener {
			pindah(this@RegisterActivity, LoginScr::class.java)
			finish()
		}

		binding.buttonRegister.setOnClickListener {
			txtEmail = binding.etEmail.text.toString()
			txtUsername = binding.etUserName.text.toString()
			txtPassword = binding.etPassword.text.toString()
			txtKode = binding.etKodeReveral.text.toString()

			authIsSupport()
		// Logika : Jika Berhasil Mendaftar Maka User diharuskan Untuk Scan sidik jari / muncul Dialog
		}
	}

	private fun register(logBiometric : String){
		CoroutineScope(Dispatchers.IO).launch {
			val url = URL("https://lks.naar.my.id/api/Auth/Register").openConnection() as HttpURLConnection
			url.setRequestProperty("Content-Type", "application/json")
			url.requestMethod = "POST"
			url.doOutput = true
			val req = JSONObject().apply {
				put("email", txtEmail)
				put("username", txtUsername)
				put("password", txtPassword)
				put("kodereveral", txtKode)
				put("biometrics", logBiometric)
			}
			url.outputStream.write(req.toString().toByteArray())
			val resCode = url.responseCode
			if (resCode == HttpURLConnection.HTTP_OK){
				val response = InputStreamReader(url.inputStream).readText()
				savedToken(response)
				Log.d("Response Api : ", "Response Code : $resCode")
				Log.d("Response Api : ", "Response Token : $response")
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
				register(logBiometric)
				saveSqlLite(logBiometric)
				Toast.makeText(this@RegisterActivity, "Berhasil Merekam Sidik Jari!", Toast.LENGTH_SHORT).show()
				logGagal = 0
				startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
			}

			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				logGagal++
				Toast.makeText(this@RegisterActivity, "Gagal Merekam Sidik Jari!", Toast.LENGTH_SHORT).show()

				if (logGagal >= 3){
					Toast.makeText(this@RegisterActivity, "Autentikasi ditolak. Silakan coba lagi nanti.", Toast.LENGTH_SHORT).show()
				}
			}
		})
		biometricPrompt.authenticate(promptInfo)
	}

	fun saveSqlLite(log : String){
		val db = SqlLite(this)
		db.insertUser(txtUsername ,txtEmail, log)
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