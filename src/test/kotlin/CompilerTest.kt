import org.junit.jupiter.api.Test
import org.kadf.app.eureka.EurekaCompiler
import org.kadf.app.eureka.semantic.EurekaException
import java.io.File
import java.io.FileInputStream

class CompilerTest {

//    @Test
    fun testSource() {
        val filename = "sema/expression-package/expression-1.mx"
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
            throw e
        }
        catch(e: Exception) {
            println("test failed")
            println(e.message)
            throw e
        }
        println("test succeeded")
    }

    fun testSingle(file: File): Boolean {
        val input = FileInputStream(file)
        val compiler = EurekaCompiler(input)
        try {
            compiler.eureka()
        }
        catch(e: EurekaException) {
            throw e
        }
        catch(e: Exception) {
            throw e
        }
        return false
    }

    @Test
    fun testGroup() {
        val path = "sema"
        var cnt = 0
        File(path).walk().filter { it.isFile }.forEach {file ->
            if (file.name.endsWith(".mx")) {
                cnt++
                println("File #$cnt ${file.name}")
                val ans = file.readLines().firstOrNull { it.startsWith("Verdict") }?.contains("Success") ?: false
                try {
                    testSingle(file)
                    if (!ans) {
                        println("test failed. ${file.path}/${file.name}: out ok, answer not ok")
                        throw Exception("test failed")
                    }
                } catch (e: EurekaException) {
                    if (ans) {
                        println("test failed. ${file.path}/${file.name}: out not ok, answer ok")
                        println(e.message)
                        println(e.ctx?.start)
                        println(e.ctx?.stop)
                        throw Exception("test failed")
                    }
                } catch (e: Exception) {
                    if (ans) {
                        println("test failed. ${file.path}/${file.name}: out not ok, answer ok")
                        println(e.message)
                        throw Exception("test failed")
                    }
                }
                println()
            }
        }
        println("test passed")
    }

//    @Test
//    fun testMain() {
//        main(arrayOf("", ""))
//    }

}