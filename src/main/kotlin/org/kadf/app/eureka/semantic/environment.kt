package org.kadf.app.eureka.semantic

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.ASTNode

open class Environment<B>(
    val outer: Environment<B>?,
    vararg init: Pair<String, B>
) {
    private val bindings = hashMapOf(*init)
    var capture = true

    val isGlobal get() = outer == null

    open fun contain(id: String) = bindings[id] != null;

    open fun lookup(id: String): B? {
        return bindings[id] ?: if(capture) outer?.lookup(id) else null
    }

    open fun register(id: String, binding: B): Boolean {
        if (bindings.contains(id)) throw Exception()
        return true.also { bindings[id] = binding }
    }
}

typealias TypeBinding = VarEnv
typealias VarBinding = IType

class TypeEnv: Environment<TypeBinding>(null) {
    init {
        register(
            "array",
            TypeBinding(null).apply {
                registerFun("size", FunctionType(IntegerType, listOf()))
            }
        )
        register(
            "string",
            TypeBinding(null).apply {
                registerFun("length", FunctionType(IntegerType, listOf()))
                registerFun("substring", FunctionType(StringType, listOf(IntegerType, IntegerType)))
                registerFun("parseInt", FunctionType(IntegerType, listOf()))
                registerFun("ord", FunctionType(IntegerType, listOf(IntegerType)))
            }
        )
        register("unit", TypeBinding(null))
        register("null", TypeBinding(null))
        register("integer", TypeBinding(null))
        register("boolean", TypeBinding(null))
        register("function", TypeBinding(null))
    }

    fun containType(type: IType): Boolean = when(type) {
        is ArrayType -> containType(type.type)
        is FunctionType -> containType(type.retType) && type.paraTypes.all { containType(it) }
        else -> super.contain(type.regiId())
    }
    fun lookupType(type: IType) = super.lookup(type.regiId())
    fun registerType(type: IType, env: VarEnv) = super.register(type.regiId(), env)
}

class VarEnv(
    outer: Environment<VarBinding>?,
    val node: ASTNode? = null,
    vararg init: Pair<String, VarBinding>
): Environment<VarBinding>(outer, *init) {
    fun containVar(id: String) = super.contain("var-$id")
    fun containFun(id: String) = super.contain("fun-$id")
    fun lookupVar(id: String) = super.lookup("var-$id") as? IDeclarableType
    fun lookupFun(id: String) = super.lookup("fun-$id") as? FunctionType
    fun registerVar(id: String, type: IDeclarableType) = super.register("var-$id", type)
    fun registerFun(id: String, type: FunctionType) = super.register("fun-$id", type)

    var isClass = false
    var isFunction = false
    var isLoop = false
    var isLambda = false
    var inferredReturnType: IType? = null

    private val outerEnv: List<VarEnv> get() {
        var env = this
        val ret = mutableListOf<VarEnv>()
        while(env.outer != null) {
            ret.add(env)
            env = env.outer as VarEnv
        }
        return ret
    }
    val outerFunc get() = outerEnv.firstOrNull { it.isFunction }
    val outerClass get() = outerEnv.firstOrNull { it.isClass }
    val outerLambda get() = outerEnv.firstOrNull { it.isLambda }
    val outerLoop get() = outerEnv.firstOrNull { it.isLoop }
}

class EnvManager(private val type: TypeEnv) {

    val global = VarEnv(null)
    var current = global

    init {
        global.registerFun("print", FunctionType(UnitType, listOf(StringType)))
        global.registerFun("println", FunctionType(UnitType, listOf(StringType)))
        global.registerFun("printInt", FunctionType(UnitType, listOf(IntegerType)))
        global.registerFun("printlnInt", FunctionType(UnitType, listOf(IntegerType)))
        global.registerFun("getString", FunctionType(StringType, listOf()))
        global.registerFun("getInt", FunctionType(IntegerType, listOf()))
        global.registerFun("toString", FunctionType(StringType, listOf(IntegerType)))
    }

    fun enter(node: ASTNode? = null): VarEnv {
        current = VarEnv(current, node)
        return current
    }
    fun leave(): VarEnv {
        current = current.outer as? VarEnv?: global
        return current
    }

    fun checkVar(id: String) = !type.containType(UserDefinedType(id)) && !current.containVar(id)
    fun checkFun(id: String) = !type.containType(UserDefinedType(id)) && !current.containFun(id)
//    fun registerVar(id: String, tp: IDeclarableType) = if (type.checkType(tp)) current.registerVar(id, tp) else throw Exception()
//    fun registerFun(id: String, tp: FunctionType) = if (type.checkType(tp)) current.registerFun(id, tp) else throw Exception()
}