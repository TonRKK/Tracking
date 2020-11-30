package com.chornerman.whereismystuff.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chornerman.whereismystuff.BuildConfig.VERSION_CODE
import com.chornerman.whereismystuff.BuildConfig.VERSION_NAME
import com.chornerman.whereismystuff.R
import com.chornerman.whereismystuff.api.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_tracking_status.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_END_POINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val couriers: Array<Pair<String, String>> by lazy {
        arrayOf(
            Pair("kerryexpress-th", "Kerry Express"),
            Pair("jt-express-th", "J&T Express"),
            Pair("flashexpress", "Flash Express"),
            // TODO Add more couriers here
        )
    }

    private var currentCourierPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tilCouriersSelector.editText?.setText(couriers[currentCourierPos].second)
        bindViewEvents()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindViewEvents() {
        tvAppTitle.setOnClickListener {
            Toast.makeText(
                applicationContext, "v$VERSION_NAME ($VERSION_CODE)", LENGTH_SHORT
            ).show()
        }

        btTrackNow.setOnClickListener {
            with(btTrackNow.text) {
                if (this == TRACK_NOW_BUTTON_TRACK_NOW) {
                    tilTrackingNumber.editText?.text.toString()
                        .takeUnless { it.isEmpty() }?.let {
                            trackNow(
                                trackingNumber = it,
                                courierCode = couriers[currentCourierPos].first
                            )
                        }
                } else if (this == TRACK_NOW_BUTTON_TRACK_MORE) {
                    trackMore()
                }
            }
        }

        tilCouriersSelector.editText?.setOnClickListener {
            showCouriersSelectorDialog()
        }

        layProgressBar.setOnTouchListener { _, _ ->
            true
        }
    }

    private fun showCouriersSelectorDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Select courier")
            setItems(couriers.map { it.second }.toList().toTypedArray()) { _, which ->
                tilCouriersSelector.editText?.setText(couriers[which].second)
                currentCourierPos = which
            }
            create()
            show()
        }
    }

    private fun trackNow(trackingNumber: String, courierCode: String) {
        toggleProgressBar(true)

        retrofit.create(TrackingService::class.java)
            .getTracking(
                trackingNumber = trackingNumber,
                carrierCode = courierCode
            ).enqueue(object : Callback<TrackingResponse> {
                override fun onResponse(
                    call: Call<TrackingResponse>, response: Response<TrackingResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.data?.let {
                            if (it.status != null) {
                                displayTrackingStatus(it)
                            } else {
                                toastError()
                            }
                        }
                    } else {
                        toastError(
                            "Unable to retrieve tracking information. Error code is ${response.code()}"
                        )
                    }
                    toggleProgressBar(false)
                }

                override fun onFailure(call: Call<TrackingResponse>, t: Throwable) {
                    toastError()
                    toggleProgressBar(false)
                }
            })
    }

    private fun toggleProgressBar(isShowing: Boolean) {
        layProgressBar.apply {
            visibility = if (isShowing) VISIBLE else GONE
        }
    }

    private fun displayTrackingStatus(data: TrackingResponse.Data) {
        hideSoftKeyboard()

        with(data) {
            layTrackingStatus.visibility = VISIBLE
            status?.run {
                tvTrackingStatus.text = capitalize()
                ivTrackingStatus.setImageResource(
                    when {
                        this == TRACKING_STATUS_DELIVERED -> {
                            R.drawable.ic_round_check_circle_56
                        }
                        this in TRACKING_STATUS_ERROR -> {
                            R.drawable.ic_round_error_56
                        }
                        else -> {
                            R.drawable.ic_round_airport_shuttle_56
                        }
                    }
                )
            }
            val summary = StringBuilder()
                .takeIf { lastEvent != null }?.append("${lastEvent}")
            // TODO Add more summaries here, please add '\n\n' immediately AFTER '}' if it is NOT the last item
            tvTrackingStatusSummary.takeUnless { summary.isNullOrEmpty() }?.text =
                summary.toString()
            enableInputs(false)
            btTrackNow.text = TRACK_NOW_BUTTON_TRACK_MORE
        }
    }

    private fun hideSoftKeyboard() {
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(View(this).windowToken, 0)
    }

    private fun trackMore() {
        layTrackingStatus.visibility = GONE
        tvTrackingStatus.text = ""
        tilTrackingNumber.editText?.setText("")
        enableInputs(true)
        btTrackNow.text = TRACK_NOW_BUTTON_TRACK_NOW
    }

    private fun toastError(errMessage: String = "Unable to retrieve tracking information") {
        Toast.makeText(applicationContext, "Error : $errMessage", LENGTH_SHORT).show()
    }

    private fun enableInputs(isEnabled: Boolean) {
        tilTrackingNumber.editText?.isEnabled = isEnabled
        tilCouriersSelector.editText?.isEnabled = isEnabled
    }
}

private const val API_END_POINT = "https://api.trackingmore.com"
private const val TRACK_NOW_BUTTON_TRACK_NOW = "track now"
private const val TRACK_NOW_BUTTON_TRACK_MORE = "track more"
