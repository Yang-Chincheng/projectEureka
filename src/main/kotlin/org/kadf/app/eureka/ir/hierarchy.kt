package org.kadf.app.eureka.ir

class Comment(val msg: String) {
    override fun toString(): String = "; $msg"
}

class Module

class Function(
    val parentModule: Module,
    id: String,
    type: Type
): Value(id, type) {
    override fun asOperand(): String = "${(type as FunctionType).retType} $id"
    override fun toString(): String {
        return "define ${asOperand()}()"
    }
    val block: MutableList<BasicBlock> = mutableListOf()
}

class BasicBlock(val parentFunction: Function) {
    val pred: MutableList<BasicBlock> = mutableListOf()
    val succ: MutableList<BasicBlock> = mutableListOf()
    val inst: MutableList<Instruction> = mutableListOf()
}



