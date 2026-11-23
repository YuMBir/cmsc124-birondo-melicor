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

class CircleFun(val name: Token, var params: List<Token>, var body: Block): Stmt{
    override fun toString(): String {
        return name.getLexeme()
    }
}
class Block(private val statements: List<Stmt>): Stmt{
    fun getStatements(): List<Stmt> = statements
    override fun toString(): String {
      return statements.joinToString(separator = "\n", prefix = "{", postfix = "}")
    }
    
}
// from name: String to token: Token. So errors have line numbers 
class Sigil(private val token: Token, private val expr: Expr? = null): Stmt{
    private val name = token.getLexeme()
    fun getToken(): Token = token
    override fun toString(): String {
        return if (expr == null){
            "(sigil $name)" 
        } else {
            "(sigil $name $expr)"
        }
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
// from name: String to token: Token. So errors have line numbers 
class Assignment(private val token: Token, private val expr: Expr): Stmt{
    private val name = token.getLexeme()
    fun getToken(): Token = token
    fun getName(): String = name
    fun getExpr(): Expr = expr
    override fun toString(): String {
        return "$name = $expr"
    }
}
class Scry(private val condition: Expr, private val block: Block, private val elseBranch: Block? = null): Stmt{
    override fun toString(): String {
        return "scry ($condition){\n$block\n}"
    }
    fun getCondition(): Expr = condition
    fun getBody(): Block = block
    fun getElseBranch(): Block? = elseBranch
}

class Whilst(private val condition: Expr, private val block: Block): Stmt{
    override fun toString(): String {
        return "whilst ($condition){\n$block\n}"
    }
    fun getCondition(): Expr = condition
    fun getBody(): Block = block
}
