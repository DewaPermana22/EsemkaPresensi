package com.example.dewapermana_smkn8jember

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class LineCartAbsensi : View {
	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	data class DataAbsensi(val date: String, val count: Int)

	private var dataList = emptyList<DataAbsensi>()
	private val points = mutableListOf<PointF>()

	private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		textSize = 30f
		textAlign = Paint.Align.CENTER
	}

	// Margins dan spacing
	private val margins = object {
		val top = 50f
		val bottom = 100f
		val left = 100f
		val right = 50f
	}

	// Spacing constants
	private val valueTextOffset = 15f
	private val dateTextOffset = 25f

	fun setData(data: List<DataAbsensi>) {
		dataList = data
		calculatePoints()
		invalidate()
	}

	private fun calculatePoints() {
		if (dataList.isEmpty()) return
		points.clear()

		val maxCount = dataList.maxOf { it.count }
		val minCount = dataList.minOf { it.count }.coerceAtMost(0)
		val chartWidth = width - margins.left - margins.right
		val chartHeight = height - margins.top - margins.bottom
		val xStep = chartWidth / (dataList.size - 1)
		val yRatio = chartHeight / (maxCount - minCount)

		dataList.forEachIndexed { index, data ->
			points.add(PointF(
				margins.left + (index * xStep),
				height - margins.bottom - ((data.count - minCount) * yRatio)
			))
		}
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		calculatePoints()
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		if (points.isEmpty()) return

		// Draw grid
		paint.apply {
			color = Color.LTGRAY
			strokeWidth = 1f
			style = Paint.Style.STROKE
		}
		drawGrid(canvas)

		// Draw line
		paint.apply {
			color = Color.RED
			strokeWidth = 5f
		}
		points.zipWithNext { a, b ->
			canvas.drawLine(a.x, a.y, b.x, b.y, paint)
		}

		// Draw points and text
		points.forEachIndexed { index, point ->
			// Points
			paint.apply {
				style = Paint.Style.FILL
			}
			canvas.drawCircle(point.x, point.y, 8f, paint)

			// Text
			paint.apply {
				color = Color.BLACK
				style = Paint.Style.FILL
			}

			// Date label (di bawah)
			canvas.drawText(
				dataList[index].date,
				point.x,
				height - margins.bottom + dateTextOffset,
				paint
			)

			// Count label (di atas titik)
			canvas.drawText(
				dataList[index].count.toString(),
				point.x,
				point.y - valueTextOffset,
				paint
			)
		}
	}

	private fun drawGrid(canvas: Canvas) {
		val yStep = (height - margins.top - margins.bottom) / 4
		repeat(5) { i ->
			val y = margins.top + (yStep * i)
			canvas.drawLine(margins.left, y, width - margins.right, y, paint)
		}

		val xStep = (width - margins.left - margins.right) / (points.size - 1)
		repeat(points.size) { i ->
			val x = margins.left + (xStep * i)
			canvas.drawLine(x, margins.top, x, height - margins.bottom, paint)
		}
	}
}