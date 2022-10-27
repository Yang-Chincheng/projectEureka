package org.kadf.app.eureka.ast

import org.kadf.app.eureka.MxStarParser.ArrayTypeContext
import org.kadf.app.eureka.MxStarParser.DeclarableTypeContext
import org.kadf.app.eureka.MxStarParser.NonVoidTypeContext
import org.kadf.app.eureka.MxStarParser.ReturnableTypeContext

sealed interface IType {
    fun typeId(): String
    fun regiId(): String
    fun match(other: IType): Boolean

}

sealed interface INonVoidType : IType, IDeclarableType, IReturnableType
sealed interface IDeclarableType : IType
sealed interface IReturnableType : IType
sealed interface INewableType : IType

object AnyType: IType {
    override fun typeId() = "any"
    override fun regiId() = "any"
    override fun match(other: IType) = true
}

object NothingType: IType {
    override fun typeId() = "nothing"
    override fun regiId() = "nothing"
    override fun match(other: IType) = false
}

// First-Order Types
object UnitType : IReturnableType {
    override fun typeId() = "void"
    override fun regiId() = "unit"
    override fun match(other: IType) = other is UnitType
}

object NullType : IType {
    override fun typeId() = "null"
    override fun regiId() = "null"
    override fun match(other: IType) =
        other is NullType || other is ArrayType || other is UserDefinedType || other is AnyType
}

object IntegerType : INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "int"
    override fun regiId() = "integer"
    override fun match(other: IType) = other is IntegerType || other is AnyType
}

object BooleanType : INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "bool"
    override fun regiId() = "boolean"
    override fun match(other: IType) = other is BooleanType || other is AnyType
}

object StringType : INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "string"
    override fun regiId() = "string"
    override fun match(other: IType) = other is StringType || other is AnyType
}

class UserDefinedType(private val id: String) :
    INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = id
    override fun regiId() = "def-$id"
    override fun match(other: IType) =
        (other is UserDefinedType && other.id == id) || other is NullType || other is AnyType
}

// Second-Order Types
class ArrayType(val type: INonVoidType, val dim: Int) :
    IDeclarableType, IReturnableType {
    init {
        if (dim < 1) throw Exception()
    }
    override fun typeId(): String = type.typeId() + "[]".repeat(dim)
    override fun regiId() = "array"
    override fun match(other: IType) =
        (other is ArrayType && type.match(other.type) && dim == other.dim) || other is NullType || other is AnyType
}

class FunctionType(val retType: IReturnableType, val paraTypes: List<IDeclarableType>): IType {
    override fun typeId(): String {
        val paraId = paraTypes.map { it.typeId() }.reduce { acc, t -> "$acc, $t" }
        return "($paraId) -> ${retType.typeId()}"
    }
    override fun regiId() = "function"
    override fun match(other: IType) =
        other is FunctionType && retType.match(other.retType)
                && paraTypes.zip(other.paraTypes).all { p -> p.first.match(p.second) }
}

val NonVoidTypeContext.type: INonVoidType
    get() = when {
        Int() != null -> IntegerType
        Bool() != null -> BooleanType
        String() != null -> StringType
        Identifier() != null -> UserDefinedType(Identifier().text)
        else -> throw Exception()
    }

val DeclarableTypeContext.type: IDeclarableType
    get() = when {
        nonVoidType() != null -> nonVoidType().type
        arrayType() != null -> arrayType().type
        else -> throw Exception()
    }

val ReturnableTypeContext.type: IReturnableType
    get() = when {
        Void() != null -> UnitType
        nonVoidType() != null -> nonVoidType().type
        arrayType() != null -> arrayType().type
        else -> throw Exception()
    }

val ArrayTypeContext.type: ArrayType
    get() = ArrayType(nonVoidType().type, total.size)




