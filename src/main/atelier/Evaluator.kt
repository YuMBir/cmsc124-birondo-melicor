package atelier
/*thrown when a valid program hits a bad value at runtime, an example would be negating a string
it also carries the token so Main.kt can report the line number.
token is nullable because a few errors have no specific token. */
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
    private fun evalUnary(node: UnaryOp): Any? {
        val right = evaluate(node.node)
        return when (node.operator.getType()) {
            TokenType.MINUS -> {
                checkNumberOperand(node.operator, right) // throws if not a number
                -(right as Double) // it is a number then turn it into a Double then negate it
            }
            //the ! is not yet implemented
            else -> throw RuntimeError(node.operator, "Unsupported unary operator.")
        }
    }
    private fun evalBinary(node: Binary): Any? {
        //from left to right
        val left = evaluate(node.left)
        val right = evaluate(node.right)
        val op = node.operator
        return when (op.getType()){
            TokenType.PLUS -> {
                checkNumberOperands(op, left, right)
                (left as Double) + (right as Double)
            }
            TokenType.MINUS -> {
                checkNumberOperands(op, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(op, left, right)
                (left as Double) * (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(op, left, right)
                if (right == 0.0) throw RuntimeError(op, "Division by zero.") //division by zero
                (left as Double) / (right as Double)
            }
            // comparison, equality, and/or, string + are not implemented as of yet
            else -> throw RuntimeError(op, "Unsupported operator.")
        }
    }    



}