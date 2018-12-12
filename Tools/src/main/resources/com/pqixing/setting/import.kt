//需要导入的工程,使用+号分割
val include = ""
//导入该工程所有的依赖工程
val dpsInclude = ""
//导入指定路径的include.kt文件
val targetInclude = ""
//不需要导入的工程
val exclude = ""
//该工程所有依赖的子工程都不导入
val dpsExclude = ""

//强制依赖，当此配置出现，会忽略其余include属性值，只要给gradle动态赋值只用
val focusInclude = ""

//本地目录的根目录，用来查找本地目录，以及下载Clone
val codeRoot = ".."


//END
