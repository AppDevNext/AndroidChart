package info.appdev.chartexample.notimportant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import info.appdev.charting.charts.Chart

open class DemoBaseCompose : ComponentActivity() {
    protected var tfLight: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize typeface
        tfLight = Typeface.createFromAsset(assets, "OpenSans-Light.ttf")

        // Hide status bars using modern WindowCompat API
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    protected fun <T : Chart<*>> saveToGallery(chart: T) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (chart.saveToGallery("HorizontalBarChartActivity_" + System.currentTimeMillis(), 70)) {
                Toast.makeText(this, "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Saving FAILED!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request permission or show message
            Toast.makeText(this, "Permission required to save", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun viewGithub() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/HorizontalBarChartActivity.kt".toUri()
        startActivity(i)
    }
}
