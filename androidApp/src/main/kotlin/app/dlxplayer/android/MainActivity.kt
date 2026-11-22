package app.dlxplayer.android

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import app.dlxplayer.shared.appDisplayName

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = appDisplayName
            textSize = 32f
            gravity = Gravity.CENTER
        })
    }
}
