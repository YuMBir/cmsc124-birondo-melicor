package atelier

import kotlin.system.exitProcess

class Program(private val statements: List<Expr>){
    override fun toString(): String{
        if (statements.isEmpty()){
            System.err.println("Error: nothing parsed")
            exitProcess(65)
        }
        return statements.joinToString("\n") { it.toString() }
    }
    fun getStatements(): List<Expr>{
        return statements
    }
}

enum class SigilType{
    ELEMENT,
    DIRECTION,
    TEXT,
    NUMBER
}

sealed interface Stmt {
    override fun toString(): String
}




class CircleFun(val name: String, var params: List<Expr>, var block: Block): Stmt{
    override fun toString(): String {
        return name
    }
}
class Block(val statements: List<Expr>): Stmt{
    override fun toString(): String {
        return statements.joinToString(separator = "\n", prefix = "{", postfix = "}")
    }
}
class Sigil(val name: String): Stmt{
    override fun toString(): String {
        return "(sigil $name)"
    }
}