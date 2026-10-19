package atelier

import kotlin.system.exitProcess

/*thrown when a valid program hits a bad value at runtime, an example would be negating a string
it also carries the token so Main.kt can report the line number.
token is nullable because a few errors have no specific token. */
class RuntimeError(val token: Token?, message: String) : RuntimeException(message)
class Evaluator {
    private val exitStatus = 0
    fun getExitStatus(): Int {
        return exitStatus
    }
    fun evaluate (node: Expr): Any? = when(node){
        is Literal -> node.token.getLiteralValue() //return the value stored in the token
        is Group -> evaluate(node.node)//evaluate the inside of the parentheses 
        is Arguments -> evalArguments(node)
        is UnaryOp -> evalUnary(node)
        is Binary-> evalBinary(node)
        is Identifier -> throw RuntimeError(node.token, "Variable is not supported yet.")
        //not implemented as of yet which are Identifier, Block, and etc.
        else -> throw RuntimeError(null, "Cannot evaluate this expression yet.")
    }
    fun evaluateProgram (program: Program): List<Any?> {
        val statements = program.getStatements()
        val values = mutableListOf<Any?>()
        for (node in statements){
            try {
                values.add(evaluate(node))
            } catch (e: RuntimeError) {
                System.err.println("[line ${e.token?.getStartLine() ?: "?"}] Runtime error: ${e.message}") // the one place runtime errors are caught, message to stderr, exit 70 (not 65)
                exitProcess(70)
            }

        }


        return values
    }
    private fun evalUnary(node: UnaryOp): Any {
        val right = evaluate(node.node)
        return when (node.operator.getType()) {
            TokenType.MINUS -> -checkNumberOperand(node.operator, right) // throws if not a number
            TokenType.BANG -> !isTruthy(right)
            //the ! is not yet implemented
            else -> throw RuntimeError(node.operator, "Unsupported unary operator.")
        }
    }
    private fun evalBinary(node: Binary): Any {
        //from left to right
        val left = evaluate(node.left)
        val right = evaluate(node.right)
        val op = node.operator
        return when (op.getType()){
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                left is String || right is String -> stringify(left) + stringify(right) 
                else -> throw RuntimeError(op, "Operands must be two numbers or two strings.")
                
            }
            TokenType.MINUS -> {
                val (l,r) = checkNumberOperands(op, left, right)
                l - r
            }
            TokenType.STAR -> {
               val (l,r) = checkNumberOperands(op, left, right)
               l * r
            }
            TokenType.SLASH -> {
                val (l,r) = checkNumberOperands(op, left, right)
                if (r == 0.0) throw RuntimeError (op, "Division by zero.")
                l/r
            }
            TokenType.GREATER -> {
                val (l,r) = checkNumberOperands(op, left, right)
                l > r
            }
            TokenType.GREATER_EQUAL -> {
                val (l,r) = checkNumberOperands(op, left, right)
                l >= r
            }
            TokenType.LESS -> {
                val (l,r) = checkNumberOperands(op, left, right)
                l < r
            }
            TokenType.LESS_EQUAL -> {
                val (l,r) = checkNumberOperands(op, left, right)
                l <= r
            }
            TokenType.AND ->{
                val l = evaluate(node.left)
                return if (!isTruthy(l)) false else isTruthy(evaluate(node.right))
            }
            TokenType.OR -> {
                val l = evaluate(node.left)
                return if (isTruthy(l)) true else isTruthy(evaluate(node.right))

            }
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            TokenType.NOT_EQUAL -> !isEqual(left,right)
            
            
            else -> throw RuntimeError(op, "Unsupported operator.")
        }
    }
    private fun checkNumberOperand(op: Token, v: Any?): Double { // for the unary 
        if (v is Double) return v
        throw RuntimeError(op, "Operand must be a number.")
    }
    private fun checkNumberOperands(op: Token, l: Any?, r: Any?): Pair<Double, Double> {//for the binary
        if (l is Double && r is Double) return Pair(l,r)
        throw RuntimeError(op, "Operands must be numbers.")
    }
    private fun evalArguments(node: Arguments): Any? { //evaluates expressions such as (args (+ 2.0 (* 3.0 4.0)))
        var result: Any? = null
        for (expr in node.getArgs()) result = evaluate(expr)
        return result
    }
    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true //nil == nil
        if (a == null || b == null) return false 
        if (a is Double && b is Double) return a == b
        return a == b
    }
    private fun isTruthy(v: Any?): Boolean = when(v){
        null -> false
        is Boolean -> v
        is Double -> !(v == 0.0 || v.isNaN())
        else -> true
    }
    fun stringify(value: Any?): String = when (value) {
        null -> "nil"
        // doubles print as 5.0; trim the ".0" so 2 + 3 prints "5"
        is Double -> { //turns number (double) into a string, drop the .0 if the number is 5.0, but if 5.5 then don't remove ".0"
            val s = value.toString()
            if (s.endsWith(".0")) s.dropLast(2) else s
        }


        // strings print without quotes; booleans print true/false
        else -> value.toString()
    }



    




}