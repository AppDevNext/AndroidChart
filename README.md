[![](https://jitpack.io/v/AppDevNext/AndroidChart.svg)](https://jitpack.io/#AppDevNext/AndroidChart)
[![PullRequest](https://github.com/AppDevNext/AndroidChart/actions/workflows/pullrequest.yml/badge.svg)](https://github.com/AppDevNext/AndroidChart/actions/workflows/pullrequest.yml)

:zap: A powerful & easy to use 100% Kotlin chart library for Android :zap:

It comes with library view based `chartLib` and composeable `chartLibCompose`

### Packagename changed ! 🛑 

During the full Kotlin conversion, 
* 4.0 🛑 package name changed to `info.appdev.charting`
* 4.0 🛑 `MPPointF` in renamed to `PointF`
* 4.0 🛑 `MPPointD` in renamed to `PointD`
* 4.1 🛑 methods like `setSomethingEnabled(true)` are now properties like `isSomething = true`
* 5.0 🛑 Remove legacy package name
* 5.1 🛑 `Entry` is now an `EntryFloat`, btw, there is now a `EntryDouble` as well
* 5.1 🛑 `BarEntry` is now an `BarEntryFloat`, btw, there is now a `BarEntryDouble` as well
* 5.1 🛑 `BubbleEntry` is now an `BubbleEntryFloat`
* 5.1 🛑 `PieEntry` is now an `PieEntryFloat`
* 5.1 🛑 `RadarEntry` is now an `RadarEntryFloat`
* 5.1 🛑 `CandleEntry` is now an `CandleEntryFloat`

### Project status: maintenance mode
Issues are ignored, but pull requests are not. If you need to get something done, submit a PR!

### Gradle Setup with jitpack.io

```gradle.kts
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.AppDevNext.AndroidChart:chartLib:VERSION")
    // or for compose
    implementation("com.github.AppDevNext.AndroidChart:chartLibCompose:VERSION")
}
```

### Gradle Setup with Maven Central [snapshot]

Currently only from Maven Central snapshot staging is provided
```gradle
repositories {
    maven { url 'https://central.sonatype.com/api/v1/publisher/deployments/download/' }
}

dependencies {
    implementation 'info.mxtracks:chart:${latestVersion}-SNAPSHOT'
}
```

**LineChart (with legend, simple design)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/simpledesign_linechart4.png)
<br/><br/>

**LineChart (with legend, simple design)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/simpledesign_linechart3.png)
<br/><br/>

**LineChart (cubic lines)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/cubiclinechart.png)
<br/><br/>

**LineChart (gradient fill)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/line_chart_gradient.png)
<br/><br/>

**BarChart (with legend, simple design)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/simpledesign_barchart3.png)
<br/><br/>

**BarChart (grouped DataSets)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/groupedbarchart.png)
<br/><br/>

**Horizontal-BarChart**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/horizontal_barchart.png)
<br/><br/>

**Combined-Chart (bar- and linechart in this case)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/combined_chart.png)
<br/><br/>

**PieChart (with selection, ...)**

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/simpledesign_piechart1.png)
<br/><br/>

**ScatterChart** (with squares, triangles, circles, ... and more)

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/scatterchart.png)
<br/><br/>

**CandleStickChart** (for financial data)

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/candlestickchart.png)
<br/><br/>

**BubbleChart** (area covered by bubbles indicates the yValue)

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/bubblechart.png)
<br/><br/>

**RadarChart** (spider web chart)

![alt tag](https://raw.github.com/AppDevNext/AndroidChart/master/screenshotsReadme/radarchart.png)

<br/>
