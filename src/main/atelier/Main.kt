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
    //arg count checker
    when (argsSize) {
        2 -> {
            //run with mode
            run(scanCode(args[1]), args[0])
        }
        1 -> {
            //print
            run(scanCode(args[0]))
        }
        0 -> {
            repl()
        }
        else -> {
            fail("invalid no. of arguments")
        }
    }
}

//main controller
fun run(source: String, mode: String = "--print") {
    val atelierScanner = Scanner(source)
    when (mode) {
        "--print" -> atelierScanner.printCode() //default mode
        "--tokenize" -> atelierScanner.printTokens()
        else -> fail("invalid flag")
    }

}
fun scanCode(path: String): String{
    val source: String = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        fail("cannot read '$path': ${error.message}")
    }
    return source
}

//REPL
fun repl(){
    val limit = 200
    var lineNo = 1
    do  {
        val line: String? = readLine()
        //exits repl if empty string
        if (!line.isNullOrEmpty()) {
            val atelierScanner = Scanner(line)
            //need to update token function so it knows what line it's in
            atelierScanner.printTokens()
        }
        else{
            System.out.write("Exited REPL".toByteArray(StandardCharsets.UTF_8))
            exitProcess(0)
        }
        lineNo++
    } while (lineNo < limit)

}

