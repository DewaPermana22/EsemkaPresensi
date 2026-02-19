package com.example.dewapermana_smkn8jember

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.S)
fun setStatusBar(window: Window) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		window.insetsController?.let { controller ->
			controller.hide(WindowInsets.Type.statusBars()) // Menghilangkan status bar
			controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
		}
	} else {
		@Suppress("DEPRECATION")
		window.decorView.systemUiVisibility = (
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				)
		window.statusBarColor = android.graphics.Color.TRANSPARENT
	}
}

fun pindah(context: Context, ac : Class<*>){
	context.startActivity(Intent(context, ac))
}

fun checkedToken(context : Context): String? {
	val sharedPref = context.getSharedPreferences("SessionUser", MODE_PRIVATE)
	val token = sharedPref.getString("token_session", null)
	Log.d("Token", "Check Token: $token")
	return token
}

fun deleteToken(context: Context){
	val sharedPref = context.getSharedPreferences("SessionUser", MODE_PRIVATE)
		.edit().remove("token_session").apply()
	Log.d("Token", "Delete Token: $sharedPref")
	return sharedPref
}

suspend fun <T>handlerAPI(url: String, req: String, token : String? = null, reqBody: JSONObject? = null, typeRes : Class<T>) : T? {
	return try {
		withContext(Dispatchers.IO) {
			val connection = URL(url).openConnection() as HttpURLConnection
			connection.requestMethod = req
			connection.setRequestProperty("Accept", "application/json")

			if (token != null) {
				connection.setRequestProperty("Authorization", "Bearer $token")
			}

			if (req == "POST" || req == "PUT") {
				connection.setRequestProperty("Content-Type", "application/json")
				connection.doOutput = true
				if (reqBody != null) {
					connection.outputStream.use { os ->
						os.write(reqBody.toString().toByteArray())
						os.flush()
					}
				}
			}
			val responseCode = connection.responseCode
			Log.d("Rescode In Utils ApiHelper", "Response code: $responseCode")

			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
				val response = connection.inputStream.bufferedReader().use { it.readText() }

				try {
					when(typeRes) {
						String::class.java -> response as T
						JSONArray::class.java -> JSONArray(response) as T
						JSONObject::class.java -> JSONObject(response) as T
						else -> throw IllegalArgumentException("Request Not Suppport : ${typeRes.name}")
					}
				} catch (e: Exception) {
					Log.e("ApiHelper", "Failed to parse response: ${e.message}")
					throw e
				}
			} else {
				val errorResponse = connection.errorStream?.bufferedReader()?.readText()
				Log.e("ApiHelper", "HTTP Error $responseCode: $errorResponse")
				null
			}
		}
	} catch (e: Exception) {
		Log.e("ApiHelper", "Network error: ${e.message}")
		null
	} finally {
	}
}

fun setImage(name: String?, image: ImageView) {
	CoroutineScope(Dispatchers.IO).launch {
		try {
			if (!name.isNullOrEmpty())
			{
				val url = URL("https://lks.naar.my.id/uploads/$name").openStream()
				val img = BitmapFactory.decodeStream(url)
				withContext(Dispatchers.Main) {
					image.setImageBitmap(img)
				}
			}
		} catch (e : Exception){
			Log.e("Debug", "Image Error : ${e.message}")
		}
	}
}