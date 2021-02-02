package co.techbrewery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.techbrewery.smartratingbar.SmartRatingBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        smartRatingBar.maxRating = 5
        smartRatingBar.onRatingChangedListener = object : SmartRatingBar.OnRatingChangedListener {
            override fun onRatingChanged(rating: Float) {
//                Log.v("SmartRatingBar", "On rating changed: $rating")
            }
        }
    }
}
