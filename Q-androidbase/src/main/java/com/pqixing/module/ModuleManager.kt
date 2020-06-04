package com.pqixing.module

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.util.Log
import com.pqixing.annotation.AnnotationInfo
import com.pqixing.annotation.RunModule
import com.pqixing.module.impl.SimpleLife
import java.util.*

object ModuleManager : SimpleLife() {
    var startCount = -1
    private var modules = HashMap<String, IModule>()
    private lateinit var mainThread: Handler
    private lateinit var app: Application

    fun attach(application: Application) {
        this.app = application
        mainThread = Handler()
        this.app.registerActivityLifecycleCallbacks(this)
        initClassModule()
    }

    fun runOnUiThread(delay: Long, cmd: Runnable) {
        mainThread.postDelayed(cmd, delay)
    }


    fun dispatch(type: Int, url: String, param: Any? = null) = forAll { if (it.onDispatch(type, url, param)) return@forAll }

    fun forAll(block: (m: IModule) -> Unit) = modules.values.forEach(block)
    fun findModule(name: String) = modules[name]


    fun getApplicaion() = app

    private fun classForName(className: String): Class<*>? = AnnotationInfo.forName(className)
    override fun onActivityStopped(activity: Activity) {
        if (--startCount == 0) modules.values.onEach { it.onBackGroup() }
    }

    override fun onActivityStarted(activity: Activity) {
        if (++startCount == 0) startCount = 1//应用刚启动
        else if (startCount == 1) {//只有一个页面，重新切换回来
            modules.values.onEach { it.onForceGroup() }
        }
    }


    private fun initClassModule() {

        val moduleClass = AnnotationInfo.runModules.mapNotNull { classForName(it) }
        AnnotationInfo.runModules.clear()
        val modules = mutableMapOf<String, IModule>()
        for (clazz in moduleClass) {
            val name = clazz.getAnnotation(RunModule::class.java)?.name
            if (name?.isNotEmpty() != true) {
                Log.i("Router", "class ${clazz.name} lose name")
                continue
            }
            val instance = clazz.newInstance() as? IModule
            if (instance == null) {
                Log.i("Router", "class ${clazz.name} can not be cast to IModule")
                continue
            }
            modules[name] = instance
        }
        loadModules(modules)
    }


    fun loadModules(m: Map<String, IModule>) {
        startCount = -1
        modules.clear()
        modules.putAll(m)

        val app = getApplicaion()
        if (app is IModule) {
            modules[app.packageName] = app
        }
        modules.values
                .onEach { i -> mainThread.post { i.onCreateOnUi() } }
                .onEach { it.onCreateOnThread() }
    }
}