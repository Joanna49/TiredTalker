package com.maciejka.tiredtalker
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
class Sinusoida (context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = context.getColor(R.color.darkest_blue)
        strokeWidth = 4f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSinusoid(canvas)
    }

    private fun drawSinusoid(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val amplitude = height / 4
        val frequency = 2 * Math.PI / width

        var x = 0f
        val yMid = height / 2

        while (x <= width) {
            val y = yMid + amplitude * sin(frequency * x).toFloat()
            canvas.drawPoint(x, y, paint)
            x += 1f
        }
    }
}