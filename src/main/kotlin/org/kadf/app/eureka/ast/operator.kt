package org.kadf.app.eureka.ast

enum class Operator(val symbol: String) {
    PRE_INC("++|"),
    PRE_DEC("--|"),
    POST_INC("|++"),
    POST_DEC("|--"),

    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),

    GE(">="),
    LE("<="),
    EQ("=="),
    NE("!="),
    GT(">"),
    LT("<"),

    NOT("~"),
    AND("&"),
    OR("|"),
    XOR("^"),
    LSH("<<"),
    RSH(">>"),

    LOGIC_NOT("!"),
    LOGIC_AND("&&"),
    LOGIC_OR("||")
}