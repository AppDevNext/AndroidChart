package info.git.versionHelper

import java.io.File
import java.util.concurrent.TimeUnit

@JvmOverloads
fun String.runCommand(workingDir: File = File("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

fun getGitCommitCount(): Int {
    val process = "git rev-list HEAD --count".runCommand()
    return process.toInt() + 580
}

fun getVersionText(): String {
    val processChanges = "git diff-index --name-only HEAD --".runCommand()
    var dirty = ""
    if (processChanges.trim().isNotEmpty())
        dirty = "-DIRTY"

    val processDescribe = "git describe".runCommand()
    return processDescribe.trim() + dirty
}
