package com.example.iaso  // Updated to match your package

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import java.util.*
import java.util.Locale

class ContributionGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val activityData = mutableMapOf<String, Int>().apply {
        // Sample data adjusted to early 2025 to match grid start
        put("2025-01-01", 1)
        put("2025-01-08", 2)
        put("2025-01-15", 3)
        put("2025-01-22", 5)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val monthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dpToPx(monthTextSizeDp)
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dpToPx(dayTextSizeDp)
        color = Color.BLACK
    }

    private val levelColors = intArrayOf(
        Color.WHITE,                    // 0: no activity
        "#eeeeee".toColorInt(),         // 1: light gray
        "#d6d6d6".toColorInt(),         // 2: medium light gray
        "#b3b3b3".toColorInt(),         // 3: medium gray
        "#808080".toColorInt(),         // 4: dark gray
        Color.BLACK                     // 5: full activity
    )

    private val rows = 7
    private val cols = 53
    private val cellSizeDp = 12f
    private val paddingDp = 20f
    private val monthTextSizeDp = 12f
    private val dayTextSizeDp = 10f

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = dpToPx(cellSizeDp)
        val padding = dpToPx(paddingDp)

        // Draw month labels (top) - adjusted for year starting Jan
        val months = listOf("Jan", "Apr", "Jul", "Oct")
        var monthX = padding + (cols / 4f * cellSize) / 2  // Center of first quarter
        months.forEach { month ->
            canvas.drawText(month, monthX, padding - dpToPx(2f), monthPaint)
            monthX += (cols / 4f * cellSize)  // Next quarter
        }

        // Draw day labels (left)
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        days.forEachIndexed { index, day ->
            val y = padding + (index * cellSize) + cellSize / 2
            canvas.drawText(day, dpToPx(5f), y, dayPaint)
        }

        // Draw grid squares
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = padding + col * cellSize
                val y = padding + row * cellSize
                val date = calculateDateForCell(col, row)
                val level = activityData[date] ?: 0
                paint.color = levelColors[level.coerceIn(0, 5)]
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paint)
            }
        }
    }

    private fun calculateDateForCell(col: Int, row: Int): String {
        val calendar = Calendar.getInstance(Locale.getDefault()).apply {
            set(2025, Calendar.SEPTEMBER, 1)  // Start from Sep 1, 2025
            add(Calendar.DAY_OF_YEAR, col * 7 + row)
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day)
    }

    fun updateData(newData: Map<String, Int>) {
        activityData.clear()
        activityData.putAll(newData)
        invalidate()  // Redraw
    }
}