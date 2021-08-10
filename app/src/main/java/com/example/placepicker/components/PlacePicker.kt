package com.example.placepicker.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.airbnb.lottie.LottieAnimationView
import com.example.placepicker.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.*

class PlacePicker : ConstraintLayout, OnMapReadyCallback {

    private var lottie: LottieAnimationView
    private lateinit var geocoder: Geocoder
    var isMapClicked = MutableLiveData<Boolean>()
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    var city = MutableLiveData<String>()
    private var isStarting = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    init {
        View.inflate(context, R.layout.place_picker, this)
        refreshDrawableState()
        val mapFragment = (context as FragmentActivity).supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        lottie = findViewById(R.id.lottie)
    }

    private fun initView(attrs: AttributeSet?) {
        attrs ?: return

        val attributeValues = context.obtainStyledAttributes(attrs, R.styleable.PlacePicker)
        with(attributeValues) {
            try {
                lottie.frame = 15
            } finally {
                recycle()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        geocoder = Geocoder(context, Locale.getDefault())
        val zoom = 10.0f
        var mPosition: LatLng
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION) or
                !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1111
                )
            }
            return
        }
        googleMap.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
            if (isStarting) {
                mPosition = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPosition, zoom))
                isStarting = false
            }
        }

        lottie.setMinAndMaxFrame(0, 15)

        googleMap.setOnCameraMoveStartedListener {
            lottie.speed = -1F
            lottie.playAnimation()
            isMapClicked.postValue(true)
        }

        googleMap.setOnCameraIdleListener {
            lottie.speed = 1F
            lottie.playAnimation()
            isMapClicked.postValue(false)
            mPosition = googleMap.cameraPosition.target

            val addresses: List<Address> = geocoder.getFromLocation(
                mPosition.latitude,
                mPosition.longitude,
                1
            )
            latitude.postValue(mPosition.latitude)
            longitude.postValue(mPosition.longitude)
            if (addresses.isNotEmpty()) {
                addresses[0].subAdminArea?.let {
                    city.postValue(it)
                }
            }
        }
    }
}
