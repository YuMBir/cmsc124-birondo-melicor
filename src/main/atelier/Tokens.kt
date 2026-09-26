package atelier
//token template
data class Token(val type: String, private val lexeme: String, private val literal: Any? = null, val startLine: Int, val endLine: Int = startLine) {
    override fun toString(): String {
        //custom tokenize output format
        //added literal
        return "Token(type=$type, lexeme=${getLexeme()}, literal=${getLiteralString()}, line=$startLine:$endLine)\n"
    }
    fun getLexeme(): String {
        if (type == "STRING"){
            return literal.toString().replace("\n", "\\n")
        }
        return literal.toString()

    }
    fun getLiteralString(): String {
        if (type == "STRING"){
            return literal.toString().replace("\n", "\\n")
        }
        return literal.toString()
    }
} 