package atelier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

//use this for scanner errors, it's good practice to have seperate fail functions so we know where the error comes from
private fun fail(message: String): Nothing {
    System.err.println("Scanner: $message")
    exitProcess(65)
}

fun scan(path: String): String{
    val source = try { //source contains the string
        Files.readString(Path.of(path), StandardCharsets.UTF_8) //read entire contents of a text file as a single string
    } catch (error: Exception) {
        fail("cannot read '$path': ${error.message}")
    }
    return source
}





