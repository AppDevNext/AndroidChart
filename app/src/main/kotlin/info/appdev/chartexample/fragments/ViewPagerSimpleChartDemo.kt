package info.appdev.chartexample.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import info.appdev.chartexample.R
import info.appdev.chartexample.databinding.ActivityAwesomedesignBinding
import info.appdev.chartexample.notimportant.DemoBase

/**
 * Demonstrates how to keep your charts straight forward, simple and beautiful with the AndroidChart library.
 */
class ViewPagerSimpleChartDemo : DemoBase() {

    private lateinit var binding: ActivityAwesomedesignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwesomedesignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.offscreenPageLimit = 3
        binding.pager.adapter = PageAdapter(this)

        showSnackbar("Swipe left and right for more awesome design examples!")
    }

    private class PageAdapter(activity: ViewPagerSimpleChartDemo) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SineCosineFragment.newInstance()
                1 -> ComplexityFragment.newInstance()
                2 -> BarChartFrag.newInstance()
                3 -> ScatterChartFrag.newInstance()
                4 -> PieChartFrag.newInstance()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }

        override fun getItemCount(): Int {
            return 5
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/fragments/SimpleChartDemo.java".toUri()
                startActivity(i)
            }
        }
        return true
    }

    /* Intentionally left empty */
    public override fun saveToGallery() = Unit

    private fun showSnackbar(text: String) {
        val viewPos: View = findViewById(android.R.id.content)
        val snackbar = Snackbar.make(viewPos, text, Snackbar.LENGTH_SHORT)
        val view = snackbar.view
        when (val params = view.layoutParams) {
            is CoordinatorLayout.LayoutParams -> {
                val paramsC = view.layoutParams as CoordinatorLayout.LayoutParams
                paramsC.gravity = Gravity.CENTER_VERTICAL
                view.layoutParams = paramsC
                snackbar.show()
            }

            is FrameLayout.LayoutParams -> {
                val paramsC = view.layoutParams as FrameLayout.LayoutParams
                paramsC.gravity = Gravity.BOTTOM
                view.layoutParams = paramsC
                snackbar.show()
            }

            else -> {
                Toast.makeText(this, text + " " + params.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
