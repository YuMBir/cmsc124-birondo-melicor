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
    private val keywords = mapOf( //added keywords for loops, and boolean
        "var" to "VAR",
        "print" to "PRINT", 
        "for" to "FOR",
        "while" to "WHILE",
        "if" to "IF",
        "else" to "ELSE",
        "true" to "TRUE",
        "false" to "FALSE",
        "and" to "AND",
        "or" to "OR",
        "circle" to "CIRCLE",
        "sigil" to "SIGIL",
        "imbue" to "IMBUE"
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
    private fun addToken(type: String, literal: Any? = null, tokenLine: Int = line) { //added tokenLine: Int = line to avoid the buggy line number saved 
        val text = source.substring(start,current)
        tokens.add(Token(type,text,literal,tokenLine))
    }
    private fun identifier() {
        while (peek().isLetterOrDigit()) advance() //looks through whole identifier
        val text = source.substring(start, current) //identifier text
        val type = keywords[text] ?: "IDENTIFIER" //keyword or identifier
        val literal: Any? = when (type) { //for the handling of boolean, true or false
            "TRUE" -> true
            "FALSE" -> false
            else -> null
        }
        addToken(type, literal)
    }
    private fun number() { //this is for dealing with numbers, updated to deal with number format errors
        while (peek().isDigit()) advance()// handles decimal point

        if (peek() == '.' && peekNext().isDigit()) { //if decimal poimt
            advance() // consume the '.'
            while (peek().isDigit()) advance()
        }
        val value = source.substring(start, current)
        val d = value.toDoubleOrNull()//added this, for more checking in the number... to be elaborated
        if (d == null) {
            reportError(line,"Invalid number literal '$value'.")
            addToken("NUMBER", 0.0)
        } else{
            addToken("NUMBER", d)
        }
    }
    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    private fun isAllowedInString(c: Char): Boolean = c in 'A'..'Z' || c in 'a'..'z' || c == '_' 

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
                        reportError(line, "Unkown escape '\\$e'.") //report unknown escape
                        valid=false
                    } //the unknown char will recorded, to not lose the data
                }
            } else{ //for ordinary char
                val c = advance()
                when {
                    c == '\n' -> sb.append(c) // raw newlines allowed
                    c == '\r' && peek() == '\n' -> { /* skip, the '\n' is appended next */ }
                    !isAllowedInString(c) -> {
                        reportError(line, "Invalid character '$c' in string; only A-Z, a-z, and '_' allowed.")
                        valid = false
                    }
                    else -> sb.append(c)
            }
            }
        }
        if(isAtEnd()){ //loop exit because no input left
            reportError(startLine, "Unterminated string.")
        } else {
            advance()
        }
        if (valid) {
            addToken("STRING",  sb.toString(), startLine)
        }
    }



    private fun reportError(line: Int, message: String) {
        hadError = true
        //replace the message for fa-il(), to not directly call exitProcess, just print the the error and keep running
        System.err.println("[line $line] Error: $message")
    }
//fail("[line $line] Error: $message")


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
            ';' -> addToken("SEMICOLON")
            '.' -> addToken("DOT")
            '*' -> addToken("STAR")
            '=' -> addToken(if (match('=')) "EQUAL_EQUAL" else "EQUAL")
            '+' -> addToken(if (match('+')) "INCREMENT" else "PLUS")
            '-' -> addToken(if(match('-')) "DECREMENT" else "MINUS")
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

    fun printTokens() {
        for (token in tokens) {
            System.out.write(token.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }
}