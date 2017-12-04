package com.kongdy.percentprogressbar.ui

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import com.kongdy.percentprogressbar.R
import com.kongdy.view.KProgressBarData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var dataArray:  SparseArray<KProgressBarData> = SparseArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // load data
        val kp1 = KProgressBarData()
        kp1.color = Color.RED
        kp1.value = 10f

        val kp2 = KProgressBarData()
        kp2.color = Color.BLUE
        kp2.value = 40f

        val kp3 = KProgressBarData()
        kp3.color = Color.GREEN
        kp3.value = 60f

        dataArray.put(0,kp1)
        dataArray.put(1,kp2)
        dataArray.put(2,kp3)

        val centerView: View = LayoutInflater.from(this).inflate(R.layout.layout_ppb_center_view,null)

        ppb_test.centerView = centerView
        ppb_test.setDataArray(dataArray)
    }
}
