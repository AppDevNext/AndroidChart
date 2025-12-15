package info.appdev.chartexample.notimportant

fun <T : DemoBaseCompose> ComposeItem<T>.toDemoBase(): ContentItem<DemoBase> {
    return if (isSection) {
        ContentItem(this.name)
    } else {
        @Suppress("UNCHECKED_CAST")
        ContentItem(this.name, this.desc, this.clazz as Class<DemoBase>)
    }
}
