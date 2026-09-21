package atelier

import kotlin.system.exitProcess

private fun fail(message: String): Nothing {
    System.err.println("Parser: $message")
    exitProcess(65)
}

class Parser(private var tokens: List<Token>) {
    var expressions = mutableListOf<Expr>()
    var current = 0
    fun printAST(){
        for (expression in expressions){
            println(expression)
        }
    }
    fun parse(){
        while (!isAtEnd()) {
            expression()
        }
    }

    fun peek(): Token{
        return tokens[current]
    }

    fun match(vararg type: String): Boolean{
        if (peek().type in type){
            consume(*type)
            return true
        }
        else{
            consume(*type)
            return false
        }
    }

    fun consume(vararg type: String): Token{
        if (peek().type in type){
            return tokens[current++]
        }
        else{
            fail("unexpected token")
        }
    }

    fun previous(): Token{
        return tokens[current - 1]
    }

    fun isAtEnd() = current >= tokens.size

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

