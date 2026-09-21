package atelier

sealed interface Expr{
    override fun toString(): String
}
class Group(expr: Expr) : Expr{
    override fun toString(): String {
        return super.toString()
    }
}
class Literal(val literal: Any) : Expr{
    override fun toString(): String {
        return literal.toString()
    }
}
class Binary(val left: Expr, val operator:Token, val right: Expr) : Expr{
    override fun toString(): String {
        return "$left ${operator.getLiteral()} $right"
    }
}

