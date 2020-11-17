package com.pqixing.model

class BrOpts(val opts: String = "") {
    var target: String? = null
    var brs: List<String> = emptyList()

    init {
        val split = opts.split("&")
        if (split.size > 1) {
            target = split[0]
        }
        brs = split[1].split(",")
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (target != null) sb.append(target).append("&")
        sb.append(brs.joinToString(","))
        return sb.toString()
    }
}