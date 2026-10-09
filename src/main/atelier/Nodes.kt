package atelier

sealed interface Expr{
    override fun toString(): String
}
class Arguments(private val expressions: List<Expr>) : Expr{
    override fun toString(): String{
        return expressions.joinToString(separator = ", ", prefix = "(args ", postfix = ")")
    }
}
class Group(val node: Expr) : Expr{
    override fun toString(): String {
        return "(group $node)"
    }
}
class Literal(val token: Token) : Expr{
    override fun toString(): String {
        return token.getLiteralString()
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


