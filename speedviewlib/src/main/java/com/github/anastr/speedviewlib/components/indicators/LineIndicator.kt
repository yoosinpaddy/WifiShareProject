package com.github.anastr.speedviewlib.components.indicators

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * this Library build By Anas Altair
 * see it on [GitHub](https://github.com/anastr/SpeedView)
 */
class LineIndicator(context: Context, private val mode: Float) : Indicator<LineIndicator>(context) {

    private val indicatorPath = Path()

    init {
        width = dpTOpx(8f)
    }

    override fun getBottom(): Float {
        return getCenterY() * mode
    }

    override fun draw(canvas: Canvas, degree: Float) {
        canvas.save()
        canvas.rotate(90f + degree, getCenterX(), getCenterY())
        canvas.drawPath(indicatorPath, indicatorPaint)
        canvas.restore()
    }

    override fun updateIndicator() {
        indicatorPath.reset()
        indicatorPath.moveTo(getCenterX(), speedometer!!.padding.toFloat())
        indicatorPath.lineTo(getCenterX(), getCenterY() * mode)

        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = width
        indicatorPaint.color = color
    }

    override fun setWithEffects(withEffects: Boolean) {
        if (withEffects && !speedometer!!.isInEditMode) {
            indicatorPaint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.SOLID)
        } else {
            indicatorPaint.maskFilter = null
        }
    }

    companion object {
        const val LINE = 1f
        const val HALF_LINE = .5f
        const val QUARTER_LINE = .25f
    }
}
