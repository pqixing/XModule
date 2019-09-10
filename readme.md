##  Modularization
#### 介绍
Modularization 是一个实现组件化管理系统,实现像开发一个独立应用一样开发组件


### 支持功能
* 组件代码按需导入和自动下载管理
* 一键快速发布组件到指定仓库,其他协作者无感知即可快速集成最新组件
* 实现仓库组件和本地代码依赖的无缝切换,方便组件间协调开发
* 支持所有组件工程多git工程批量进行git操作,包含checkout, merge, branch等操作
* 组件无需修改任何配置,通过插件直接快速运行. 既是模块也是app类型
* 组件支持分离api模块层,支持只对外暴露api层,业务实现层完全封闭, 从框架层彻底隔离组件,防止相互交叉引用
  
  
### 快速使用集成
* 在AndroidStudio -> Settings-> Plugins 搜索 Modularization  ,[安装ide插件](https://plugins.jetbrains.com/plugin/12121-modularization)
* 在工程root目录的build.gradle中,添加一下代码,
```
buildscript {
    ext {
        kotlin_version = "1.3.11"
    }
    repositories {
        maven {
            url "https://dl.bintray.com/pqixing86/modularization"
        }
        mavenLocal()
        google()
        maven {
            url "http://maven.aliyun.com/nexus/content/groups/public"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath 'com.pqixing.gradle:modularization:1.2.1'
    }
}

apply plugin : "com.module.manager"
manager {
    docRepoUrl = "https://github.com/pqixing/md_demo_repo.git" //通用管理配置文件存放git地址
    docRepoUser "empty"//docGit的用户，如不填，取projectInfo文件中的配置
    docRepoPsw "empty"//docGit的密码，如不填，取projectInfo文件中的配置
}
```
> 同步一下,根目录下会生成Config.java文件,需配置git用户名和密码,其他配置参数见注释
```
public class Config {
    /**
     * 用户名称,可用于git
     */
    public String userName = "pengqixing";

    /**
     * 用户密码,可用于git
     */
    public String passWord = "pengqixing";
}
```
> 详细了解更多功能实现,请查看[集成Demo文档](https://github.com/pqixing/modularization_demo)

### 开发过程操作说明 

