package atelier

import java.nio.charset.StandardCharsets

/*thrown when a valid program hits a bad value at runtime, an example would be negating a string
it also carries the token so Main.kt can report the line number.
token is nullable because a few errors have no specific token. */
class RuntimeError(val token: Token?, message: String) : RuntimeException(message)
class Evaluator {
    private var exitStatus = 0
    private var environment = Environment()
    fun getExitStatus(): Int {
        return exitStatus
    }
    fun evaluate (node: Expr): Any? = when(node){
        is Literal -> node.token.getLiteralValue() //return the value stored in the token
        is Group -> evaluate(node.node)//evaluate the inside of the parentheses 
        is Arguments -> evalArguments(node)
        is UnaryOp -> evalUnary(node)
        is Binary-> evalBinary(node)
        is Identifier -> environment.get(node.getName(), node.token) //added node.token to get the line number for error reporting
        else -> throw RuntimeError(null, "Cannot evaluate this expression yet.")
    }
    fun evaluateProgram(program: Program): List<Any?> {
        val values = mutableListOf<Any?>()
        for (stmt in program.getStatements()) {
            try {
                when (stmt) {
                    is ExprStmt -> values.add(evaluate(stmt.expr)) 
                    else -> execute(stmt)                          
                }
            } catch (e: RuntimeError) {
                System.err.println("[line ${e.token?.getStartLine() ?: "?"}] Runtime error: ${e.message}")
                exitStatus = 70
            }
        }
        return values
    }
    private fun evalUnary(node: UnaryOp): Any {
        val right = evaluate(node.node)
        return when (node.operator.getType()) {
            TokenType.MINUS -> -checkNumberOperand(node.operator, right) // throws if not a number
            TokenType.BANG -> !isTruthy(right)
            else -> throw RuntimeError(node.operator, "Unsupported unary operator.")
        }
    }
    private fun evalBinary(node: Binary): Any {
        val op = node.operator

        // Examples:  false and (1 / 0)  ->  false   (right side never runs)
       //                      true or nope       ->  true    (right side never runs)
        if (op.getType() == TokenType.AND) {
            return isTruthy(evaluate(node.left)) && isTruthy(evaluate(node.right))
        }
        if (op.getType() == TokenType.OR) {
            return isTruthy(evaluate(node.left)) || isTruthy(evaluate(node.right))
        }

        //from left to right
        val left = evaluate(node.left)
        val right = evaluate(node.right)
        return when (op.getType()){
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                left is String || right is String -> left.toString() + right.toString()
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
        is String -> v.isNotEmpty()
        else -> true
    }

    // Plain ./run file, stop at the first runtime error. Main then exits with exitStatus (70).
    fun executeProgram(program: Program) {
        for (statement in program.getStatements()) {
            try {
                execute(statement)
            } catch (e: RuntimeError) {
                System.err.println("[line ${e.token?.getStartLine() ?: "?"}] Runtime error: ${e.message}")
                exitStatus = 70
                return
            }
        }
    }
    fun executeBlock(block: Block, blockEnv: Environment) {
        val statements = block.getStatements()
        val prevEnv = this.environment
        try{
            this.environment = blockEnv
            for (statement in statements){
                execute(statement)
            }
        }finally {
            this.environment = prevEnv // restore even when a statement throws, so the REPL stays usable
        }
    }

    fun execute(stmt: Stmt){
        when (stmt){
            is Block -> executeBlock(stmt, Environment(this.environment)) //new scope
            is ExprStmt -> evaluate(stmt.expr) //evaluate the expression and discard the value
            is Etch -> {
                val value = evaluate(stmt.getArgs())
                System.out.write("$value\n".toByteArray(StandardCharsets.UTF_8))
            }
            is Sigil -> {
                val init = stmt.getExpr()
                if (init == null){
                    environment.define(stmt.getToken())
                }else{
                    // initializer is evaluated before define, so `sigil x = x` can't see the new x
                    environment.define(stmt.getToken(), evaluate(init))
                }
            }
            is Assignment -> {
                environment.assign(stmt.getToken(), evaluate(stmt.getExpr()))
            }
            else -> throw RuntimeError(null, "Cannot execute this statement yet.") 
        }
    }
}