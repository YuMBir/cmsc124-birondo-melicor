package atelier

class Parser(private var tokens: List<Token> = listOf()) {
    private fun reportError(lineText: String, message: String): Nothing {
        System.err.println("Parser: [line $lineText] $message")
        error("ParserError")
    }
    private var exitStatus = 0
    var trees = mutableListOf<Expr>()
    var current = 0
    fun getExitStatus(): Int {
        return exitStatus
    }
    fun setParser(newTokens: List<Token>){
        current = 0
        trees.clear()
        tokens = newTokens

    }

    fun parse(): Program{
        while (!isAtEnd()) {
            try{
                //skip blank lines and empty statements
                while (match(TokenType.NEWLINE, TokenType.SEMICOLON)) { }
                if (isAtEnd()) break
                trees.add(highest())
                if (isAtEnd()) break
                if(match(TokenType.SEMICOLON, TokenType.NEWLINE)){
                    //one trailing ';' is fine but consecutive ;; are not allowed
                    if (peek().getType() == TokenType.SEMICOLON) {
                        reportError(peek().getLine(),"Unexpected ';' after ';'. Expected expression after ';'")
                    }
                }else{
                    reportError(peek().getLine(), "Unexpected ${peek().getType()}. NEWLINE, SEMICOLON, or EOF Expected after Expression")
                }
            }catch(e:Exception){
                synchronize()
            }
        }
        return Program(trees)
    }
    fun synchronize(){
        exitStatus = 65
        while (peek().getType() !in listOf(TokenType.SEMICOLON, TokenType.NEWLINE, TokenType.EOF)) {
            discard()
        }
        if (match(TokenType.NEWLINE, TokenType.SEMICOLON)){
            parse()
        }
        parse()
    }

    /**
     * Returns current + increment (default is 0) token
     */
    fun peek(increment: Int = 0): Token{
        return tokens[current + increment]
    }

    /**
     * consume and return
     */
    fun advance(): Token{
        val token = tokens[current]
        if (!isAtEnd()) current++
        return token
    }

    /**
     * Checks token type and consumes if true, else returns false
     * Multiple token types can be used in the condition.
     */
    fun match(vararg type: TokenType): Boolean{
        if (peek().getType() in type){
            consume(*type)
            return true
        }
        else{
            return false
        }
    }

    /**
     * Advances to next token.
     * Returns error if token type is incorrect.
     */
    fun consume(vararg type: TokenType): Token{
        if (peek().getType() in type){
            return advance()
        }
        else{
            reportError(peek().getLine(),"Unexpected token: ${previous().getType()}. Expected token of type(s) ${type.contentToString()}")
        }
    }

    /**
     * Returns last consumed token.
     */
    fun previous(): Token{
        return tokens[current - 1]
    }
    /**
    * Advances without returning a token
    */
    fun discard(){
        current++
    }

    fun isAtEnd() = peek().getType() == TokenType.EOF

    fun highest(): Expr{
        return arguments()
    }

    fun arguments(): Expr{
        var args = mutableListOf<Expr>()
        args.add(highArg())
        while (match(TokenType.COMMA)){
            args.add(highArg())
        }
        return Arguments(args)
    }
    fun highArg(): Expr{
        return logicOr() //highest precedence for arguments here
    }
    fun logicOr(): Expr{
        var node = logicAnd()
        while (match(TokenType.OR)){
            node = Binary(node, previous(), logicAnd())
        }
        return node
    }
    fun logicAnd(): Expr{
        var node = equality()
        while (match(TokenType.AND)){
            node = Binary(node, previous(), equality())
        }
        return node
    }
    fun equality(): Expr{
        var node = relational()
        while (match (TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)){
            node = Binary(node, previous(), relational())
        }
        return node
    }
    fun relational(): Expr{
        var node = expression()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            node = Binary(node, previous(), expression())
        }
        return node
    }

    fun expression(): Expr{
        return addTerm()
    }
    fun addTerm(): Expr{
        var node = mulTerm()
        while (match(TokenType.PLUS, TokenType.MINUS)){
            node = Binary(node, previous(), mulTerm())
        }
        return node
    }
    fun mulTerm(): Expr{
        var node = unaryTerm()
        while (match(TokenType.STAR, TokenType.SLASH)){
            node = Binary(node, previous(), unaryTerm())
        }
        return node
    }
    fun unaryTerm(): Expr{
        if (match(TokenType.MINUS, TokenType.BANG)){
            var operator = previous()
            var operand = unaryTerm()
            return UnaryOp(operator, operand)
        }
        return primary()
    }
    fun primary(): Expr{
        //ADDED TokenType.STRING
        if (match(TokenType.NUMBER, TokenType.STRING, TokenType.TRUE, TokenType.FALSE, TokenType.NIL)){
            return Literal(previous())
        }
        if (match(TokenType.IDENTIFIER)){ //ADDED THIS 
            return Identifier(previous())
            }
        if (match(TokenType.LEFT_PAREN)){
            val node = logicOr() //was expression() then changed to logicOr() to consider <, ==, and, or.
            consume(TokenType.RIGHT_PAREN)
            return Group(node)
        }
        reportError(peek().getLine(),"unexpected token at primary(): ${peek().getType()}")
    }
}