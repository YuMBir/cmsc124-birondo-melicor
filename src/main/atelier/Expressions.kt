package atelier

import kotlin.system.exitProcess

sealed interface Expr{
    override fun toString(): String
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

//ADDED THE NEW CODE HERE
class Identifier(val token: Token) : Expr {
    override fun toString(): String {
        return token.getLexeme()
    }
    fun getName(): String {
        return token.getLexeme()
    }
}



