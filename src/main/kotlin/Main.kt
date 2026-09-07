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
        when (path){
            "--tokenizer" -> tokenize(scan(args[1]))
            else -> fail("$path is an invalid flag")
        }
    }

}
fun scan(path: String): String{
    val source = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        fail("cannot read '$path': ${error.message}")
    }
    return source
}

fun tokenize(code: String){
    val chars = code.toCharArray()
    var line = 1
    var tokens = mutableListOf<Token>()
    for (char in chars){
        var type = "NULL"
        when (char){
            '=' -> type = "EQUALS"
            '(' -> type = "LEFT_PAREN"
            ')' -> type = "RIGHT_PAREN"
            '{' -> type = "LEFT_BRACE"
            '}' -> type = "RIGHT_BRACE"
            ':' -> type = "COLON"
            '\n' -> line++
            else -> type = "NULL"
        }
        if (type != "NULL"){
            val newToken = Token(type, char.toString(), line)
            tokens.add(newToken)
        }
    }
    for (token in tokens){
        println(token)
    }
}

data class Token(val type: String, val lexeme: String, val line: Int)


