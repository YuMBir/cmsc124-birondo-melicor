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
    val argsSize = args.size
    val path: String
    //arg count checker
    if (argsSize == 2){
        //run with mode
        val mode = args[0]
        path = args[1]
        when (mode){
            "--tokenize" -> tokenize(scan(path))
            else -> fail("invalid flag")
        }
    } else if(argsSize == 1){
        path = args[0]
        //placeholder for the future
        val code = scan(path)
        System.out.write(code.toByteArray(StandardCharsets.UTF_8))
    } else{
        fail("invalid no. of arguments")
    }
}

