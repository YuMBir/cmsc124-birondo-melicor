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
            val ast = atelierParser.parse()
            System.out.write(ast.toString().toByteArray(StandardCharsets.UTF_8))
            exitProcess(atelierParser.getExitStatus())
        }
        "--eval" -> {
            if (atelierScanner.hadError) exitProcess(65) // Static errors first, it will reject before running anything (exit 65).
            val atelierParser = Parser(tokens)
            val ast = atelierParser.parse()
            // parser sets exitStatus to 65 on a syntax error, so reject before evaluating
            if (atelierParser.getExitStatus() != 0) exitProcess(atelierParser.getExitStatus())  // 65
            val atelierEval = Evaluator()
            val values = atelierEval.evaluateProgram(ast)
            for (value in values) {
                System.out.write("$value\n".toByteArray(StandardCharsets.UTF_8))
            }
            if (atelierEval.getExitStatus() != 0) exitProcess(atelierEval.getExitStatus())

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
    val evaluator = Evaluator()

    do  {
        val line: String? = readlnOrNull()
        //exits repl if empty string
        if (line.isNullOrEmpty()){
            System.out.write("Exited REPL".toByteArray(StandardCharsets.UTF_8))
            exitProcess(0)
        }
        atelierScanner.resetTokenizer()
        atelierScanner.scanLine(line)
        atelierScanner.tokenize()
        val tokens = atelierScanner.getTokenList()

        //static error, just report then keep evaluating
        if (atelierScanner.hadError){
            atelierScanner.hadError = false
            lineNo++
            continue
        }

        atelierParser.setParser(tokens)
        val ast = atelierParser.parse()

        if(atelierParser.getExitStatus() != 0){
            atelierParser.resetExitStatus() 
            lineNo++
            continue
        }
        //evaluation of each statement
        for (statement in ast.getStatements()){
            try{
                val value = evaluator.evaluate(statement)
                System.out.write("$value\n".toByteArray(StandardCharsets.UTF_8))
            }catch(e: RuntimeError) {
                System.err.println("[line ${e.token?.getStartLine() ?: "?"}] Runtime error: ${e.message}")
            }
        }
        lineNo++
    }while (lineNo <= limit)
    System.out.write("Line Limit Reached. Exiting REPL".toByteArray(StandardCharsets.UTF_8))
    exitProcess(0)
}

