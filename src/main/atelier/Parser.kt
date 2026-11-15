package atelier

class Parser(private var tokens: List<Token> = listOf()) {
    private fun reportError(lineText: String, message: String): Nothing {
        System.err.println("Parser: [line $lineText] $message")
        error("ParserError")
    }
    private var exitStatus = 0
    var trees = mutableListOf<Stmt>()
    var current = 0
    fun getExitStatus(): Int {
        return exitStatus
    }
    fun resetExitStatus(){
        exitStatus = 0
    }
    fun setParser(newTokens: List<Token>){
        current = 0
        trees.clear()
        tokens = newTokens
        exitStatus = 0

    }

    fun parse(): Program{
        if(isAtEnd()){
            exitStatus = 65 //for empty programs
        }
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
                    if (peek().getType() == TokenType.EQUAL){
                        reportError(peek().getLine(), "Invalid assignment target.")
                    }
                    reportError(peek().getLine(), "Unexpected ${peek().getType()}. NEWLINE, SEMICOLON, or EOF Expected after Expression")
                }
            }catch(e:Exception){
                synchronize()
            }
        }
        return Program(trees)
    }
    fun synchronize(){
        exitStatus = 65 // any syntax or error means the program is rejected before running
        //skips to the end of the broken statement and parse()'s loop skips the terminator
        // and resumes, so no recursion is needed. And the loop always makes progress. 
        while (!isAtEnd() && peek().getType() !in listOf(TokenType.SEMICOLON, TokenType.NEWLINE)) { //remove the EOF 
            advance() //discard tokens until we find a semicolon or newline
        }
        
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
            advance()
            return true
        }
        else{
            return false
        }
    }

    /**
     * Advances to next token and returns.
     * Returns error if token type is incorrect.
     */
    fun consume(type: TokenType, message: String): Token{
        if (peek().getType() == type){
            return advance()
        }
        reportError(peek().getLine(), message)
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

    fun highest(): Stmt{
        return declaration()
    }

    fun block(): Stmt{
        consume(TokenType.LEFT_BRACE, "Expected '{' at start of block.")
        val statements = mutableListOf<Stmt>()
        while (true){
            while (match(TokenType.NEWLINE, TokenType.SEMICOLON)){ } //skip blank lines
            if (peek().getType() == TokenType.RIGHT_BRACE || isAtEnd()){
                break
            }
            statements.add(declaration())
            if(!match(TokenType.NEWLINE, TokenType.SEMICOLON) && peek().getType() != TokenType.RIGHT_BRACE){
                reportError(peek().getLine(), "Expected newline or ';' after statement.")
            }
        }
        if (isAtEnd()){
            reportError(peek().getLine(), "Expected '}' at end of block.")
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' at end of block.")
        return Block(statements)
    }

    fun declaration(): Stmt {
        if (peek().getType() == TokenType.LEFT_BRACE) {
            return block()
        }
        if (match(TokenType.ETCH)) {
            return prntStmt()
        }
        if (match(TokenType.SIGIL)){
            return varDecl()
        }
        if (peek().getType() == TokenType.IDENTIFIER && peek(1).getType() == TokenType.EQUAL){
            return assignment()
        }
        // default
        return ExprStmt(topExpr())
    }
    fun varDecl(): Stmt {
        val nameToken = consume(TokenType.IDENTIFIER, "Expected variable name after sigil.") //keep the Token
        val enders = listOf(TokenType.SEMICOLON, TokenType.NEWLINE, TokenType.RIGHT_BRACE, TokenType.EOF)
        if(peek().getType() in enders){
            //declaration without assignment
            return Sigil(nameToken)
        }
        consume(TokenType.EQUAL, "Expected '=' or end of statement after variable name in declaration.")
        if (peek().getType() in enders){
            reportError(peek().getLine(), "Expected value after '='.")
            //declaration without assignment
            
        }
        
        return Sigil(nameToken, topExpr())

    }
    fun assignment(): Stmt {
        val nameToken = consume(TokenType.IDENTIFIER, "Expected variable name in assignment.") //consume the identifier and keep the Token for error reporting
        consume(TokenType.EQUAL, "Expected '=' in assignment.")
        if (peek().getType() in listOf(TokenType.SEMICOLON, TokenType.NEWLINE, TokenType.RIGHT_BRACE, TokenType.EOF)){
            reportError(peek().getLine(), "Expected value after '=' in assignment.")
        }
        val value = topExpr()
        return Assignment(nameToken, value)
    }

    fun prntStmt(): Stmt{
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'etch'.")
        val value = topExpr()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
        return Etch(value)
    }
    fun arguments(): Expr{
        var args = mutableListOf<Expr>()
        args.add(topExpr())
        while (match(TokenType.COMMA)){
            args.add(topExpr())
        }
        return Arguments(args)
    }
    fun topExpr(): Expr{
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
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
            return Group(node)
        }
        reportError(peek().getLine(),"Expected expression.")
    }
}