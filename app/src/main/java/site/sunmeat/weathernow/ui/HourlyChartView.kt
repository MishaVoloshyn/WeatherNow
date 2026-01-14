package site.sunmeat.weathernow.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HourlyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 1f
        alpha = 70
    }

    private var temps: List<Double> = emptyList()

    fun setData(values: List<Double>) {
        temps = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (temps.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()

        val left = paddingLeft.toFloat()
        val right = (width - paddingRight).toFloat()
        val top = paddingTop.toFloat()
        val bottom = (height - paddingBottom).toFloat()

        // grid (2 линии)
        val midY = (top + bottom) / 2f
        canvas.drawLine(left, midY, right, midY, gridPaint)
        canvas.drawLine(left, bottom, right, bottom, gridPaint)

        val minT = temps.minOrNull() ?: return
        val maxT = temps.maxOrNull() ?: return
        val span = max(1e-6, (maxT - minT))

        fun x(i: Int): Float {
            val t = i.toFloat() / (temps.size - 1).toFloat()
            return left + (right - left) * t
        }

        fun y(value: Double): Float {
            val norm = ((value - minT) / span).toFloat()
            // сверху больше
            return bottom - (bottom - top) * norm
        }

        val path = Path()
        path.moveTo(x(0), y(temps[0]))
        for (i in 1 until temps.size) {
            path.lineTo(x(i), y(temps[i]))
        }

        // цвета берем из текущей темы (через текущий textColor/foreground нельзя просто — упрощаем)
        // пусть будет белая линия, а в light — темная: делаем адаптивно по яркости фона сложно,
        // поэтому используем альфу: выглядит норм и там и там.
        linePaint.alpha = 220
        dotPaint.alpha = 230

        canvas.drawPath(path, linePaint)

        // точки
        val r = resources.displayMetrics.density * 3f
        for (i in temps.indices) {
            canvas.drawCircle(x(i), y(temps[i]), r, dotPaint)
        }
    }
}
