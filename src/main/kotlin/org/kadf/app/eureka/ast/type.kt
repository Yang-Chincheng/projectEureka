package org.kadf.app.eureka.ast

sealed interface IType {
    fun typeId(): String
}

sealed interface INonVoidType : IType
sealed interface IDeclarableType : IType
sealed interface IReturnableType : IType
sealed interface INewableType : IType

sealed class Type

object VoidType : Type(), IReturnableType {
    override fun typeId() = "void"
}

object IntegerType : Type(), INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "int"
}

object BooleanType : Type(), INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "bool"
}

object StringType : Type(), INonVoidType, IDeclarableType, IReturnableType {
    override fun typeId() = "string"
}

class UserDefinedType(private val id: String) :
    Type(), INonVoidType, IDeclarableType, IReturnableType, INewableType {
    override fun typeId() = id
}


class ArrayType(val id: INonVoidType, val dim: Int, val scales: List<Int>) :
    Type(), IDeclarableType, IReturnableType, INewableType {
    init {
        if (scales.size > dim) throw Exception("too much scale information")
    }

    override fun typeId(): String {
        return scales.fold(id.typeId()) { acc, sz -> "$acc[$sz]" } + "[]".repeat(dim - scales.size)
    }
}
