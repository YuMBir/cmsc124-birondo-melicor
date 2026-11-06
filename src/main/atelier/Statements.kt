package atelier

import kotlin.system.exitProcess



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
class Sigil(val name: String): Stmt{
    override fun toString(): String {
        return "(sigil $name)"
    }
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