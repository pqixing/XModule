##  Gradle组件化gradle插件 -- modularization
>  为解决项目中工程依赖关系复杂，编译构建时间过长，模块无版本管理导致功能开发相互影响等问题，开发次开发，目的是解开工程间依赖，实现模块的版本化管理，快速进行模块调试构建

###  Android插件说明
> com.android.library 　　  ->  　　  com.module.library  　　　 依赖库插件
> com.android.application　　->　com.module.application 　      主工程插件

完整配置模型　
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
             //启动运行的Applike -> 理解为组件的Application
            applicationLike ="com.dachen.mdclogin.Applike"
            app_name = project.name
            app_icon
            app_theme
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