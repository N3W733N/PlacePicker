package com.example.placepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeLiveData()
    }

    private fun subscribeLiveData() {
        main_place_picker.address.observe(this,
            { main_cep_txt.text = it })

        main_place_picker.isMapClicked.observe(this,
            { card_view_cep_layout.isVisible = !it })
    }
}