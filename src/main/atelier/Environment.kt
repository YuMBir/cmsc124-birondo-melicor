package atelier

class Environment (private val enclosingEnv: Environment? = null){
    private val values = mutableMapOf<String, Any?>()
    /**
     * Creates a new key `name` in values map, throws `RuntimeException` if it already exists in environment
     */
    fun define (name: String, value: Any? = null, token: Token? = null){
        if (values.containsKey(name)){
            throw RuntimeError(token, "Variable '$name' already defined in this environment.")
        }
        values[name] = value
    }

    fun get(name: String, token: Token? = null): Any? {
        val env = lookup(name, token)
        return env.values[name]
    }

    /**
     * Looks up if `name` key exists.  Changes the value assigned to key `name` in the values map
     */
    fun assign(name: String, value: Any?, token:Token? = null){
        val env = lookup(name, token)
        env.values[name] = value
    }

    /**
     * Returns `Environment` if name is defined in environment or enclosing environments, else returns a RuntimeException
     */
    fun lookup(name: String) : Environment {
        var env: Environment = this
        while (env != null){
            if (env.values.containsKey(name)){
                return env
            }
            env = env.enclosingEnv ?: break
        }
        throw RuntimeError(token, "Variable '$name' is not defined in this environment.")
    }
}