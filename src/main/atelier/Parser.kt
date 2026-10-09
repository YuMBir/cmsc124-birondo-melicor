package atelier

import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

private fun reportError(message: String): Nothing {
    System.err.println("Parser: $message")
    exitProcess(65)
}

class Parser(private var tokens: List<Token> = listOf()) {
    var expressions = mutableListOf<Expr>()
    var current = 0
    fun printAST(){
        if (expressions.isEmpty()){
            System.err.println("Error: nothing parsed")
            exitProcess(65)
        }
        for (expression in expressions){
            System.out.write(expression.toString().toByteArray(StandardCharsets.UTF_8))
            System.out.write("\n".toByteArray(StandardCharsets.UTF_8))
        }
        System.out.write("\n".toByteArray(StandardCharsets.UTF_8))
    }

    fun setParser(newTokens: List<Token>){
        current = 0
        expressions.clear()
        tokens = newTokens

    }

    fun parse(){
        while (!isAtEnd()) {
            //skip blank lines and empty statements
            while (match(TokenType.NEWLINE, TokenType.SEMICOLON)) { }
            if (isAtEnd()) break
            expressions.add(highest())
            if (isAtEnd()) break
            if(match(TokenType.SEMICOLON)){
                //one trailing ';' is fine but consecutive ;; are not allowed
                if (peek().getType() == TokenType.SEMICOLON) {
                    reportError("Unexpected ';' after ';'")
                }
            }else if (!match(TokenType.NEWLINE)){   //require a terminator after each expression
                reportError("Expected newline or ';' after expression")
            }
        }
    }

    /**
     * Returns current + increment (default is 0) token
     */
    fun peek(increment: Int = 0): Token{
        return tokens[current + increment]
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
            return tokens[current++]
        }
        else{
          reportError("unexpected token: ${previous()} expected token of type(s) ${type.contentToString()}")
        }
    }

    /**
     * Returns last consumed token.
     */
    fun previous(): Token{
        return tokens[current - 1]
    }

    fun isAtEnd() = peek().getType() == TokenType.EOF

    fun highest(): Expr{
        return arguments()
    }

    fun arguments(): Expr{
        var args = mutableListOf<Expr>()
        args.add(firstArg())
        while (match(TokenType.COMMA)){
            args.add(firstArg())
        }
        return Arguments(args)
    }
    fun firstArg(): Expr{
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
            val node = logicOr() //was expression() then changed to logicOr() to consider <, ==, and, or or.
            consume(TokenType.RIGHT_PAREN)
            return Group(node)
        }
        reportError("unexpected token at primary(): ${peek().getType()}")
    }
}