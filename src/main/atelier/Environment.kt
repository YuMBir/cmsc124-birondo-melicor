package atelier

class Environment (private val enclosingEnv: Environment? = null){
    private val values = mutableMapOf<String, Any?>()

    fun define (name: String, value: Any? = null){
        if (values.containsKey(name)){
            throw IllegalArgumentException("Environment already defined for name $name")
        }
        values[name] = value
    }
    fun get(name: String): Any? {
        val env = lookup(name)
        return env.values[name]
    }

    fun assign(name: String, value: Any?){
        val env = lookup(name)
        env.values[name] = value
    }

    /**
     * returns Environment if name is defined in environment or enclosing environments, else returns a RuntimeException
     */
    fun lookup(name: String) : Environment {
        var currentEnv = this
        while (!(currentEnv.values.containsKey(name))) {
            if (currentEnv.enclosingEnv == null){
                throw RuntimeException("$name not defined for environment")
            }else{
                currentEnv = currentEnv.enclosingEnv
            }
        }
        return currentEnv
    }
}