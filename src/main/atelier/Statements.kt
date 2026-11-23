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
//fixed the parameter lists to match the changes made in lab4, with the Tokens
class CircleFun(val name: Token, var params: List<Token>, var body: Block): Stmt{
    // Prints the whole declaration so --parse output shows the params and the body, not just the name
    override fun toString(): String {
        return "(circle ${name.getLexeme()} (${params.joinToString(" ") { it.getLexeme() }}) $body)"
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
// manifest [value]  (the return statement)
// value is null for a circle without manifest
// keyword is the `manifest` token, kept so a top-level manifest can report a line number.
class Return(val keyword: Token, val value: Expr?) : Stmt {
    override fun toString(): String {
        return if (value == null) "(manifest)" else "(manifest $value)"
    }
}