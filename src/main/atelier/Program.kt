package atelier

import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess


class Program(private val statements: List<Stmt>) {
    fun printAST(){
        System.out.write("AST:\n".toByteArray(StandardCharsets.UTF_8))
        for (statement in statements){
            System.out.write("$statement\n".toByteArray(StandardCharsets.UTF_8))
        }
    }
    fun getStatements(): List<Stmt> = statements

    override fun toString(): String =
        statements.joinToString("\n") { it.toString() }
}


