package com.github.mikephil.charting.utils

import android.content.res.AssetManager
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Utilities class for interacting with the assets and the devices storage to load and save DataSet objects from and to .txt files.
 */
object FileUtils {
    private const val LOG = "Chart-FileUtils"

    /**
     * Loads an array of Entries from a textfile from the assets folder.
     *
     * @param path the name of the file in the assets folder (+ path if needed)
     */
    fun loadEntriesFromAssets(am: AssetManager, path: String): MutableList<Entry> {
        val entries: MutableList<Entry> = ArrayList()

        try {
            BufferedReader(
                InputStreamReader(am.open(path), StandardCharsets.UTF_8)
            ).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    // process line
                    val split: Array<String?> = line.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (split.size <= 2) {
                        entries.add(Entry(split[1]!!.toFloat(), split[0]!!.toFloat()))
                    } else {
                        val vals = FloatArray(split.size - 1)

                        for (i in vals.indices) {
                            vals[i] = split[i]!!.toFloat()
                        }

                        entries.add(BarEntry(split[split.size - 1]!!.toInt().toFloat(), vals))
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        }

        return entries
    }

    fun loadBarEntriesFromAssets(am: AssetManager, path: String): MutableList<BarEntry> {
        val entries: MutableList<BarEntry> = ArrayList()

        try {
            BufferedReader(InputStreamReader(am.open(path), StandardCharsets.UTF_8)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val split: Array<String?> = line.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    entries.add(BarEntry(split[1]!!.toFloat(), split[0]!!.toFloat()))
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        }

        return entries
    }
}
