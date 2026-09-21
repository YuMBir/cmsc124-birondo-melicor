package atelier

class Parser(private var tokens: List<Token>) {
    var expressions = mutableListOf<Expr>()
    fun printAST(){
        for (expression in expressions){
            println(expression)
        }
    }
    fun parse(): Expr{
        TODO()
    }
    fun expression(): Expr{
        TODO()
    }
    fun term(): Expr{
        TODO()
    }
    fun factor(): Expr{
        TODO()
    }
    fun primary(): Expr{
        TODO()
    }
}

