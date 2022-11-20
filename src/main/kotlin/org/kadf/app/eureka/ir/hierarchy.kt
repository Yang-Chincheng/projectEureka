package org.kadf.app.eureka.ir

class Comment(val msg: String) {
    override fun toString(): String = "; $msg"
}

class Module

class Function(
    val parentModule: Module,
    id: String,
    val funcType: FunctionType,
    val argList: List<Value>
): Value(funcType, id), IUser {
    val block: MutableList<BasicBlock> = mutableListOf()
    init {
        argList.forEach { it.addUser(this) }
    }
    override fun toString(): String = "@$id"
}

class BasicBlock(
    val parentFunction: Function,
    val label: Label
) {
    val pred: MutableList<BasicBlock> = mutableListOf()
    val succ: MutableList<BasicBlock> = mutableListOf()
    val inst: MutableList<IInstruction> = mutableListOf()
}



