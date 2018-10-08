package com.pqixing.tools

class Shell{


    public static void run(String cmd){
        def process = Runtime.getRuntime().exec(cmd)
        process.in.readLines()
        process.out
    }
}
