package org.kadf.app.eureka

import org.kadf.app.eureka.semantic.EurekaException
import java.io.File
import java.io.FileInputStream


fun main(args: Array<String>) {
    val filename = "sema/basic-package/basic-69.mx"
    val file = FileInputStream(filename)
    val compiler = EurekaCompiler(file)
    try {
        compiler.eureka()
    }
    catch(e: EurekaException) {
        println("MxE")
        println(e.message)
        println(e.ctx?.start ?: "null")
        println(e.ctx?.stop ?: "null")
        throw Exception()
    }
    catch(e: Exception) {
        println("test failed")
        println(e.message)
        throw e
    }
    println("test succeeded")
    var cnt = 10
    File(filename).forEachLine {
        if (cnt > 0) {
            cnt--
            println(it)
        }
    }
//    val input = try {
//        when (args[1]) {
//            "-console" -> System.`in`
//            "-file" -> FileInputStream(args[2])
//            else -> throw Exception("unknown input option.")
//        }
//    } catch (e: IndexOutOfBoundsException) {
//        println("too few arguments.")
//        return
//    } catch (e: NoSuchFileException) {
//        println("code file not found.")
//        return
//    } catch (e: Exception) {
//        println(e.message)
//        return
//    }.let {
//        CharStreams.fromStream(it!!)
//    }
//
//    when (args[0]) {
//        "testrig" -> {}
//        "parse" -> {}
//        "semantic" -> {}
//        "codegen" -> {}
//        else -> {}
//    }
}