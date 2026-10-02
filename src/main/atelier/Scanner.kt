package atelier
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

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
    private val keywords = mapOf( //keywords of our language
        "manifest" to TokenType.MANIFEST,
        "whilst" to TokenType.WHILST,
        "etch" to TokenType.ETCH,
        "scry" to TokenType.SCRY,
        "else" to TokenType.ELSE,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "and" to TokenType.AND,
        "or" to TokenType.OR,
        "circle" to TokenType.CIRCLE,
        "sigil" to TokenType.SIGIL,
        "imbue" to TokenType.IMBUE,
        "null" to TokenType.NIL
    )


    private fun isAtEnd() = current >= source.length //checks for file ending
    private fun advance(): Char { //consumes the character
        check(!isAtEnd()){//added this, as there's no need for advance after EOF
            "advance() called at EOF, current-$current"
        }
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
    private fun addToken(type: TokenType, literal: Any? = null, startLine: Int = line, endLine: Int = line) { //replaced tokenLine: Int = line
        val text = source.substring(start,current)
        tokens.add(Token(type,text,literal, startLine, endLine))// replace tokenLine with startLine and endLine
    }
    private fun isIdentifierAllowedChar(c: Char): Boolean = c in 'A'..'Z' || c in 'a'..'z' || c == '_' || c =='-'
    private fun identifier() {
        while (isIdentifierAllowedChar(peek()) || peek().isLetterOrDigit()) advance() //looks through whole identifier
        val text = source.substring(start, current) //identifier text
        val type = keywords[text] ?: TokenType.IDENTIFIER //keyword or identifier
        val literal: Any? = when (type) { //for the handling of boolean, true or false
            TokenType.TRUE -> true
            TokenType.FALSE -> false
            else -> null
        }
        addToken(type, literal)
    }
    private fun number() { //this is for dealing with numbers, updated to deal with number format errors
        while (peek().isDigit()) advance()// consume the integer part

        if (peek() == '.' && peekNext().isDigit()) { //if decimal point
            advance() // consume the '.'
            while (peek().isDigit()) advance()
        }else if (peek() == '.' && peekNext().isLetter()){ //for 3.toString
            advance()
            while (isIdentifierAllowedChar(peek()) || peek().isLetterOrDigit()) advance()
            reportError(line, "Invalid, letter after a decimal point.")
            return
        }
        if (peek().isLetter()){ //for the 3variable
            while (isIdentifierAllowedChar(peek()) || peek().isLetterOrDigit()) advance()
            reportError(line, "Identifier starts with a number.")
            return
        }

        val value = source.substring(start, current)
        val d = value.toDoubleOrNull()//added this, for more checking in the number... to be elaborated
        if (d == null) {
            reportError(line,"Invalid number literal '$value'.")
            return
        } 
        addToken(TokenType.NUMBER, d)
    }
    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    

    private fun string(){ 
        val startLine = line
        val sb = StringBuilder() // holds the decoded value, escapes are already resolved by the time a char lands here
        var valid = true
        while (peek()!= '"' && !isAtEnd()){ //this goes on until a closing quote is found/ run out of input to peek
            if ( peek() == '\\'){ //if the char is the start of an escape sequence
                advance() //consume backlash, not adding to sb
                if (isAtEnd()) { //backlash is the last char in the file
                    break
                }
                when (val e = advance()){ //checks the char after backslash
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    else -> {
                        reportError(line, "Unknown escape '\\$e'.") //report unknown escape
                        valid=false
                    } //unknown escape: nothing appended, string will be rejected
                }
                
            }
            else{ //for ordinary char
                    sb.append(advance())
            }
        }
        if(isAtEnd()){ //loop exit because no input left
            reportError(startLine, "Unterminated string.")
            addToken(TokenType.STRING, source.substring(start+1, current))
            return
        } 
        advance()
        val endLine = line
        if (valid) {
            addToken(TokenType.STRING,  sb.toString(), startLine, endLine)
        }
    }



    private fun reportError(line: Int, message: String) {
        hadError = true
        //replace the message for fa-il(), to not directly call exitProcess, just print the error and keep running
        System.err.println("[line $line] Error: $message")
    }


    fun printCode(){ //prints the entire source as UTF-8
        System.out.write(source.toByteArray(StandardCharsets.UTF_8))
    }

    //turn source text into a list of tokens
    fun tokenize(){
        while (!isAtEnd()){
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", startLine = line, endLine = line))
    }
    private fun scanToken(){ //decides what type of token
        when (val c = advance()){
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ':' -> addToken(TokenType.COLON)
            ';' -> addToken(TokenType.SEMICOLON)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '*' -> addToken(TokenType.STAR)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if(match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '!' -> addToken(if(match('=')) TokenType.NOT_EQUAL else TokenType.BANG)
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
                    }
                    else {
                        advance() //consumes *
                        advance() //skip a character inside the comment
                    }
                }
                else {
                    addToken(TokenType.SLASH) // for non comments
                }
            }
            ' ', '\r', '\t' -> {}         //ignore whitespace
            '\n' -> addToken(TokenType.NEWLINE, startLine = line -1, endLine = line -1)
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

    fun getTokenList(): List<Token>{
        return tokens
    }

    fun printTokens() {
        for (token in tokens) {
            System.out.write(token.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }
}
