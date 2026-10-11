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
    atelierScanner.tokenize()
    val tokens = atelierScanner.getTokenList()
    when (mode) {
        "--print" -> atelierScanner.printCode() //default mode
        "--tokenize" -> atelierScanner.printTokens()
        "--parse" -> {
            val atelierParser = Parser(tokens)
            atelierParser.parse()
            atelierParser.printAST()
            exitProcess(atelierParser.exitStatus)
        }
        "--eval" -> {
            if (atelierScanner.hadError) exitProcess(65) // Static errors first, it will reject before running anything (exit 65).
            val atelierParser = Parser(tokens)
            atelierParser.parse()
            // parser sets exitStatus to 65 on a syntax error, so reject before evaluating
            if (atelierParser.exitStatus != 0) exitProcess(atelierParser.exitStatus)  // 65
            val evaluator = Evaluator()
            try {
                for (tree in atelierParser.trees) { // one tree per expression in the file, print each value on its own line (stdout)
                    val value = evaluator.evaluate(tree)
                    System.out.write((evaluator.stringify(value) + "\n").toByteArray(StandardCharsets.UTF_8))
                }
            } catch (e: RuntimeError) {
                    System.err.println("[line ${e.token?.getStartLine() ?: "?"}] Runtime error: ${e.message}") // the one place runtime errors are caught, message to stderr, exit 70 (not 65)
                    exitProcess(70)
                }
        }
        else -> fail("invalid flag")
    }
    if (atelierScanner.hadError) exitProcess(65) //placed this after when expression, to print the token statements instead of just returning error statements

}
fun scanCode(path: String): String{
    val source: String = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        fail("cannot read '$path': ${error.message}")
    }

    return source.replace(Regex("\r\n?"), "\n")
}

//REPL
fun repl(){
    val limit = 100
    var lineNo = 1
    val atelierScanner = Scanner()
    val atelierParser = Parser()
    do  {
        val line: String? = readlnOrNull()
        //exits repl if empty string
        if (!line.isNullOrEmpty()) {
            atelierScanner.resetTokenizer()
            atelierScanner.scanLine(line)
            atelierScanner.tokenize()
            val tokens = atelierScanner.getTokenList()
            atelierScanner.printTokens()

            System.out.write("AST: \n".toByteArray(StandardCharsets.UTF_8))
            atelierParser.setParser(tokens)
            atelierParser.parse()
            atelierParser.printAST()
        }
        else{
            System.out.write("Exited REPL".toByteArray(StandardCharsets.UTF_8))
            exitProcess(0)
        }
        lineNo++
    } while (lineNo <= limit)
    System.out.write("Line Limit Reached. Exiting REPL".toByteArray(StandardCharsets.UTF_8))
    exitProcess(0)
}

