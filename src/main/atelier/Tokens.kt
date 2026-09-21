package atelier
//token template
data class Token(private val type: String, private val lexeme: String, private val literal: Any? = null, val line: Int){
    override fun toString(): String {
        //custom tokenize output format
        //added literal
        return "Token(type=$type, lexeme=$lexeme, literal=$literal, line=$line)\n"
    }
    fun getLiteralValue(): Any?{
        return literal
    }
    fun getLexeme(): String{
        return lexeme
    }
    fun getType(): String{
        return type
    }
} 