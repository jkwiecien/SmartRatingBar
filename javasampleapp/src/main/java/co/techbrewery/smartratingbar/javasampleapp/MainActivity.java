package co.techbrewery.smartratingbar.javasampleapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import co.techbrewery.smartratingbar.SmartRatingBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmartRatingBar ratingBar = findViewById(R.id.smartRatingBar);
        ratingBar.setOnRatingChangedListener(new SmartRatingBar.OnRatingChangedListener() {
            @Override
            public void onRatingChanged(float rating) {
                Log.v("SmartRatingBar", "On rating changed: " + rating);
            }
        });
    }
}
