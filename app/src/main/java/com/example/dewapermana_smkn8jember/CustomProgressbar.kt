package com.example.dewapermana_smkn8jember

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat

class CircularProgressBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

	private var progress = 0
	private var maxProgress = 100

	private val paintBackground = Paint().apply {
		color = Color.LTGRAY
		style = Paint.Style.STROKE
		strokeWidth = 30f
		isAntiAlias = true
	}

	private val paintProgress = Paint().apply {
		color = Color.parseColor("#FFC107") // Warna progress (kuning)
		style = Paint.Style.STROKE
		strokeWidth = 30f
		isAntiAlias = true
		strokeCap = Paint.Cap.ROUND
	}

	private val paintText = Paint().apply {
		color = Color.BLACK
		textSize = 55f
		textAlign = Paint.Align.CENTER
		isAntiAlias = true
		typeface = ResourcesCompat.getFont(context, R.font.poppins_semibold)
	}

	// Fungsi untuk mengatur progress
	fun setProgress(current: Int, max: Int) {
		progress = current
		maxProgress = max
		invalidate() // Redraw
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		val centerX = width / 2f
		val centerY = height / 2f
		val radius = (width / 2f) - 30f

		// Gambar lingkaran background
		canvas.drawCircle(centerX, centerY, radius, paintBackground)

		// Hitung sudut untuk progress
		val sweepAngle = (progress.toFloat() / maxProgress) * 360

		// Gambar progress
		canvas.drawArc(
			centerX - radius, centerY - radius,
			centerX + radius, centerY + radius,
			-90f, sweepAngle, false, paintProgress
		)

		// Gambar teks persen
		val percentage = (progress.toFloat() / maxProgress * 100).toInt()
		canvas.drawText("$percentage%", centerX, centerY + 15f, paintText)
	}
}