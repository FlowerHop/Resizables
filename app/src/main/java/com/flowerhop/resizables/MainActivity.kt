package com.flowerhop.resizables

import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        panZoomView.setRoi(Roi(
            rectF = RectF(0f, 0f, contentView.layoutParams.width.toFloat(), contentView.layoutParams.height.toFloat()),
            30f
        ))

        panZoomView.onSizeListener = object : PanZoomView.OnSizeListener {
            override fun onSizeChanging(roi: Roi) {
                contentView.layoutParams.let {
                    it.width  = roi.rectF.width().toInt()
                    it.height = roi.rectF.height().toInt()
                }

                contentView.rotation = roi.degree
                contentView.x = roi.rectF.left
                contentView.y = roi.rectF.top
                contentView.requestLayout()
            }

            override fun onSizeChanged(roi: Roi) {
                Log.e("TAG", "onSizeChanged: $roi" )
            }
        }
        panZoomView.invalidate()
    }

}