
package com.github.mikephil.charting.utils;

import android.content.res.AssetManager;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities class for interacting with the assets and the devices storage to
 * load and save DataSet objects from and to .txt files.
 *
 * @author Philipp Jahoda
 */
public class FileUtils {

	private static final String LOG = "Chart-FileUtils";

	/**
	 * Loads an array of Entries from a textfile from the assets folder.
	 *
	 * @param path the name of the file in the assets folder (+ path if needed)
	 */
	public static List<Entry> loadEntriesFromAssets(AssetManager am, String path) {

		List<Entry> entries = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(am.open(path), StandardCharsets.UTF_8))) {

			String line = reader.readLine();

			while (line != null) {
				// process line
				String[] split = line.split("#");

				if (split.length <= 2) {
					entries.add(new Entry(Float.parseFloat(split[1]), Float.parseFloat(split[0])));
				} else {

					float[] vals = new float[split.length - 1];

					for (int i = 0; i < vals.length; i++) {
						vals[i] = Float.parseFloat(split[i]);
					}

					entries.add(new BarEntry(Integer.parseInt(split[split.length - 1]), vals));
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			Log.e(LOG, e.toString());

		}

		return entries;
	}

	public static List<BarEntry> loadBarEntriesFromAssets(AssetManager am, String path) {

		List<BarEntry> entries = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(am.open(path), StandardCharsets.UTF_8))) {

			String line = reader.readLine();

			while (line != null) {
				// process line
				String[] split = line.split("#");

				entries.add(new BarEntry(Float.parseFloat(split[1]), Float.parseFloat(split[0])));

				line = reader.readLine();
			}
		} catch (IOException e) {
			Log.e(LOG, e.toString());

		}

		return entries;
	}
}
