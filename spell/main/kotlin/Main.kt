import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess


private fun fail(message: String): Nothing {
    System.err.println("lab0: $message")
    exitProcess(65)
}

fun main(args: Array<String>) {
    val path = args.firstOrNull() ?: fail("expected one source-file path")

    val source = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        fail("cannot read '$path': ${error.message}")
    }

    System.out.write(source.toByteArray(StandardCharsets.UTF_8))//prints raw data in console

}
fun tokenScan(source: String){
    TODO()
}

data class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int){
    TODO()
    //prints the output
}


