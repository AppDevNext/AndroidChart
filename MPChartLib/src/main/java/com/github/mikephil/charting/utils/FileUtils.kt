package com.github.mikephil.charting.utils

import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

/**
 * Utilities class for interacting with the assets and the devices storage to
 * load and save DataSet objects from and to .txt files.
 *
 * @author Philipp Jahoda
 */
object FileUtils {
    private const val LOG = "MPChart-FileUtils"

    /**
     * Loads a an Array of Entries from a textfile from the sd-card.
     *
     * @param path the name of the file on the sd-card (+ path if needed)
     * @return
     */
    fun loadEntriesFromFile(path: String): MutableList<Entry?> {
        val sdcard = Environment.getExternalStorageDirectory()

        // Get the text file
        val file = File(sdcard, path)

        val entries: MutableList<Entry?> = ArrayList()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?

            while ((br.readLine().also { line = it }) != null) {
                val split: Array<String?> = line!!.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (split.size <= 2) {
                    entries.add(Entry(split[0]!!.toFloat(), split[1]!!.toInt().toFloat()))
                } else {
                    val vals = FloatArray(split.size - 1)

                    for (i in vals.indices) {
                        vals[i] = split[i]!!.toFloat()
                    }

                    entries.add(BarEntry(split[split.size - 1]!!.toInt().toFloat(), vals))
                }
            }
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        }

        return entries

        // File sdcard = Environment.getExternalStorageDirectory();
        //
        // // Get the text file
        // File file = new File(sdcard, path);
        //
        // List<Entry> entries = new ArrayList<Entry>();
        // String label = "";
        //
        // try {
        // @SuppressWarnings("resource")
        // BufferedReader br = new BufferedReader(new FileReader(file));
        // String line = br.readLine();
        //
        // // firstline is the label
        // label = line;
        //
        // while ((line = br.readLine()) != null) {
        // String[] split = line.split("#");
        // entries.add(new Entry(Float.parseFloat(split[0]),
        // Integer.parseInt(split[1])));
        // }
        // } catch (IOException e) {
        // Log.e(LOG, e.toString());
        // }
        //
        // DataSet ds = new DataSet(entries, label);
        // return ds;
    }

    /**
     * Loads an array of Entries from a textfile from the assets folder.
     *
     * @param am
     * @param path the name of the file in the assets folder (+ path if needed)
     * @return
     */
    @JvmStatic
    fun loadEntriesFromAssets(am: AssetManager, path: String): MutableList<Entry> {
        val entries: MutableList<Entry> = ArrayList()

        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(
                InputStreamReader(am.open(path), "UTF-8")
            )

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
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    Log.e(LOG, e.toString())
                }
            }
        }

        return entries

        // String label = null;
        // List<Entry> entries = new ArrayList<Entry>();
        //
        // BufferedReader reader = null;
        // try {
        // reader = new BufferedReader(
        // new InputStreamReader(am.open(path), "UTF-8"));
        //
        // // do reading, usually loop until end of file reading
        // label = reader.readLine();
        // String line = reader.readLine();
        //
        // while (line != null) {
        // // process line
        // String[] split = line.split("#");
        // entries.add(new Entry(Float.parseFloat(split[0]),
        // Integer.parseInt(split[1])));
        // line = reader.readLine();
        // }
        // } catch (IOException e) {
        // Log.e(LOG, e.toString());
        //
        // } finally {
        //
        // if (reader != null) {
        // try {
        // reader.close();
        // } catch (IOException e) {
        // Log.e(LOG, e.toString());
        // }
        // }
        // }
        //
        // DataSet ds = new DataSet(entries, label);
        // return ds;
    }

    /**
     * Saves an Array of Entries to the specified location on the sdcard
     *
     * @param entries
     * @param path
     */
    fun saveToSdCard(entries: MutableList<Entry>, path: String) {
        val sdcard = Environment.getExternalStorageDirectory()

        val saved = File(sdcard, path)
        if (!saved.exists()) {
            try {
                saved.createNewFile()
            } catch (e: IOException) {
                Log.e(LOG, e.toString())
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(saved, true))

            for (e in entries) {
                buf.append(e.y.toString() + "#" + e.x)
                buf.newLine()
            }

            buf.close()
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        }
    }

    @JvmStatic
    fun loadBarEntriesFromAssets(am: AssetManager, path: String): MutableList<BarEntry> {
        val entries: MutableList<BarEntry> = ArrayList()

        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(
                InputStreamReader(am.open(path), "UTF-8")
            )

            var line = reader.readLine()

            while (line != null) {
                // process line
                val split: Array<String?> = line.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                entries.add(BarEntry(split[1]!!.toFloat(), split[0]!!.toFloat()))

                line = reader.readLine()
            }
        } catch (e: IOException) {
            Log.e(LOG, e.toString())
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    Log.e(LOG, e.toString())
                }
            }
        }

        return entries

        // String label = null;
        // ArrayList<Entry> entries = new ArrayList<Entry>();
        //
        // BufferedReader reader = null;
        // try {
        // reader = new BufferedReader(
        // new InputStreamReader(am.open(path), "UTF-8"));
        //
        // // do reading, usually loop until end of file reading
        // label = reader.readLine();
        // String line = reader.readLine();
        //
        // while (line != null) {
        // // process line
        // String[] split = line.split("#");
        // entries.add(new Entry(Float.parseFloat(split[0]),
        // Integer.parseInt(split[1])));
        // line = reader.readLine();
        // }
        // } catch (IOException e) {
        // Log.e(LOG, e.toString());
        //
        // } finally {
        //
        // if (reader != null) {
        // try {
        // reader.close();
        // } catch (IOException e) {
        // Log.e(LOG, e.toString());
        // }
        // }
        // }
        //
        // DataSet ds = new DataSet(entries, label);
        // return ds;
    }
}
