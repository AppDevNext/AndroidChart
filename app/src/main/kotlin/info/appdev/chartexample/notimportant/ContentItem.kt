package info.appdev.chartexample.notimportant

class ContentItem<T : DemoBase> {
    val name: String
    val desc: String
    var isSection = false
    var clazz: Class<T>? = null

    constructor(n: String) {
        name = n
        desc = ""
        isSection = true
    }

    constructor(name: String, description: String, clazzName: Class<T>) {
        this.name = name
        desc = description
        clazz = clazzName
    }
}
