package atelier
//token template
data class Token(val type: String, val lexeme: String, val literal: Any? = null, val line: Int){
    override fun toString(): String {
        //custom tokenize output format
        //added literal
        return "Token(type=$type, lexeme=$lexeme, literal=$literal, line=$line)\n"
    }
    fun getLiteral(): String{
        return literal.toString()
    }
} 