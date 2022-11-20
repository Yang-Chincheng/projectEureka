package org.kadf.app.eureka.ir

sealed interface Instruction

sealed interface ITerminator: Instruction

sealed interface IBinary: Instruction

sealed interface IUnary: Instruction

sealed interface IBitwiseBinary: Instruction

sealed interface IMemory: Instruction

sealed interface IAggregate: Instruction
