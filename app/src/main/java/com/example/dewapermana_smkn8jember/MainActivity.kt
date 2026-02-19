package com.example.dewapermana_smkn8jember

import android.app.Dialog
import android.content.pm.PackageManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dewapermana_smkn8jember.databinding.ActivityMainBinding
import com.example.dewapermana_smkn8jember.databinding.ActivityProfileBinding
import com.example.dewapermana_smkn8jember.databinding.ItemRcTooBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.Permissions
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private val LOCATION_PERMISSION_REQUEST_CODE = 12341

	//Titik Kordinat Meja Barat
	private val MejaBarat = Location("").apply {
		latitude = -8.21362173
		longitude = 113.45854227
	}

	//Titik Kordinat Meja Timur
	private val MejaTimur = Location("").apply {
		latitude = -8.21361865
		longitude =  113.45858639
	}

	private val radius = 100f

	private lateinit var binding: ActivityMainBinding
	private lateinit var drawerLayout: DrawerLayout
	private lateinit var navView: NavigationView
	private lateinit var icon: ImageView
	private lateinit var nameDrawer: TextView
	private lateinit var photoDrawer: ImageView
	private lateinit var captureButton: Button
	private lateinit var sendAbsen: Button
	private lateinit var cameraExecutor: ExecutorService
	private var cameraCaptureSession: CameraCaptureSession? = null
	private lateinit var cameraDevice: CameraDevice
	private var currentPhotoPath: String = ""
	private lateinit var PreviewImageAbsen : ImageView
	private lateinit var biometricPrompt : BiometricPrompt
	private lateinit var promptInfo : BiometricPrompt.PromptInfo
	private lateinit var executor : Executor
	private lateinit var buttonToNextAbsen : Button
	private  var StatusAbsen : Int = 0
	private lateinit var ButtonIzin : Button
	private lateinit var ButtonPulang : Button
	private lateinit var ButtonSakit : Button
	private lateinit var TokenUser : String
	private lateinit var NameImage : String
	private var IdAbsen : Int = 0

	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window!!)

		//reset sesi jika beda hari
		clearSessionAbsen(this@MainActivity)

		val cartline =  findViewById<LineCartAbsensi>(R.id.lineChart)

		val sampleData = listOf(
			LineCartAbsensi.DataAbsensi("Sen", 10),
			LineCartAbsensi.DataAbsensi("Sel", 15),
			LineCartAbsensi.DataAbsensi("Rab", 12),
			LineCartAbsensi.DataAbsensi("Kam", 18),
			LineCartAbsensi.DataAbsensi("Jum", 14)
		)
		cartline.setData(sampleData)
		//deletedIdAbsen(this)

		IdAbsen = checkedIdAbsen(this)
		Log.d("Id Absen In Sharepref", "Ini : $IdAbsen")
		navView = binding.navView
		drawerLayout = binding.drawerLayout
		icon = binding.customIcon

		navView.apply {
			itemIconTintList = null
			itemBackground = null

			(getChildAt(0) as? RecyclerView)?.apply {
				addItemDecoration(object : RecyclerView.ItemDecoration() {
					override fun getItemOffsets(
						outRect: Rect,
						view: View,
						parent: RecyclerView,
						state: RecyclerView.State
					) {
						outRect.bottom = resources.getDimensionPixelSize(R.dimen.menu_item_spacing)
					}
				})
			}
		}



		val bottomNavLayout = layoutInflater.inflate(R.layout.nav_bottom_layout, navView, false)
		navView.addView(bottomNavLayout)

		(bottomNavLayout.layoutParams as ViewGroup.MarginLayoutParams).apply {
			topMargin = resources.getDimensionPixelSize(R.dimen.logout_top_margin)
		}

		bottomNavLayout.setOnClickListener {
			deleteToken(this@MainActivity)
		}
		//Set Greeting For User
		binding.greetings.text = greetings()
		val header = navView.getHeaderView(0)
		photoDrawer = header.findViewById(R.id.imgPictDrawer)
		nameDrawer = header.findViewById(R.id.user_name)
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		binding.btnToCheck.setOnClickListener {
			if (!checkLocation()) {
				return@setOnClickListener
			}
			if (!checkDiizinkan()){
				Toast.makeText(this@MainActivity, getReason(), Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			if (isIzinOrSakit(this@MainActivity)){
				Toast.makeText(this, "Anda Hari ini Tidak Hadir, tidak bisa absen!", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			if (sudahAbsenHariIni(this)){
				Toast.makeText(this, "Anda Sudah Melakukan Absen, Silahkan Coba Lagi Nanti Atau Besok!", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			if (IdAbsen == 0){
				showButtonDialog()
			}else{
				showModalsPulang()
			}
		}
		icon.setOnClickListener {
			if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
				drawerLayout.openDrawer(GravityCompat.END)
			}
		}

		TokenUser = checkedToken(this).toString()
		if (TokenUser.isNotEmpty()){
			CoroutineScope(Dispatchers.Main).launch {
				val respon = handlerAPI("https://lks.naar.my.id/api/Auth/me", "GET", token = TokenUser, typeRes = JSONArray::class.java )
				if (respon != null){
					for (i in 0 until respon.length()){
						val response = respon.getJSONObject(i)
						val namauser = response?.getString("nama")
						val namaImage = response?.getString("profilPict")
						binding.nameUser.text = namauser
						nameDrawer.text = namauser
						setImage(namaImage, binding.profilPict)
						setImage(namaImage, photoDrawer)
					}
				}

			}
		}

		navView.setNavigationItemSelectedListener { item ->
			when(item.itemId) {
				R.id.profilUser -> {
					pindah(this@MainActivity, ProfileActivity::class.java)
					drawerLayout.closeDrawer(GravityCompat.END)
					true
				}
				R.id.slipGaji -> {
					pindah(this@MainActivity, SLipGajiAct::class.java)
					drawerLayout.closeDrawer(GravityCompat.END)
					true
				}
				R.id.kehadiran -> {
					pindah(this@MainActivity, RiwayatAbsenAct::class.java)
					drawerLayout.closeDrawer(GravityCompat.END)
					true
				}
				R.id.todo -> {
					pindah(this@MainActivity, TodoActivity::class.java)
					drawerLayout.closeDrawer(GravityCompat.END)
					true
				}
				else -> false
			}
		}

		cameraExecutor = Executors.newSingleThreadExecutor()

		// fetch list task
		if (TokenUser.isNotEmpty()){
			CoroutineScope(Dispatchers.Main).launch {
				val res = handlerAPI("https://lks.naar.my.id/api/Task/me", "GET", token= TokenUser, typeRes = JSONArray::class.java)
				binding.rcUserTask.layoutManager = LinearLayoutManager(this@MainActivity)
				binding.rcUserTask.adapter = res?.let { adapterProgress(it) }
			}
		}
	}

	@Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
	override fun onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
			drawerLayout.closeDrawer(GravityCompat.END)
		} else {
			super.onBackPressed()
		}
	}

	@RequiresApi(Build.VERSION_CODES.O)
	fun greetings() : String {
		val time = LocalTime.now()
		return when{
			time in LocalTime.of(5, 0)..LocalTime.of(10, 59) -> "Hai, Selamat Pagi!"
			time in LocalTime.of(11, 0)..LocalTime.of(15, 0) -> "Hai, Selamat Siang!"
			time in LocalTime.of(15, 0)..LocalTime.of(18, 0) -> "Hai, Selamat Sore!"
			else -> "Hai, Selamat Malam!"
		}
	}


	// Kamera
	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
			if (isGranted) {
				startCamera()
			} else {
				Toast.makeText(this, "Izin kamera diperlukan!", Toast.LENGTH_SHORT).show()
			}
		}

	private fun requestCameraPermission() {
		when {
			ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
				startCamera()
			}
			else -> {
				requestPermissionLauncher.launch(Manifest.permission.CAMERA)
			}
		}
	}


	private fun startCamera() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "Izin kamera belum diberikan!", Toast.LENGTH_SHORT).show()
			return
		}
		val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
		val filephoto = createFile()
		val photoURI: Uri = FileProvider.getUriForFile(
			this, "com.example.dewapermana_smkn8jember.fileprovider", filephoto
		)
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
		val cameraId = cameraManager.cameraIdList.firstOrNull{
			id -> cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
		}

		if (cameraId != null){
			cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback(){
				override fun onOpened(camera: CameraDevice) {
					cameraDevice = camera
				}

				override fun onDisconnected(camera: CameraDevice) {
					camera.close()
				}

				override fun onError(camera: CameraDevice, error: Int) {
					camera.close()
				}
			}, null)
		} else {
			Toast.makeText(this, "Kamera belakang tidak ditemukan!", Toast.LENGTH_SHORT).show()
		}
		startActivityForResult(intent, 100)
	}

	@Deprecated("This method has been deprecated")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == 100 && resultCode == RESULT_OK) {
			try {
				val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

				val exif = ExifInterface(currentPhotoPath)
				val orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_UNDEFINED
				)

				val rotatedBitmap = when (orientation) {
					ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
					ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
					ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
					else -> bitmap
				}

				PreviewImageAbsen.setImageBitmap(rotatedBitmap)
				sendAbsen.visibility = View.VISIBLE
				captureButton.isEnabled = false
			} catch (e : Exception){
				Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
				Log.e("Error Absen Image : ", "Error Pesan : ${e.message}")
			}
			Toast.makeText(this, "Gambar disimpan di : $currentPhotoPath", Toast.LENGTH_SHORT).show()
		}
	}

	private fun stopCamera() {
		if (::cameraDevice.isInitialized) {
			cameraCaptureSession?.close()
			cameraDevice.close()
		} else {
			Toast.makeText(this, "Kamera belum diinisialisasi!", Toast.LENGTH_SHORT).show()
		}
	}

	fun createFile() : File{
		val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
		val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
		val nameUser = binding.nameUser.text.toString()
		val fileName = "${nameUser}__${timeStamp}_.jpg"
		val file = File(storageDir, fileName)
		currentPhotoPath = file.absolutePath
		NameImage = fileName
		return file
	}

	override fun onDestroy() {
		super.onDestroy()
		stopCamera()
	}

	fun rotateImage(src : Bitmap, angle : Float) : Bitmap {
		val matrix = Matrix()
		matrix.postRotate(angle)
		return Bitmap.createBitmap(
			src, 0, 0, src.width, src.height,
			matrix, true
		)
	}

	//Adapter Rc Progress
	class adapterProgress(val item : JSONArray) : RecyclerView.Adapter<adapterProgress.ViewHolder>(){
		class ViewHolder(val binding : ItemRcTooBinding) : RecyclerView.ViewHolder(binding.root)

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val view = LayoutInflater.from(parent.context)
			val binding = ItemRcTooBinding.inflate(view, parent, false)
			return ViewHolder(binding)
		}

		override fun getItemCount(): Int = item.length()

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val data = item.getJSONObject(position)
			holder.binding.nameTodo.text = data.getString("taskName")
			val sudahSelesai = data.getString("sudahSelesai")
			val belumSelesai = data.getString("belumSelesai")
			holder.binding.jmlahTask.text = "$sudahSelesai dari $belumSelesai Selesai"
			holder.binding.deadlineTask.text = data.getString("deadline")
			holder.binding.progressCircular.setProgress(sudahSelesai.toInt(),belumSelesai.toInt())
			holder.binding.toDetailTask.setOnClickListener {
				val context = holder.itemView.context
				val intent = Intent(context, DetailTaskActivity::class.java).apply {
					putExtra("task_id", data.getInt("taskID"))
					putExtra("name_task", data.getString("taskName"))
				}
				context.startActivity(intent)
			}
		}
	}

	//Set Logic Absen
	private fun setupBiometric() {
		val executor = ContextCompat.getMainExecutor(this)
		biometricPrompt = BiometricPrompt(this, executor,
			object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					super.onAuthenticationSucceeded(result)

					val imageFile = File(currentPhotoPath)
					if (imageFile.exists()) {
						sendImage(imageFile)
						sendAbsen(NameImage)
					}
				}

				override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
					super.onAuthenticationError(errorCode, errString)
					Toast.makeText(this@MainActivity,
						"Autentikasi gagal: $errString", Toast.LENGTH_SHORT)
						.show()
				}

				override fun onAuthenticationFailed() {
					super.onAuthenticationFailed()
					Toast.makeText(this@MainActivity,
						"Autentikasi gagal", Toast.LENGTH_SHORT)
						.show()
				}
			})

		promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Verifikasi Sidik Jari")
			.setSubtitle("Verifikasi sidik jari untuk mengirim foto")
			.setNegativeButtonText("Batal")
			.build()
	}

	fun sendImage(fileImage: File) {
		val main = checkedToken(this@MainActivity)
		//val imageName = fileImage.name

		CoroutineScope(Dispatchers.IO).launch {
			try {
				val boundary = "------WebKitFormBoundary" + System.currentTimeMillis()
				val url = URL("https://lks.naar.my.id/api/PictureImage")
				val connection = url.openConnection() as HttpURLConnection

				connection.requestMethod = "POST"
				connection.doOutput = true
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
				connection.setRequestProperty("Authorization", "Bearer $main")

				val outputStream = connection.outputStream
				val writer = outputStream.bufferedWriter()

				writer.write("--$boundary\r\n")
				writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"${fileImage.name}\"\r\n")
				writer.write("Content-Type: image/jpg\r\n\r\n")
				writer.flush()

				FileInputStream(fileImage).use { fileInput ->
					fileInput.copyTo(outputStream)
				}

				writer.write("\r\n--$boundary--\r\n")
				writer.flush()
				writer.close()
				outputStream.close()

				// Cek response
				val responseCode = connection.responseCode
				withContext(Dispatchers.Main) {
					if (responseCode == HttpURLConnection.HTTP_OK) {
						captureButton.isEnabled = true
						sendAbsen.visibility = View.GONE
						Toast.makeText(this@MainActivity, "Berhasil Upload Absen!", Toast.LENGTH_SHORT).show()
						recreate()
					} else {
						val response = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown Error"
						Toast.makeText(this@MainActivity, "Upload gagal: $response", Toast.LENGTH_SHORT).show()
					}
				}
			} catch (e: Exception) {
				Log.e("Upload Error", "Error: ${e.message}", e)
				withContext(Dispatchers.Main) {
					Toast.makeText(this@MainActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	//Check Status
	fun showButtonDialog(){
		val dialog = Dialog(this)
		val view = LayoutInflater.from(this).inflate(R.layout.if_user_masuk, null)
		dialog.setContentView(view)
		buttonToNextAbsen = view.findViewById(R.id.nextBtn)
		ButtonIzin = view.findViewById(R.id.reqIzin)
		ButtonSakit = view.findViewById(R.id.reqSakit)
		buttonToNextAbsen.setOnClickListener {
			StatusAbsen = 1
			isNextAbsen()
			Log.d("Debug Status absen", "Status saat Ini : $StatusAbsen")
		}
		ButtonSakit.setOnClickListener {
			StatusAbsen = 6
			isNextAbsen()
			Log.d("Debug Status absen", "Status saat Ini (Sakit diKlik) : $StatusAbsen")
		}
		ButtonIzin.setOnClickListener {
			StatusAbsen = 5
			isNextAbsen()
			Log.d("Debug Status absen", "Status saat Ini (Izin diKlik) : $StatusAbsen")
		}
		dialog.setCancelable(true)
		dialog.show()
	}

	//I want to Absen Func
	private fun isNextAbsen(){
		val dialog = Dialog(this)
		val view = LayoutInflater.from(this).inflate(R.layout.modals_absen, null)
		dialog.setContentView(view)
		captureButton = view.findViewById(R.id.captureButton)
		PreviewImageAbsen = view.findViewById(R.id.hasilImage)
		sendAbsen = view.findViewById(R.id.SendButton)
		sendAbsen.visibility = View.GONE

		// Absen selain Selfie Juga Sidik Jari
		setupBiometric()

		captureButton.setOnClickListener {
			requestCameraPermission()
		}

		sendAbsen.setOnClickListener {
			biometricPrompt.authenticate(promptInfo)
		}
		dialog.setCancelable(true)
		dialog.show()

		dialog.setOnDismissListener {
			if (::cameraDevice.isInitialized) {
				stopCamera()
			}
		}
	}

	//Send Absen
	fun sendAbsen( pathimg : String){
		CoroutineScope(Dispatchers.Main).launch {
			if (IdAbsen != 0 ){
				val reqPulang = JSONObject().apply {
					put("absen_id", IdAbsen)
					put("selfieKeluar", pathimg)
				}
				val response = handlerAPI("https://lks.naar.my.id/api/Absensi/Harian/me/keluar", "PUT", reqBody = reqPulang, token=TokenUser, typeRes = JSONObject::class.java)
				Log.d("Debug ID Absen Pulang", "Response Absensi Pulang : $response")
				if (response != null){
					Toast.makeText(this@MainActivity, "Berhasil Absen Pulang!", Toast.LENGTH_SHORT).show()
					deletedIdAbsen(this@MainActivity)
					saveAbsenSession(this@MainActivity)
				}
			}else{
				val req = JSONObject().apply {
					put("status_id", StatusAbsen)
					put("selfie", pathimg)
				}
				val response = handlerAPI("https://lks.naar.my.id/api/Absensi/Harian/me/masuk", "POST", reqBody = req, token=TokenUser, typeRes = JSONObject::class.java)
				Log.d("Debug ID Absen", "Response Absensi Masuk : $response")
				if (response != null){
					val idAbsen = response.getInt("id")
					val statusID = response.getInt("statusId")
					if (statusID == 1){
						savedIdAbsen(idAbsen)
					} else {
						val setStatus = when(statusID) {
							5 -> "Izin"
							6 -> "Sakit"
							else -> "Invalid"
						}
						saveStatusAbsen(this@MainActivity, setStatus)
					}
					saveAbsenSession(this@MainActivity)
				}
			}
		}
	}

	//Save ID Absen
	private fun savedIdAbsen(read: Int) {
		val sharedPref = getSharedPreferences("IdAbsensi", MODE_PRIVATE)
		sharedPref.edit().putInt("id_absen", read).apply()
		Log.d("Id Absen", "Saved id Absen : $read")
	}

	//Delete Jika Sudah
	private fun deletedIdAbsen(context: Context) {
		val sharedPref = getSharedPreferences("IdAbsensi", MODE_PRIVATE)
			.edit().remove("id_absen").apply()
		Log.d("Token", "Delete Token: $sharedPref")
		return sharedPref
	}

	//Check id
	fun checkedIdAbsen(context : Context): Int {
		val sharedPref = context.getSharedPreferences("IdAbsensi", MODE_PRIVATE)
		val id = sharedPref.getInt("id_absen", 0)
		Log.d("Id Absen", "Check IdAben: $id")
		return id
	}

	//Modal Mau Pulang
	fun showModalsPulang(){
		val dialog = Dialog(this)
		val view = LayoutInflater.from(this).inflate(R.layout.modals_mau_pulang, null)
		dialog.setContentView(view)
		ButtonPulang = view.findViewById(R.id.BtnMauPulang)
		ButtonPulang.setOnClickListener {
			isNextAbsen()
		}
		dialog.setCancelable(true)
		dialog.show()
	}

	//Kondisi Absen Hnaya daapat diakses pada hari Senin - Jum'at
	fun checkDiizinkan() : Boolean {
		val calendar = Calendar.getInstance()
		val jam = calendar.get(Calendar.HOUR_OF_DAY)
		val menit = calendar.get(Calendar.MINUTE)
		val hari = calendar.get(Calendar.DAY_OF_WEEK)
		if (hari == Calendar.SATURDAY || hari == Calendar.SUNDAY) return false
		val CheckPagi = (jam == 6 && menit >= 30) || (jam == 7) || (jam == 8 && menit == 0)
		val CheckSore = (jam == 15 && menit >= 30) || (jam == 16) || (jam == 17 && menit == 0)
		return CheckPagi || CheckSore
	}

	//Buat Pesan
	fun getReason(): String {
		val calendar = Calendar.getInstance()
		val hari = calendar.get(Calendar.DAY_OF_WEEK)

		if (hari == Calendar.SATURDAY || hari == Calendar.SUNDAY) {
			return "Absen tidak bisa dilakukan pada hari Sabtu dan Minggu."
		}
		return "Absen hanya bisa dilakukan pada pukul 06:30 - 08:00 dan 15:30 - 17:00."
	}

	//Simpan Sesi Absen
	fun saveAbsenSession(context: Context) {
		val sharedPref = context.getSharedPreferences("Absensi", MODE_PRIVATE)
		val editor = sharedPref.edit()

		val calendar = Calendar.getInstance()
		val jam = calendar.get(Calendar.HOUR_OF_DAY)

		val sesiAbsen = if (jam in 6..8) "pagi" else "sore"
		val today = calendar.get(Calendar.DAY_OF_YEAR)

		if (sudahAbsenHariIni(context)) return

		editor.putString("jam_absen", sesiAbsen)
		editor.putLong("tanggal_terakhir_absen", today.toLong())

		editor.apply()
	}


	//Buat User Hanya bisa absen Sekali baik datang maupun pulang
	fun sudahAbsenHariIni(context: Context): Boolean {
		val sharedPref = context.getSharedPreferences("Absensi", MODE_PRIVATE)

		val lastAbsen = sharedPref.getString("jam_absen", null)
		val lastAbsenDate = sharedPref.getLong("tanggal_terakhir_absen", -1)

		val calendar = Calendar.getInstance()
		val jam = calendar.get(Calendar.HOUR_OF_DAY)
		val today = calendar.get(Calendar.DAY_OF_YEAR)

		val sesiSekarang = if (jam in 6..8) "pagi" else "sore"

		return lastAbsen == sesiSekarang && lastAbsenDate == today.toLong()
	}

	//Check Status Absen
	fun saveStatusAbsen(context: Context, status: String) {
		val sharedPref = context.getSharedPreferences("Absensi", MODE_PRIVATE)
		val editor = sharedPref.edit()

		editor.putString("status_absen", status)
		editor.apply()
	}

	//Simpan Sesi User Sakit / izin
	fun isIzinOrSakit(context: Context): Boolean {
		val sharedPref = context.getSharedPreferences("Absensi", MODE_PRIVATE)
		val statusAbsen = sharedPref.getString("status_absen", "")
		return statusAbsen == "Izin" || statusAbsen == "Sakit"
	}

	//Reset Semua Sessi Absen Jika Berbeda Hari
	fun clearSessionAbsen(context: Context) {
		val sharedPref = context.getSharedPreferences("Absensi", MODE_PRIVATE)
		val editor = sharedPref.edit()
		val tanggal_terakhir = sharedPref.getLong("tanggal_terakhir_absen", -1)
		val calendar = Calendar.getInstance()
		val today = calendar.get(Calendar.DAY_OF_YEAR)
		if (tanggal_terakhir != today.toLong()) {
			editor.remove("jam_absen")
			editor.remove("status_absen")
			editor.remove("tanggal_terakhir_absen")
			editor.apply()
		}
	}

	//Check Permission
	private fun checkPermission() : Boolean{
		if (ContextCompat.checkSelfPermission(
			this, Manifest.permission.ACCESS_FINE_LOCATION
		) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
				LOCATION_PERMISSION_REQUEST_CODE
			)
			return false
		}
		return true
	}

	//Logic Geolocation
	private fun checkLocation() : Boolean{
		if (checkPermission()) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
			{
				var isInsideRadius = false
				fusedLocationClient.lastLocation.addOnSuccessListener { location ->
					location?.let {
						val jarakDariiBarat = location.distanceTo(MejaBarat)
						val jarakDariTimur = location.distanceTo(MejaTimur)

						if (jarakDariiBarat <= radius || jarakDariTimur <= radius)
						{
							isInsideRadius = true
							//Toast.makeText(this, "Anda berada dalam radius yang diizinkan", Toast.LENGTH_SHORT).show()
						} else {
							Toast.makeText(this, "Tidak dapat melakukan Absensi, anda berada di luar radius yang diizinkan! Jarak terdekat: ${minOf(jarakDariiBarat, jarakDariTimur).toInt()} meter", Toast.LENGTH_LONG).show()
						}
					} ?: run {
						Toast.makeText(this, "Mohon Aktifkan GPS anda!", Toast.LENGTH_SHORT).show()
					}
				}
				return isInsideRadius
			}
		}
		return false
	}


	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray,
		deviceId: Int
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
		when (requestCode) {
			LOCATION_PERMISSION_REQUEST_CODE -> {
				if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					checkLocation()
				} else {
					Toast.makeText(this, "Izin lokasi diperlukan!", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

}