package site.sunmeat.weathernow.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class TempChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val temps = mutableListOf<Float>()

    fun setTemps(values: List<Float>) {
        temps.clear()
        temps.addAll(values)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (temps.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()

        val minT = temps.minOrNull() ?: return
        val maxT = temps.maxOrNull() ?: return
        val range = max(1f, maxT - minT)

        // padding
        val left = 24f
        val right = 24f
        val top = 24f
        val bottom = 24f

        val usableW = w - left - right
        val usableH = h - top - bottom

        // простая авто-раскраска: берем currentTextColor через theme (без выбора цветов руками)
        // Paint цвет по умолчанию — черный, но в темной теме будет плохо.
        // Хитрим: берём цвет из View's context theme через android.R.attr.textColorPrimary.
        paintLine.color = resolveTextColorPrimary()
        paintDot.color = paintLine.color

        val stepX = usableW / (temps.size - 1)

        var prevX = left
        var prevY = top + usableH * (1f - ((temps[0] - minT) / range))

        for (i in 1 until temps.size) {
            val x = left + stepX * i
            val y = top + usableH * (1f - ((temps[i] - minT) / range))
            canvas.drawLine(prevX, prevY, x, y, paintLine)
            prevX = x
            prevY = y
        }

        // точки
        for (i in temps.indices) {
            val x = left + stepX * i
            val y = top + usableH * (1f - ((temps[i] - minT) / range))
            canvas.drawCircle(x, y, 6f, paintDot)
        }
    }

    private fun resolveTextColorPrimary(): Int {
        val a = intArrayOf(android.R.attr.textColorPrimary)
        val ta = context.obtainStyledAttributes(a)
        val color = ta.getColor(0, 0xFFFFFFFF.toInt())
        ta.recycle()
        return color
    }
}
