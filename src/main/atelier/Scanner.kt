package atelier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

//use this for scanner errors, it's good practice to have separate fail functions so we know where the error comes from
private fun fail(message: String): Nothing {
    System.err.println("Scanner: $message")
    exitProcess(65)
}

//scanner class so that we don't have to keep passing values
class Scanner(val source: String) {
    var tokens = mutableListOf<Token>()

    fun printCode(){ //just prints the code in the file line by line
        System.out.write(source.toByteArray(StandardCharsets.UTF_8))
    }
    //write token scanner here
    init {
        tokenize()
    }
    fun tokenize(){
        val chars = source.toCharArray()
        var line = 1

        for (char in chars){ //CHANGE THIS FOR prog check 2
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
            if (type != "NULL"){ //redudant daw sabi ni sir
                val newToken = Token(type, char.toString(), line)
                tokens.add(newToken)
            }
        }
    }

    fun printTokens(){
        // token output
        for (token in tokens){
            System.out.write(token.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }
}





