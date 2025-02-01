package com.example.work_time_note.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomBarChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintBar = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val paintLabel = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private var chartData: List<Pair<String, Int>> = emptyList()

    /**
     * Nastavení dat pro graf
     */
    fun setChartData(data: List<Pair<String, Int>>) {
        chartData = data
        invalidate() // Znovu vykreslit graf
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (chartData.isEmpty()) return

        // Výpočet parametrů pro vykreslení grafu
        val barWidth = width / (chartData.size * 2) // Šířka sloupce
        val maxValue = chartData.maxOf { it.second } // Největší hodnota

        chartData.forEachIndexed { index, (label, value) ->
            val left = index * 2 * barWidth + barWidth / 2
            val right = left + barWidth
            val barHeight = if (maxValue > 0) height * value / maxValue else 0
            val top = height - barHeight
            val bottom = height.toFloat()

            // Kreslení sloupce
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom, paintBar)

            // Kreslení popisků
            canvas.drawText(label, (left + right) / 2f, height + 40f, paintLabel) // Popis osy X
            canvas.drawText(value.toString(), (left + right) / 2f, top - 20f, paintLabel) // Hodnota nad sloupcem
        }
    }
}
