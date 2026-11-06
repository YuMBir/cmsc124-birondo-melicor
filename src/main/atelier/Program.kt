package atelier

import kotlin.system.exitProcess

class Program(private val statements: List<Expr>){
    class Program(private val statements: List<Stmt>) {
        fun getStatements(): List<Stmt> = statements

        override fun toString(): String =
            statements.joinToString("\n") { it.toString() }
    }
    fun getStatements(): List<Expr>{
        return statements
    }
}