package atelier

class Environment(private val enclosingEnv: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    /**
     * Creates a new binding in THIS environment only (never walks outward).
     * Throws RuntimeError if the name already exists in this same scope.
     */
    fun define(token: Token, value: Any? = null) {
        val name = token.getLexeme()
        if (values.containsKey(name)) {
            throw RuntimeError(token, "Variable '$name' already defined in this environment.")
        }
        values[name] = value
    }

    fun get(name: String, token: Token? = null): Any? = lookup(name, token).values[name]

    /**
     * Updates the innermost existing binding. Never creates one.
     */
    fun assign(token: Token, value: Any?) {
        val name = token.getLexeme()
        lookup(name, token).values[name] = value
    }

    /**
     * Returns the Environment that holds `name`, searching outward through enclosing
     * environments. Throws RuntimeError if no environment has it.
     */
    fun lookup(name: String, token: Token? = null): Environment {
        var env: Environment? = this
        while (env != null) {
            if (env.values.containsKey(name)) return env
            env = env.enclosingEnv
        }
        throw RuntimeError(token, "Undefined variable '$name'.")
    }
}