package org.kadf.app.eureka.ir

sealed class Value(
    val type: Type,
    val id: String? = null,
    private val userList: MutableList<IUser> = mutableListOf()
) {
    open fun addUser(user: IUser) { userList.add(user) }
    override fun toString(): String = "$id"
    open val ir: String = "<undef>"
}

class SymbolTable {
    private val addrTable: HashMap<String, Value> = hashMapOf()
    private val cntTable: HashMap<String, Int> = hashMapOf()

    fun clear() {
        addrTable.clear()
        cntTable.clear()
    }

    fun rename(id: String): String {
        val cnt = cntTable[id] ?: 0
        return "$id.${cnt}".also { cntTable[id] = cnt + 1 }
    }

    fun getAddress(id: String): Value? = addrTable[id]
    fun putAddress(id: String, value: Value) { addrTable[id] = value }

}

sealed interface IUser
