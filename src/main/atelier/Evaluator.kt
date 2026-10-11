package atelier

// thrown when a valid program hits a bad value at runtime, an example would be negating a string
// it also carries the token so Main.kt can report the line number.
// token is nullable because a few errors have no specific token.
class RuntimeError(val token: Token?, message: String) : RuntimeException(message)
