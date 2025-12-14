package info.appdev.chartexample.notimportant

class ComposeItem<T : DemoBaseCompose> {
    val name: String
    val desc: String
    var isSection = false
    var clazz: Class<T>? = null

    constructor(n: String, d: String, clazzName: Class<T>) {
        name = n
        desc = d
        clazz = clazzName
    }
}
