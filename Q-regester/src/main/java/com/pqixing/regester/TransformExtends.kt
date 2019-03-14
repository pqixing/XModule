package com.pqixing.regester

class TransformExtends {
    val f = mutableSetOf<String>()
    fun filters(filers: List<String>) {
        this.f.addAll(filers)
    }
}
