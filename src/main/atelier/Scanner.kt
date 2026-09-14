package atelier
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

//use this for scanner errors, it's good practice to have separate fail functions so we know where the error comes from
private fun fail(message: String): Nothing {
    System.err.println("Scanner: $message")
    exitProcess(65)
}

//scanner class so that we don't have to keep passing values
class Scanner(private var source: String = "") {
    var tokens = mutableListOf<Token>()
    private var start = 0 //index indicates present lexeme
    private var current = 0 //index indicating how far the file is read
    private var line = 1

    init {
        //write init code here
    }

    fun scanLine(sourceLine: String){
        this.source = sourceLine
    }

    var hadError = false
    private val keywords = mapOf(
        "var" to "VAR",
        "print" to "PRINT"
    )


    private fun isAtEnd() = current >= source.length //checks for file ending
    private fun advance(): Char { //consumes the character
        val c = source[current++]
        if (c == '\n') {
            line++
        }
        return c
    }
    //reads a character that isn't consumed yet, in other words, it implements lookahead
    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]
    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected ) return false  //no character left or next character is not the expected character
        current++
        return true //consumes a match
    }
    private fun addToken(type: String, literal: Any? = null) {
        val text = source.substring(start,current)
        tokens.add(Token(type,text,literal,line))
    }
    private fun identifier() {
        while (peek().isLetterOrDigit()) advance() //looks through whole identifier
        val text = source.substring(start, current) //identifier text
        addToken(keywords[text] ?: "IDENTIFIER") //keyword or identifier
    }
    private fun number() { //this is for dealing with numbers
        while (peek().isDigit()) advance()// handles decimal point
        if (peek() == '.' && peekNext().isDigit()) { //if decimal poimt
            advance() // consume the '.'
            while (peek().isDigit()) advance()
        }
        val value = source.substring(start, current)
        addToken("NUMBER", value.toDouble())
    }
    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    private fun string(){
        while (peek()!= '"' && !isAtEnd()){
            advance()
        }
        if (isAtEnd()) {
            reportError(line, "Unterminated string.")
            return
        }
        advance()   // consume the closing "

        val value = source.substring(start + 1, current - 1)   // strip surrounding quotes
        addToken("STRING", value)
        }
    private fun reportError(line: Int, message: String) {
        hadError = true
        fail("[line $line] Error: $message")
    }



    fun printCode(){ //just prints the code in the file line by line
        System.out.write(source.toByteArray(StandardCharsets.UTF_8))
    }

    //write token scanner here
    fun tokenize(){
        while (!isAtEnd()){
            start = current
            scanToken()
        }
        tokens.add(Token("EOF", "", line = line))
    }
    private fun scanToken(){ //hindi ko gin enum class
        when (val c = advance()){
           '(' -> addToken("LEFT_PAREN")
            ')' -> addToken("RIGHT_PAREN")
            '{' -> addToken("LEFT_BRACE")
            '}' -> addToken("RIGHT_BRACE")
            ':' -> addToken("COLON")
            '.' -> addToken("DOT")
            '=' -> addToken(if (match('=')) "EQUAL_EQUAL" else "EQUAL")
            '<' -> addToken(if (match('=')) "LESS_EQUAL" else "LESS")
            '>' -> addToken(if(match('=')) "GREATER_EQUAL" else "GREATER")
            '!' -> addToken(if(match('=')) "NOT_EQUAL" else "NOT")
            '/' -> {
                if (match('/')){
                    while (peek() != '\n' && !isAtEnd())
                    advance()
                } else if (match('*')){ //for the multiple line comment
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()){
                        advance() //consumes /
                    }
                    if (isAtEnd()){ //not finished /* */
                        reportError(line, "Unterminated block comment.")
                    } else {
                        advance() //consumes *
                        advance() //consumes /
                    }
                }else {
                    addToken("SLASH") // for non comments
                }
            }
            ' ', '\r', '\t' -> {}         //ignore whitespace
            '\n' -> {}
            '"' -> string()
            else -> {
                if (c.isLetter()) identifier()
                else if (c.isDigit()) number()
                else reportError(line, "Unexpected character '$c'.")
        }
        }
    }
    //for REPL
    fun resetTokenizer(){
        current = 0
        tokens.clear()
    }
    //set line
    fun setLine(lineNo: Int){
        line = lineNo
    }

    fun printTokens() {
        for (token in tokens) {
            System.out.write(token.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }
}