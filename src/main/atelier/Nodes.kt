package atelier

import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

sealed interface Expr{
    override fun toString(): String
}
class Program(private val statements: List<Expr>): Expr{
    override fun toString(): String{
        if (statements.isEmpty()){
            System.err.println("Error: nothing parsed")
            exitProcess(65)
        }
        return statements.joinToString("\n") { it.toString() }
    }
}

class Arguments(private val expressions: List<Expr>) : Expr{
    override fun toString(): String{
        return expressions.joinToString(separator = ", ", prefix = "(args ", postfix = ")")
    }
    fun getArgs(): List<Expr>{
        return expressions
    }
}
class Group(val node: Expr) : Expr{
    override fun toString(): String {
        return "(group $node)"
    }
}
class Literal(val token: Token) : Expr{
    override fun toString(): String {
        if (token.getType() == TokenType.STRING){
            return "\"${token.getLiteralString()}\""
        }
        else{
            return token.getLiteralString()
        }
    }
}
class Binary(val left: Expr, val operator:Token, val right: Expr) : Expr{
    override fun toString(): String {
        return "(${operator.getLexeme()} $left $right)"
    }
}
class UnaryOp(val operator: Token, val node: Expr) : Expr{
    override fun toString(): String {
        return "(${operator.getLexeme()} $node)"
    }
}
class CircleNode(val name: String, var params: List<Expr>, var block: Block): Expr{
    override fun toString(): String {
        return name
    }
}
class Block(val statements: List<Expr>): Expr{
    override fun toString(): String {
        return statements.joinToString(separator = "\n", prefix = "{", postfix = "}")
    }
}
class Sigil(val name: String, val type: SigilType): Expr{
    override fun toString(): String {
        return "(sigil: $type $name)"
    }
}
//ADDED THE NEW CODE HERE
class Identifier(val token: Token) : Expr {
    override fun toString(): String {
        return token.getLexeme()
    }
}
enum class SigilType{
    ELEMENT,
    DIRECTION
}


