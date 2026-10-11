package atelier

// thrown when a valid program hits a bad value at runtime, an example would be negating a string
// it also carries the token so Main.kt can report the line number.
// token is nullable because a few errors have no specific token.
class RuntimeError(val token: Token?, message: String) : RuntimeException(message)
class Evaluator {
    fun evaluate (node: Expr): Any? = when(node){

        is Literal -> node.token.getLiteralValue() //scanner computed value, here, just retun the value
        is Group -> evaluate(node.node)//evaluate the inside of the parentheses 
        is Arguments -> evalArguments(node)
        is UnaryOp -> evalUnary(node)
        is Binary-> evalBinary(node)
        //not implemented as of yet which are Identifier, Block, and etc.
        else -> throw RuntimeError(null, "Cannot evaluate this expression yet.")
    }
}