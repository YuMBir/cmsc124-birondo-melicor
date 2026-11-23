package atelier

/*
 * One interface for anything that can be invoked.
 * evalCall() in Evaluator.kt only talks to this interface, so the not-callable check and the
 * arity check live in ONE place and apply equally to user circles and native functions.
 */
interface Callable {
    fun arity(): Int                                            // how many parameters it expects
    fun call(evaluator: Evaluator, args: List<Any?>): Any?      // run it and give back a result
}

/*
 * Courier for `manifest`. It is NOT an error, it just carries the returned value up the host call
 * stack, skipping every execute() frame in between (blocks, loops, ifs), until UserFunction.call
 * catches it.
 * It extends RuntimeException, NOT RuntimeError, so evaluateProgram/executeProgram (which catch
 * RuntimeError) never mistake it for a real failure.
 * The last two constructor args (false, false) turn off suppression and stack trace capture.
 * We throw one of these on every `manifest`, so building a stack trace each time is wasted work.
 */
class ReturnSignal(val value: Any?) : RuntimeException(null, null, false, false)

/*
 * A circle declared by the user.
 *   declaration = the AST node (name, parameter tokens, body block)
 *   closure     = the Environment that was ACTIVE WHERE THE CIRCLE WAS DECLARED
 *
 * Keeping `closure` is what makes the language lexically scoped: a name inside the body means
 * what it meant where the circle was written, not what it means in whoever calls it. It is also
 * what keeps captured variables alive after the defining call finished (closures), because this
 * object holds a reference to that Environment.
 */
class UserFunction(
    private val declaration: CircleFun,
    private val closure: Environment
) : Callable {

    override fun arity(): Int = declaration.params.size

    override fun call(evaluator: Evaluator, args: List<Any?>): Any? {
        // A FRESH environment on EVERY call. If we made one per circle instead, recursion would
        // share a single set of parameters and break.
        // Its parent is `closure` (definition site), NOT the caller's environment.
        // Using the caller's environment here would give dynamic scoping, which is a defect.
        val env = Environment(closure)

        // Parameters are always local, so define() (never assign()).
        // evalCall already checked arity, so args.size == params.size here.
        for (i in declaration.params.indices) {
            env.define(declaration.params[i], args[i])
        }

        evaluator.callDepth++   // lets `manifest` know it is legally inside a circle
        try {
            evaluator.executeBlock(declaration.body, env)
        } catch (r: ReturnSignal) {
            return r.value       // a `manifest` landed here
        } finally {
            evaluator.callDepth--  // runs on normal finish, on manifest, and on runtime errors
        }
        return null              // fell off the end without manifest -> null
        //in more clear way, if a circle do not use manifest inside the circle, function, then it returns null in eval
    }

    // So `etch(f)` prints something readable instead of a Kotlin class name.
    override fun toString(): String = "<circle ${declaration.name.getLexeme()}>"
}