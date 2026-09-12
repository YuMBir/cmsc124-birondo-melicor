package atelier
//token template
data class Token(val type: String, val lexeme: String, val line: Int){
    override fun toString(): String {
        //custom tokenize output format
        return "Token(type=$type, lexeme=$lexeme, line=$line)\n"
    }
}