package atelier

import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

private fun fail(message: String): Nothing {
    System.err.println("Parser: $message")
    exitProcess(65)
}

class Parser(private var tokens: List<Token> = listOf()) {
    var expressions = mutableListOf<Expr>()
    var current = 0
    fun printAST(){
        System.out.write("AST: \n".toByteArray(StandardCharsets.UTF_8))
        for (expression in expressions){
            System.out.write(expression.toString().toByteArray(StandardCharsets.UTF_8))
        }
        if (expressions.isEmpty()){
            System.out.write("Nothing parsed\n".toByteArray(StandardCharsets.UTF_8))
        }
        System.out.write("\n".toByteArray(StandardCharsets.UTF_8))
    }

    fun setParser(newTokens: List<Token>){
        current = 0
        tokens = newTokens

    }

    fun parse(){
        while (!isAtEnd()) {
            expressions.add(expression())
        }
    }

    fun peek(): Token{
        return tokens[current]
    }

    fun match(vararg type: String): Boolean{
        if (peek().getType() in type){
            consume(*type)
            return true
        }
        else{
            return false
        }
    }

    fun consume(vararg type: String): Token{
        if (peek().getType() in type){
            return tokens[current++]
        }
        else{
            fail("unexpected token: ${previous()} expected token of type(s) ${type.contentToString()}")
        }
    }

    fun previous(): Token{
        return tokens[current - 1]
    }

    fun isAtEnd() = peek().getType() == "EOF"

    fun expression(): Expr{
        return term()
    }
    fun term(): Expr{
        var node = factor()
        while (match("PLUS", "MINUS")){
            node = Binary(node, previous(), factor())
        }
        return node
    }
    fun factor(): Expr{
        var node = primary()
        while (match("STAR", "SLASH")){
            node = Binary(node, previous(), primary())
        }
        return node
    }
    fun primary(): Expr{
        if (match("NUMBER")){
            return Literal(previous().getLiteralValue()!!)
        }
        if (match("LEFT_PAREN")){
            val node = expression()
            consume("RIGHT_PAREN")
            return Group(node)
        }
        fail("unexpected token: unterminated parenthesis")
    }
}

