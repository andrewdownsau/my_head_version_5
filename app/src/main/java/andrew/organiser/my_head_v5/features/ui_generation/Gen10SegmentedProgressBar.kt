package andrew.organiser.my_head_v5.features.ui_generation

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable


class Gen10SegmentedProgressBar(
    private val parts: Long,
    private val fillColor: Int,
    private val emptyColor: Int,
    private val separatorColor: Int
) :
    Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cutOffWidth = 0

    override fun onLevelChange(level: Int): Boolean {
        invalidateSelf()
        return true
    }

    override fun draw(canvas: Canvas) {
        // Calculate full progress bar bounds
        val bounds = bounds
        val progressBarFullWidth = bounds.width().toFloat()
        val progressBarHeight = bounds.height().toFloat()
        cutOffWidth = (level * progressBarFullWidth / 10000).toInt()


        //Calculate the width of each block and segment based on number of parts
        val fullBlockWidth = (progressBarFullWidth / this.parts)
        val segmentPortion = when(parts){
            1L -> 1F
            in 2L..5L -> 0.95F
            in 6L..15L -> 0.9F
            in 16L..35L -> 0.8F
            else -> 0.7F }
        val segmentWidth = fullBlockWidth * segmentPortion

        //Draw separator fill as background
        val fullBox = RectF(0f, 0f, progressBarFullWidth, progressBarHeight)
        paint.color = this.separatorColor
        canvas.drawRect(fullBox, this.paint)

        //Start drawing the filled and empty segments depending on Level
        //var startX = fullBlockWidth * (1 - segmentPortion)
        var startX = 0F
        for (i in 0 until this.parts) {
            val endX = if(i != parts - 1) startX + segmentWidth else startX + fullBlockWidth

            //in ideal condition this would be the rectangle
            val part = RectF(startX, 0f, endX, progressBarHeight)

            //if the segment is below level the paint color should be fill color
            if ((startX + segmentWidth) <= cutOffWidth) {
                paint.color = this.fillColor
                canvas.drawRect(part, this.paint)
            } else if (startX < cutOffWidth) {
                val part1 = RectF(startX, 0f, cutOffWidth.toFloat(), progressBarHeight)
                paint.color = this.fillColor
                canvas.drawRect(part1, this.paint)

                val part2 = RectF(
                    cutOffWidth.toFloat(),
                    0f,
                    (startX + segmentWidth),
                    progressBarHeight
                )
                paint.color = this.emptyColor
                canvas.drawRect(part2, this.paint)
            } else {
                paint.color = this.emptyColor
                canvas.drawRect(part, this.paint)
            }

            //update the startX to start the new segment with the gap of divider and segment width
            startX += fullBlockWidth
        }
    }


    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(cf: ColorFilter?) {
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}