package org.kadf.app.eureka.ir
//
sealed class IrValue(
    val type: IrType,
    val id: IrIdent = IrAnonymousIdent,
    val isAddress: Boolean = false
) {
    private val user: MutableList<IrUser> = mutableListOf()
    open fun addUser(vararg users: IrUser) { user.addAll(users) }
    override fun toString(): String = "$id"
    open val ir: String = "<undef>"
}

val IrValue.deRefType get() = type.deRef
val IrValue.asRefType get() = type.asRef

sealed interface IrUser

class IrParameter(id: IrVarIdent, type: IrType): IrValue(type, id)

class IrGlobalAssign(
    id: IrVarIdent,
    private val value: IrValue
): IrValue(value.asRefType, id) {
    override val ir: String
        get() = if (value.type is IrBoolType) "$id = global i8 $value"
            else "$id = global ${value.type} $value"
}

class IrConstAssign(
    id: IrVarIdent,
    private val value: IrValue
): IrValue(value.asRefType, id) {
    override val ir: String
        get() = if (value.type is IrBoolType) "$id = constant i8 $value"
        else "$id = constant ${value.type} $value"
}
