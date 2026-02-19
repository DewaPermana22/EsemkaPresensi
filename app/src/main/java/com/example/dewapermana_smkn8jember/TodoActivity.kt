package com.example.dewapermana_smkn8jember

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.dewapermana_smkn8jember.databinding.ActivityTodoBinding
import com.example.dewapermana_smkn8jember.databinding.ChildTaskBinding
import com.example.dewapermana_smkn8jember.databinding.TaskNameExpandBinding
import com.google.android.material.animation.AnimationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class TodoActivity : AppCompatActivity() {
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityTodoBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setStatusBar(window!!)
		val token = checkedToken(this)
		binding.expandItem.setGroupIndicator(null)
		CoroutineScope(Dispatchers.Main).launch {
			val response = handlerAPI("https://lks.naar.my.id/api/Task/status/me", "GET", token = token, typeRes = JSONArray::class.java)
			binding.expandItem.setAdapter(response?.let { expanListAdapter(this@TodoActivity, it) })
		}

		binding.expandItem.setOnGroupExpandListener { gp ->
			binding.expandItem.setSelection(gp)
		}
	}

	//Adapter for Expanabled List
	class expanListAdapter(val context: Context, val data : JSONArray, ) : BaseExpandableListAdapter(){
		override fun getGroupCount(): Int = data.length()

		override fun getChildrenCount(groupPosition: Int) = run {
			try {
				val grup = data.getJSONObject(groupPosition)
				grup.getJSONArray("itemTask").length()
			} catch (e : Exception) {
				0
				Log.e("Debug Error in Todo Activity", "Error : ${e.message}")
			}
		}

		override fun getGroup(groupPosition: Int): Any {
			return data.getJSONObject(groupPosition)
		}

		override fun getChild(groupPosition: Int, childPosition: Int): Any {
			val group = data.getJSONObject(groupPosition)
			return group.getJSONArray("itemTask").getJSONObject(childPosition)
		}

		override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

		override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()


		override fun hasStableIds(): Boolean = false

		override fun getGroupView(
			groupPosition: Int,
			isExpanded: Boolean,
			convertView: View?,
			parent: ViewGroup?
		): View {
			val binding = if (convertView == null){
				TaskNameExpandBinding.inflate(LayoutInflater.from(context), parent, false)
			} else {
				TaskNameExpandBinding.bind(convertView)
			}

			val item = data.getJSONObject(groupPosition)
			binding.txtTaskName.text = item.getString("taskName")

			val setImage = if (isExpanded == true) R.drawable.arrow_up else R.drawable.arrow_down
			binding.transitionClip.setImageResource(setImage)

			return binding.root
		}

		override fun getChildView(
			groupPosition: Int,
			childPosition: Int,
			isLastChild: Boolean,
			convertView: View?,
			parent: ViewGroup?
		): View {
			val binding = if (convertView == null){
				ChildTaskBinding.inflate(LayoutInflater.from(context), parent, false)
			} else {
				ChildTaskBinding.bind(convertView)
			}


			val child = data.getJSONObject(groupPosition).getJSONArray("itemTask").getJSONObject(childPosition)
			binding.childTask.text = child.getString("todoTask1")
			val isSelesai = child.getInt("isSelesai")
			when(isSelesai){
				0 -> binding.statusChildTask.text = "Belum Selesai"
				1 -> binding.statusChildTask.text = "Pending / Cek"
				2 -> binding.statusChildTask.text = "Selesai"
			}

			return binding.root
		}

		override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

	}
}