package atelier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess


private fun fail(message: String): Nothing {
    System.err.println("Main: $message")
    exitProcess(65)
}

fun main(args: Array<String>) {
    val path = args.firstOrNull() ?: fail("expected one source-file path")

    val source = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        when (path){
            "--tokenize" -> tokenize(scan(args[1]))
            else -> fail("$path is an invalid flag")
        }
    }

}

