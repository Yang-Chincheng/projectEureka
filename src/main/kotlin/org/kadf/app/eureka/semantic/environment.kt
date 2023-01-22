package org.kadf.app.eureka.semantic

import org.kadf.app.eureka.ast.*
import org.kadf.app.eureka.ast.nodes.AstNode
import org.kadf.app.eureka.ir.IrValue
import org.kadf.app.eureka.utils.DefaultErrorHandler
import org.kadf.app.eureka.utils.ErrorHandler

class TypeBinding {
    val properties = mutableMapOf<String, AstType>()
    val methods = mutableMapOf<String, AstFuncType>()
}

class TypeEnv {
    private val bindings = mutableMapOf<String, TypeBinding>()

    fun contain(id: String) = bindings[id] != null
    fun containType(type: AstType): Boolean = when (type) {
        is AstArrayType -> containType(type.type)
        is AstFuncType -> containType(type.retType) && type.paraTypes.all { containType(it) }
        else -> contain(type.typeId)
    }

    private fun lookup(id: String): TypeBinding? = bindings[id]
    fun lookupType(type: AstType) = lookup(type.typeId)
    fun lookupProp(type: AstType, id: String): AstType? = lookupType(type)?.properties?.get(id)
    fun lookupMeth(type: AstType, id: String): AstFuncType? = lookupType(type)?.methods?.get(id)


    fun register(id: String, binding: TypeBinding, handler: ErrorHandler = DefaultErrorHandler) {
        if (bindings.contains(id)) handler.report("duplicate declaration of type $id")
        bindings[id] = binding
    }

    fun registerType(type: AstType, handler: ErrorHandler = DefaultErrorHandler) {
        register(type.typeId, TypeBinding(), handler)
    }

    fun registerProp(type: AstType, id: String, pType: AstType, handler: ErrorHandler = DefaultErrorHandler) {
        lookupType(type)?.properties?.let {
            if (it.contains(id)) handler.report("duplicate declaration of property $id")
            it[id] = pType
        } ?: run {
            handler.report("try registering property of an nonexistent type")
        }

    }

    fun registerMeth(type: AstType, id: String, mType: AstFuncType, handler: ErrorHandler) {
        lookupType(type)?.methods?.let {
            if (it.contains(id)) handler.report("duplicate declaration of method $id")
            it[id] = mType
        } ?: run {
            handler.report("try registering method of an nonexistent type")
        }
    }
}

class VarBinding(
    val type: AstType
) {
    lateinit var value: IrValue
}

class VarEnv(
    val outer: VarEnv?,
    val node: AstNode? = null,
    typeEnv: TypeEnv? = null
) {
    var capture = true
    private val bindings = mutableMapOf<String, VarBinding>()
    val tEnv: TypeEnv = typeEnv ?: outer!!.tEnv
    
    var isClass = false
    var isFunction = false
    var isLoop = false
    var isLambda = false
    var returnType: AstType? = null
    private val outerEnv: List<VarEnv>
        get() {
            var env = this
            val ret = mutableListOf<VarEnv>()
            while (env.outer != null) {
                ret.add(env)
                env = env.outer!!
            }
            return ret
        }

    private fun contain(id: String) = bindings[id] != null
    fun containVar(id: String) = contain("var-$id")
    fun containFun(id: String) = contain("fun-$id")

    private fun lookup(id: String): VarBinding? {
        return bindings[id] ?: if (capture) outer?.lookup(id) else null
    }

    fun getVarType(id: String) = lookup("var-$id")?.type
    fun getFunType(id: String) = lookup("fun-$id")?.type as? AstFuncType

    fun getVarValue(id: String) = lookup("var-$id")?.value
    fun getFunValue(id: String) = lookup("fun-$id")?.value
    fun setVarValue(id: String, value: IrValue) {
        lookup("var-$id")?.let { it.value = value }
    }

    fun setFunValue(id: String, value: IrValue) {
        lookup("fun-$id")?.let { it.value = value }
    }

    private fun register(id: String, binding: VarBinding, handler: ErrorHandler = DefaultErrorHandler) {
        if (bindings.contains(id)) handler.report("registry failed")
        bindings[id] = binding
    }

    fun registerVar(id: String, type: AstType, handler: ErrorHandler = DefaultErrorHandler) {
        if (tEnv.contain("def-$id")) handler.report("registry id conflict with type id")
        register("var-$id", VarBinding(type), handler)
    }

    fun registerFun(id: String, type: AstFuncType, handler: ErrorHandler = DefaultErrorHandler) {
        if (tEnv.contain("def-$id")) handler.report("registry id conflict with type id")
        register("fun-$id", VarBinding(type), handler)
    }

    val outerFunc get() = outerEnv.firstOrNull { it.isFunction }
    val outerClass get() = outerEnv.firstOrNull { it.isClass }
    val outerLambda get() = outerEnv.firstOrNull { it.isLambda }
    val outerLoop get() = outerEnv.firstOrNull { it.isLoop }
//    fun isMemberVar(id: String): Boolean {
//        return outerEnv.firstOrNull { it.containVar(id) }?.isClass ?: false
//    }
//
//    fun isMemberFun(id: String): Boolean {
//        return outerEnv.firstOrNull { it.containFun(id) }?.isClass ?: false
//    }
//
}

class EnvManager(root: AstNode, tEnv: TypeEnv) {

    val global = VarEnv(null, root, tEnv)
    var current = global

    init {
        val arrayTypeBinding = TypeBinding().apply {
            methods["size"] = AstFuncType(AstIntegerType)
        }
        val stringTypeBinding = TypeBinding().apply {
            methods["length"] = AstFuncType(AstIntegerType)
            methods["substring"] = AstFuncType(AstStringType, AstIntegerType, AstIntegerType)
            methods["parseInt"] = AstFuncType(AstIntegerType)
            methods["ord"] = AstFuncType(AstIntegerType, AstIntegerType)
        }
        tEnv.apply {
            register("array", arrayTypeBinding)
            register("string", stringTypeBinding)
            register("unit", TypeBinding())
            register("null", TypeBinding())
            register("integer", TypeBinding())
            register("boolean", TypeBinding())
            register("function", TypeBinding())
            register("any", TypeBinding())
            register("nothing", TypeBinding())
        }

        global.registerFun("print", AstFuncType(AstUnitType, AstStringType))
        global.registerFun("println", AstFuncType(AstUnitType, AstStringType))
        global.registerFun("printInt", AstFuncType(AstUnitType, AstIntegerType))
        global.registerFun("printlnInt", AstFuncType(AstUnitType, AstIntegerType))
        global.registerFun("getString", AstFuncType(AstStringType))
        global.registerFun("getInt", AstFuncType(AstIntegerType))
        global.registerFun("toString", AstFuncType(AstStringType, AstIntegerType))

    }

    fun enter(node: AstNode? = null): VarEnv {
        current = VarEnv(current, node)
        return current
    }

    fun leave(): VarEnv {
        current = current.outer ?: global
        return current
    }

}

