package atelier
//token template
data class Token(val type: String, private val lexeme: String, private val literal: Any? = null, val startLine: Int, val endLine: Int = startLine) {
    override fun toString(): String {
        //custom tokenize output format
        //added literal
        return "Token(type=$type, lexeme=${getLexeme()}, literal=${getLiteralString()}, line=$startLine:$endLine)\n"
    }
    fun getLexeme(): String {
         when (type){
             "STRING" -> return lexeme.replace("\n", "\\n").replace("\r\n", "\\n").replace("\r","\\r") //added escapes for \r
             "NEWLINE" -> return "\\n"
         }
        return lexeme

    }
    fun getLiteralString(): String {
        if (type == "STRING"){
            return literal.toString().replace("\n", "\\n").replace("\r\n", "\\n").replace("\r","\\r") //added escapes for \r
        }
        return literal.toString()
    }
} 