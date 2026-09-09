package atelier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

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

    // token output
    for (token in tokens){
        System.out.write(token.toString().toByteArray(StandardCharsets.UTF_8))
    }
}

data class Token(val type: String, val lexeme: String, val line: Int){
    override fun toString(): String {
        //custom tokenize output format
        return "(type=$type, lexeme=$lexeme, line=$line)\n"
    }
}

