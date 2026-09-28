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
             "STRING" -> return lexeme.replace(Regex("[\\n\\r\\t]")){
                 when (it.value){
                     "\n" -> "\\n"
                     "\r" -> ""
                     "\t" -> "\\t"
                     else -> it.value
                 }
             }
             "NEWLINE" -> return "\\n"
         }
        return lexeme

    }
    fun getLiteralString(): String {
        if (type == "STRING"){
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
} 