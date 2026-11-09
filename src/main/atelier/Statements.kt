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
class Block(private val statements: List<Stmt>): Stmt{
    consume(TokenType.LEFT_BRACE)
    val statements = mutableListOf<Stmt>()
    while (true){
        while (match(TokenType.NEWLINE, TokenType.SEMICOLON)){ } //skip blank lines
        if (peek().getType() == TokenType.RIGHT_BRACE || isAtEnd()){
            break
        }
        statements.add(declaration())
        if(!match(TokenType.NEWLINE, TokenType.SEMICOLON) && peek().getType() != TokenType.RIGHT_BRACE){
            reportError(peek().getStartLine(), "Expected newline or ';' after statement.")
        }
    }
    if (isAtEnd()){
        reportError(peek().getLine(), "Expected '}' at end of block.")
    }
    consume(TokenType.RIGHT_BRACE)
    return Block(statements)
    //override fun toString(): String {
      //  return statements.joinToString(separator = "\n", prefix = "{", postfix = "}")
    //}
    //fun getStatements(): List<Stmt> = statements
}
// from name: String to token: Token. So errors have line numbers 
class Sigil(private val token: Token, private val expr: Expr? = null): Stmt{
    private val name = token.getLexeme()
    fun getToken(): Token = token
    override fun toString(): String {
        if (expr == null){
            return "(sigil $name)"     
        } else {
            return "(sigil $name $expr)"
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