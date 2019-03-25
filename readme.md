##  组件化管理系统
gradle插件
> com.module.android 　　

![ide插件](https://plugins.jetbrains.com/plugin/12121-modularization)


launch注解使用



### 零. 插件使用说明   
随着项目的庞大，组件化开发变的非常有必要，在业务节藕拆分之外，为了实现代码的拆分隔离，多git的代码更加方便管理，modularization插件应声而生,其中包含gradle插件，注解插件，intelij 搭配使用   
>  自动导入和管理开发代码   ![gif](imgs/importcode.gif)   
>  依赖方式在maven和local代码之间轻松切换。 模块独立运行调试更加简单  ![gif](imgs/dependent.gif)    
>  上传代码到Maven仓库   ![gif](imgs/tomaven.gif)     


###  一. Android插件说明
> com.android.library 　　  ->  　　  com.module.library  　　　 依赖库插件
> com.android.application　　->　com.module.application 　      主工程插件

#### 1. 完整配置模型　
> **note** 以下模板中的配置属性值，均为插件中设置的属性值，大部分实际使用中可以不进行配置，使用默认值即可

```
apply plugin :'com.module.library'　　　　
//apply plugin :'com.module.application'

//此插件属性配置区域
moduleConfig{

    //给插件使用的工程添加默认依赖  目前包含 dcuser,dcnet,dccommon,router,mvpbase五个基础组件
    addDefaultImpl = true

    //是否在同步前，更新一遍版本号,默认会从仓库爬取项目依赖库最新的版本号
    updateBeforeSync = true

    androidConfig{ //等同于Android官方的属性配置，以下为
            buildToolsVersion = '26.0.2'
            compileSdkVersion = '26'
            minSdkVersion = '16'
            targetSdkVersion = '21'
            versionCode = '1'　
            versionName = "1.0"
            applicationId = ""
            support_v4 = "26.1.0"
            support_v7 = "26.1.0"

            //兼容６.０以上　Appache框架
            compatAppache = true
            //开启ktolin支持
            kotlinEnable = true
            kotlin_version = "1.2.0"

            //启用渠道包选项(只支持application 插件) 默认实现了大辰常用的渠道设置
            flavorsEnable = false
    }
    //基础版本号,当指定环境版本号没有进行配置时,会读取此属性
    pom_version
    //通用上传配置,当指定环境没有配置是否可上传时,读取此属性
    uploadEnable = false

    //Maven环境配置,默认支持debug,test,release三个仓库
    mavenTypes {
           release{
                 //是否开启上传到仓库,只对library插件生效,不配置则读取通用配置
                 uploadEnable
                 //上传仓库版本号,没有默认值
                 pom_version
                 //仓库url地址, debug,test,release环境无须配置
                 maven_url
                 //上传仓库的名称,默认使用工程名,无须配置
                 artifactId
                 //上传仓库的group,无须配置,默认使用com.dachen.android
                 groupName
                 //仓库上传用户名,debug,test,release环境无须配置
                 userName
                 //仓库上传用密码,debug,test,release环境无须配置
                 password
                 //上传的key,release环境上传需要校验次key,其他环境不校验,默认不开放
                 uploadKey
                 //更新说明,test和release环境上传必须填写更新说明
                 updateDesc
           }
           test{     ...   }
           debug{  }
           other{    ...   }
    }
    mavenType = mavenTypes.test

    uploadEnable = true
    pom_version = "0.1"
    //注意事项 : 环境配置中的 pom_version 默认只配两位 如1.0; 实际上传版本号为 release : 1.0; test 1.0.(自动递增) debug 1.0.(时间戳)


    //此配置只针对library插件生效
    runTypes{
        test{
            //组件作为单独的App运行
            asApp = true
            //启动运行的页面
            launchActivity ="com.dachen.mdclogin.LoginActivity"
            app_name = project.name
            app_icon
            app_theme
            //是否在登录以后才跳转页面
            afterLogin = true
        }
        other{ ... }
    }
    runType =runTypes.test

    //内部工程依赖方式
    dependModules{
            //独立组件依赖,实际上是runtime时依赖,无法直接引用依赖库的内容
            add "mdclogin"
            //常规依赖,可以正常使用该依赖库中的内容
            addImpl "router"
            //常规依赖,同时自动去除该依赖包中对包含默认分组的依赖
            addExImpl "mvpbase"

            //以上三种依赖方式都可以进阶配置
            addXXX ("module"){
                //使用本地工程,而非仓库版本
                local = true
                //该module默认的group,默认无须配置
                group = ""
                //指定该依赖的版本号,默认无须配置,自动获取最新版本号
                version = ""
                //内部依赖 按照分组去除
                excludeGroup ("com.dachen.android","xxx.xxx.xx")

                //内部依赖 按照模块名去除
                excludeModule ("module1","module2")
            }
        }

}

//配置结束标记　　必须要配置，否则插件不会生效
endConfig()

```

#### 2. 进阶配置项
> 以上配置均为modularization插件配置项,如不满足要求或需要更多配置时,可基于原始配置,进行再配置

####  2.1 增加全局配置,作为每个module的基础配置
>  在工程的根目录的gradle.properties中,使用allProjectConfig属性配置全局配置文件的路径, 例如 : allProjectConfig = /home/user/allProjectConfig.gradle
```
/home/user/allProjectConfig.gradle
moduleConfig{//配置选项参照 完整配置模型　

}
```

>  **Note** 全局配置中,一定不要不要不要添加 **endConfig()** 结束标记,否则......................会报错的

#### 2.2 增加特殊配置(默认自动化构建使用) ,作为指定module的本地配置使用
>  在module工程目录下,新建 local.gradle 文件进行配置,配置选项参照 全局配置
> **Note** 三种配置级别重要性  local.gradle > build.gradle > allProjectConfig.gradle   　　　其中second.gradle文件会被插件默认添加到.gitignore文件中，只当做本地配置文件使用



#### 2.3 第三方插件补充配置　　
> 在modularization　配置项之外，如果需要对额外的选项进行配置　（如buildType 等）　可以　endConfig()之后，进行其他配置
```
apply plugin :'com.module.library'　　　　
//apply plugin :'com.module.application'
//此插件属性配置区域
moduleConfig{
    ....
}
endConfig()

//Android配置，配置方式参考Andriod官方,此出配置会默认覆盖moduleConfig中相同的Android属性
android{
    ....
}
//非内部依赖库,按照常规进行配置
dependencies {
    implementation "com.android.support:support-v4:$support-v4"
}

 //其他
 ...

```

#### 2.4 高级配置项
>  在工程目录中的gradle.properties 中,还可以进行少量隐藏配置
```
//强制只用本地工程,Y : 对配置的工程使用project方式依赖, 否则默认依赖仓库版本 (注意,依赖本地工程时,需要把工程目录导入Studio中)
focusLocal = Y
//是否添加默认实现 Y  默认给每个工程添加 router , dcnet,dcuser,mvpbase,dccommon 五个基础工程(这五个工程本身除外)  否则不自动添加(router工程始终添加)
addDefaultImpl = N
//隐藏配置,优先级最高,配置方式跟进阶配置一样,若配置此属性,同时触发生成批量上传工程的脚本
hiddenConfig = configPath
```


#### 3. 文件输出介绍
>  插件在使用过程中,会输出一些文件,默认保存在模块下.modularization 目录下

针对library或者Application模块,会包含以下文件
>
>  dependency.txt　　　 该模块的依赖关系输出
>  .cache 　　缓存文件输出目录
>  |-- java　　   组件单独运行时，自动生成的Java代码目录
>  |-- android.gradle    自动生成的gradle配置文件
>  |-- AndroidManifest.xml    自动生成组件单独运行时,需要的清单文件
>  |-- dependencies.gradle   通过moduleConfig进行依赖配置后,自动生成的配置文件
>  |-- kotlin.gradle      kotlin 支持文件
>  |-- sourceSets.gradle  组件运行的sourceSet配置

针对root工程,会生成以下文件
>
>  allModulePath.log　　　 自动生成当前所有工程以及对应的绝对地址映射文件
>  clone.bat/clone.sh 　　批量clone所有git的脚本
>  module.gradle　　      所有导入的模块列表
>  module_{env}.version        所有导入的模块列表在仓库中的版本号显示
>  plugin.version    当前使用modularization插件的版本号
>  print.log   打印的日志记录
>  setting.gradle     这个才是真正实现自动导入模块的配置文件


#### 4. Gradle命令介绍

























