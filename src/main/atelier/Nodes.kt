package atelier

sealed interface Expr{
    override fun toString(): String
    fun eval() : Expr?
}
class Group(expr: Expr) : Expr{
    override fun toString(): String {
        return super.toString()
    }
    override fun eval() : Expr? {
        TODO()
    }
}

