package info.appdev.chartexample.notimportant

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.core.view.size
import info.appdev.charting.charts.Chart
import com.google.android.material.snackbar.Snackbar
import info.appdev.chartexample.R
import java.text.DateFormatSymbols

abstract class DemoBase : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    protected val parties: Array<String> = arrayOf(
        "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
        "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
        "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
        "Party Y", "Party Z"
    )

    protected var tfRegular: Typeface? = null

    protected var tfLight: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        optionMenus.clear()

        tfRegular = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf")
        tfLight = Typeface.createFromAsset(assets, "OpenSans-Light.ttf")

        title = this.javaClass.asSubclass(this.javaClass).simpleName.replace("Activity", "")

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onStart() {
        super.onStart()

        // Hide status bars using modern WindowCompat API
        // Note: We don't call setDecorFitsSystemWindows(false) because these activities
        // use traditional AppCompat ActionBar which needs to fit within system windows
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            for (i in 0 until menu.size) {
                val menuItem: MenuItem = menu[i]
                optionMenus.add(menuItem.title.toString())
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_CLOSE,
                    R.anim.move_left_in_activity,
                    R.anim.move_right_out_activity
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity)
            }
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery()
            } else {
                Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    protected fun requestStoragePermission(view: View?) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(view!!, "Write permission is required to save image to gallery", Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        this@DemoBase,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_STORAGE
                    )
                }
                .show()
        } else {
            Toast.makeText(applicationContext, "Permission Required!", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this@DemoBase, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE)
        }
    }

    protected fun saveToGallery(chart: Chart<*>?, name: String) {
        chart?.let {
            if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70))
                Toast.makeText(applicationContext, "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT).show()
        }
    }

    protected abstract fun saveToGallery()

    companion object {
        private const val PERMISSION_STORAGE = 0

        //  Jan, Feb,... Dec
        val months = DateFormatSymbols().months.toList().map { it.take(3) }
        val optionMenus: MutableList<String> = mutableListOf()
    }
}
