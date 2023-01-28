package org.kadf.app.eureka.ast

import org.kadf.app.eureka.MxStarParser.ArrayTypeContext
import org.kadf.app.eureka.MxStarParser.DeclarableTypeContext
import org.kadf.app.eureka.MxStarParser.NonVoidTypeContext
import org.kadf.app.eureka.MxStarParser.ReturnableTypeContext
import org.kadf.app.eureka.utils.DefaultErrorHandler
import org.kadf.app.eureka.utils.ErrorHandler

sealed class AstType {
    abstract val typeId: String
    abstract fun match(other: AstType): Boolean

    companion object {
        fun intersect(ty1: AstType?, ty2: AstType?, handler: ErrorHandler = DefaultErrorHandler): AstType? {
            if (ty1 == null) return ty2
            if (ty2 == null) return ty1
            if (!ty1.match(ty2)) handler.report("type intersection failed")
            if (ty1 is AstAnyType || ty1 is AstNullType) return ty2
            if (ty2 is AstAnyType || ty2 is AstNullType) return ty1
            return ty1
        }
    }
}

object AstAnyType : AstType() {
    //    override fun typeId() = "any"
    override val typeId = "any"
    override fun match(other: AstType) = true
}

object AstNothingType : AstType() {
    //    override fun typeId() = "nothing"
    override val typeId = "nothing"
    override fun match(other: AstType) = false
}

// First-Order Types
object AstUnitType : AstType() {
    //    override fun typeId() = "void"
    override val typeId = "unit"
    override fun match(other: AstType) = other is AstAnyType || other is AstUnitType
}

object AstNullType : AstType() {
    //    override fun typeId() = "null"
    override val typeId = "null"
    override fun match(other: AstType) =
        other is AstAnyType || other is AstNullType || other is AstArrayType || other is AstUserDefType
}

object AstIntegerType : AstType() {
    //    override fun typeId() = "int"
    override val typeId = "integer"
    override fun match(other: AstType) = other is AstAnyType || other is AstIntegerType
}

object AstBooleanType : AstType() {
    //    override fun typeId() = "bool"
    override val typeId = "boolean"
    override fun match(other: AstType) = other is AstAnyType || other is AstBooleanType
}

object AstStringType : AstType() {
    //    override fun typeId() = "string"
    override val typeId = "string"
    override fun match(other: AstType) = other is AstAnyType || other is AstStringType
}

class AstUserDefType(val id: String) : AstType() {
    //    override fun typeId() = id
    override val typeId = "def.$id"
    override fun match(other: AstType) =
        other is AstAnyType || other is AstNullType || (other is AstUserDefType && other.id == id)
}

// Second-Order Types
class AstArrayType(val type: AstType, val dim: Int) : AstType() {
    init {
        if (dim < 1) throw Exception()
    }

    //    override fun typeId(): String = type.typeId() + "[]".repeat(dim)
    override val typeId = "array"
    override fun match(other: AstType) =
        other is AstAnyType || other is AstNullType || (other is AstArrayType && type.match(other.type) && dim == other.dim)
    val deRef: AstType
        get() = when(dim) {
            1 -> type
            else -> AstArrayType(type, dim - 1)
        }
}

class AstFuncType(val retType: AstType, val paraTypes: List<AstType>) : AstType() {
    constructor(vararg type: AstType) : this(type[0], type.drop(1))

    override val typeId = "function"
    override fun match(other: AstType) =
        other is AstFuncType && retType.match(other.retType)
                && paraTypes.zip(other.paraTypes).all { p -> p.first.match(p.second) }

    fun invocable(args: List<AstType>): Boolean =
        paraTypes.size == args.size && paraTypes.zip(args).all { it.first.match(it.second) }
}

val NonVoidTypeContext.type: AstType
    get() = when {
        Int() != null -> AstIntegerType
        Bool() != null -> AstBooleanType
        String() != null -> AstStringType
        Identifier() != null -> AstUserDefType(Identifier().text)
        else -> throw Exception()
    }

val DeclarableTypeContext.type: AstType
    get() = when {
        nonVoidType() != null -> nonVoidType().type
        arrayType() != null -> arrayType().type
        else -> throw Exception()
    }

val ReturnableTypeContext.type: AstType
    get() = when {
        Void() != null -> AstUnitType
        nonVoidType() != null -> nonVoidType().type
        arrayType() != null -> arrayType().type
        else -> throw Exception()
    }

val ArrayTypeContext.type: AstType
    get() = AstArrayType(nonVoidType().type, total.size)

