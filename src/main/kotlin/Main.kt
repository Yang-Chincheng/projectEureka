package org.kadf.app.eureka

//import org.antlr.v4.*
import org.antlr.v4.runtime.CharStreams
import java.io.FileInputStream
import java.lang.IndexOutOfBoundsException

fun main(args: Array<String>) {
    val input = try {
        when (args[1]) {
            "console" -> System.`in`
            "file" -> FileInputStream(args[2])
            else -> throw Exception("unknown input option.")
        }
    } catch(e: IndexOutOfBoundsException) {
        println("too few arguments.")
        return
    } catch(e: NoSuchFileException) {
        println("code file not found.")
        return
    } catch(e: Exception) {
        println(e.message)
        return
    }.let {
        CharStreams.fromStream(it)
    }

    when (args[0]) {
        "testrig" -> {}
        "parse" -> {}
        "semantic" -> {}
        "codegen" -> {}
        else -> {}
    }
}