package com.example.dewapermana_smkn8jember

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dewapermana_smkn8jember.databinding.ActivityDetailTaskBinding
import com.example.dewapermana_smkn8jember.databinding.ItemTaskBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class DetailTaskActivity : AppCompatActivity() {
	private lateinit var binding: ActivityDetailTaskBinding
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityDetailTaskBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window)
		val idTask = intent.getIntExtra( "task_id", 0)
		val nameTask = intent.getStringExtra( "name_task")
		val tokenUser = checkedToken(this)
		if (idTask != 0 && tokenUser != ""){
			binding.nameTaskInDetail.text = nameTask
			CoroutineScope(Dispatchers.Main).launch {
				val url = handlerAPI("https://lks.naar.my.id/api/Task/$idTask","GET", token = tokenUser, typeRes = JSONArray::class.java)
				binding.rcTaskItem.layoutManager = LinearLayoutManager(this@DetailTaskActivity)
				val adapter = url?.let {
					adapter_task_item(it, object : adapter_task_item.OnTaskCheckedListener {
						override fun onTaskChecked(checkedTaskIds: List<Int>) {
							val Count = checkedTaskIds.size
							binding.markAsUwes.text = "($Count) Tandai Selesai"
						}
					})
				}
				binding.rcTaskItem.adapter = adapter
			}
		}

		binding.markAsUwes.setOnClickListener {
			val adapter = binding.rcTaskItem.adapter as adapter_task_item
			val allIdSelected = getTodoID(adapter)
			if (allIdSelected.isNotEmpty()) tokenUser?.let { it1 -> updateStatus(allIdSelected, it1) } else Toast.makeText(this@DetailTaskActivity, "Tidak ada tugas untuk diupdate!", Toast.LENGTH_SHORT).show()
		}
	}


	// Adapter
	class adapter_task_item(val data : JSONArray, val listener: OnTaskCheckedListener) : RecyclerView.Adapter<adapter_task_item.viewHolder>() {
		class viewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

		private val checkedTaskIds = mutableListOf<Int>()

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
			val binding = LayoutInflater.from(parent.context)
			val views = ItemTaskBinding.inflate(binding, parent, false)
			return viewHolder(views)
		}

		override fun getItemCount(): Int = data.length()

		override fun onBindViewHolder(holder: viewHolder, position: Int) {
			val items = data.getJSONObject(position)
			val taskId = items.getInt("id")
			val taskName = items.getString("todoTask1")
			val isSelesai = items.getInt("isSelesai")
			holder.binding.checkBoxTask.text = taskName
			holder.binding.checkBoxTask.isChecked = isSelesai == 1
			if (isSelesai == 1 || isSelesai == 2) {
				holder.binding.checkBoxTask.isChecked = true
				holder.binding.checkBoxTask.isEnabled = false
				holder.binding.checkBoxTask.alpha = 0.9f
			} else {
				holder.binding.checkBoxTask.isChecked = false
				holder.binding.checkBoxTask.isEnabled = true
				holder.binding.checkBoxTask.alpha = 1.0f
			}
			holder.binding.checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
				if (isChecked) {
					checkedTaskIds.add(taskId)
				} else {
					checkedTaskIds.remove(taskId)
				}
				listener.onTaskChecked(checkedTaskIds)
			}
		}

		fun getSelectedTaskIds(): List<Int> {
			return checkedTaskIds
		}

		interface OnTaskCheckedListener {
			fun onTaskChecked(checkedTaskIds: List<Int>)
		}
	}

	// Get ID yang Dicentang
	fun getTodoID(adapter: adapter_task_item) : List<Int>{
		return adapter.getSelectedTaskIds()
	}


	//Update Masal
	private fun updateStatus(todoID : List<Int>, token : String) {
		CoroutineScope(Dispatchers.Main).launch {
			try {
				todoID.forEach { id ->
					Log.d("Update Task", "Updating Task ID: $id")
					val res = handlerAPI("https://lks.naar.my.id/api/Task/update/$id", "PUT", token = token, typeRes = JSONObject::class.java)
					if (res != null) Toast.makeText(this@DetailTaskActivity, "Berhasil Mengupdate Status Tugas!", Toast.LENGTH_SHORT).show()
					recreate()
				}
			} catch (e: Exception) {
				Log.e("Error Di Detail Task", "Error Message: ${e.message}")
				Toast.makeText(
					this@DetailTaskActivity,
					"Error: ${e.message}",
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}
}