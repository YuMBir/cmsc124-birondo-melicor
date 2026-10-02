package atelier
//token template
data class Token(private val type: TokenType, private val lexeme: String, private val literal: Any? = null, val startLine: Int, val endLine: Int = startLine) {
    override fun toString(): String {
        //custom tokenize output format
        //added literal
        return "Token(type=$type, lexeme=${getLexeme()}, literal=${getLiteralString()}, line=$startLine:$endLine)\n"
    }
    fun getLexeme(): String {
        return when (type){
            TokenType.STRING -> lexeme.replace(Regex("[\\n\\r\\t]")){
                when (it.value){
                    "\n" -> "\\n"
                    "\r" -> ""
                    "\t" -> "\\t"
                    else -> it.value
                }
            }

            TokenType.NEWLINE -> "\\n"
            else -> lexeme
        }
    }
    fun getLiteralString(): String {
        if (type == TokenType.STRING){
            return literal.toString().replace(Regex("[\\n\\r\\t]")){
                when (it.value){
                    "\n" -> "\\n"
                    "\r" -> ""
                    "\t" -> "\\t"
                    else -> it.value
                }
            }
        }
        return literal.toString()
    }
    fun getLiteralValue(): Any?{
        return literal
    }
    fun getType(): TokenType{
        return type
    }
}

enum class TokenType {
    NUMBER, IDENTIFIER, STRING, FALSE, TRUE,
    NEWLINE, NIL, EOF,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COLON, SEMICOLON, COMMA, DOT,
    STAR, PLUS, MINUS, SLASH,
    EQUAL_EQUAL, EQUAL, NOT_EQUAL, LESS_EQUAL, GREATER_EQUAL, LESS, GREATER, BANG,
    AND, OR,
    MANIFEST, WHILST, ETCH, SCRY, CIRCLE, SIGIL, IMBUE, ELSE


}