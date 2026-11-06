package atelier


enum class SigilType{
    ELEMENT,
    DIRECTION,
    TEXT,
    NUMBER
}

sealed interface Stmt {
    override fun toString(): String
}


class ExprStmt(var expr: Expr): Stmt {
    override fun toString(): String{
        return expr.toString()
    }
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
class Sigil(private val name: String, private val expr: Expr? = null): Stmt{
    override fun toString(): String {
        return "(sigil $name)"
    }
    fun getName(): String = name
    fun getExpr(): Expr? = expr
}

class Etch(private val args: Expr): Stmt{
    override fun toString(): String {
        return "etch ($args)"
    }
    fun getArgs(): Expr {
        return args
    }
    fun getString(): String {
        return args.toString()
    }
}