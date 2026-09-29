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
        for (expression in expressions){
            System.out.write(expression.toString().toByteArray(StandardCharsets.UTF_8))
            System.out.write("\n".toByteArray(StandardCharsets.UTF_8))
        }
        if (expressions.isEmpty()){
            System.out.write("Nothing parsed\n".toByteArray(StandardCharsets.UTF_8))
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
            expressions.add(highest())
            match("NEWLINE") // consume newline
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
    fun match(vararg type: String): Boolean{
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
    fun consume(vararg type: String): Token{
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

    fun isAtEnd() = peek().getType() == "EOF"

    fun highest(): Expr{
        return arguments()
    }

    fun arguments(): Expr{
        var args = mutableListOf<Expr>()
        args.add(firstArg())
        while (match("COMMA")){
            args.add(firstArg())
        }
        return Arguments(args)
    }
    fun firstArg(): Expr{
        return logicAnd() //highest precedence for arguments here
    }
    fun logicAnd(): Expr{
        var node = logicOr()
        while (match("AND")){
            node = Binary(node, previous(), logicOr())
        }
        return node
    }
    fun logicOr(): Expr{
        var node = equality()
        while (match("OR")){
            node = Binary(node, previous(), equality())
        }
        return node
    }
    fun equality(): Expr{
        var node = relational()
        while (match ("EQUAL_EQUAL", "NOT_EQUAL")){
            node = Binary(node, previous(), relational())
        }
        return node
    }
    fun relational(): Expr{
        var node = expression()
        while (match("GREATER", "GREATER_EQUAL", "LESS", "LESS_EQUAL")){
            node = Binary(node, previous(), expression())
        }
        return node
    }

    fun expression(): Expr{
        return addTerm()
    }
    fun addTerm(): Expr{
        var node = mulTerm()
        while (match("PLUS", "MINUS")){
            node = Binary(node, previous(), mulTerm())
        }
        return node
    }
    fun mulTerm(): Expr{
        var node = unaryTerm()
        while (match("STAR", "SLASH")){
            node = Binary(node, previous(), unaryTerm())
        }
        return node
    }
    fun unaryTerm(): Expr{
        if (match("MINUS", "BANG")){
            var operator = previous()
            var operand = unaryTerm()
            return UnaryOp(operator, operand)
        }
        return primary()
    }
    fun primary(): Expr{
        if (match("NUMBER", "TRUE", "FALSE", "NIL")){
            return Literal(previous())
        }
        if (match("LEFT_PAREN")){
            val node = expression()
            consume("RIGHT_PAREN")
            return Group(node)
        }
        reportError("unexpected token at primary(): ${peek().getType()}")
    }
}

