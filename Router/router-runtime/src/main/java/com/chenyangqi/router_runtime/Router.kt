package com.chenyangqi.router_runtime

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * @author : ChenYangQi
 * date   : 7/13/21 23:38
 * desc   :
 */
object Router {

    private const val TAG = "RouterTAG"

    // 编译期间生成的总映射表
    private const val GENERATED_MAPPING = "com.chenyangqi.router.mapping.generated.RouterMapping"

    // 存储所有映射表信息
    private val mapping: HashMap<String, String> = HashMap()

    fun init() {

        try {
            val clazz = Class.forName(GENERATED_MAPPING)
            val method = clazz.getMethod("get")
            val allMapping: Any? = method.invoke(null)
            Log.i(TAG, "---->" + allMapping.toString())
            val map = allMapping as Map<String, String>
            if (map.isNotEmpty()) {
                Log.i(TAG, "init: get all mapping:")
                map.onEach {
                    Log.i(TAG, "    ${it.key} -> ${it.value}")
                }
                mapping.putAll(map)
            }

        } catch (e: Throwable) {
            Log.e(TAG, "init: error while init router : $e")
            e.printStackTrace()
        }

    }

    fun go(context: Context, url: String) {

        if (context == null || url == null) {
            Log.i(TAG, "go: param error.")
            return
        }

        // 匹配URL，找到目标页面
        // router://imooc/profile?name=imooc&message=hello

        val uri = Uri.parse(url)
        val scheme = uri.scheme
        val host = uri.host
        val path = uri.path

        var targetActivityClass = ""

        mapping.onEach {
            val ruri = Uri.parse(it.key)
            val rscheme = ruri.scheme
            val rhost = ruri.host
            val rpath = ruri.path

            if (rscheme == scheme && rhost == host && rpath == path) {
                targetActivityClass = it.value
            }
        }

        if (targetActivityClass == "") {
            Log.e(TAG, "go:     no destination found.")
            return
        }

        // 解析URL里的参数，封装成为一个 Bundle

        val bundle = Bundle()
        val query = uri.query
        query?.let {
            if (it.length >= 3) {
                val args = it.split("&")
                args.onEach { arg ->
                    val splits = arg.split("=")
                    bundle.putString(splits[0], splits[1])
                }
            }
        }


        // 打开对应的Activity，并传入参数

        try {
            val activity = Class.forName(targetActivityClass)
            val intent = Intent(context, activity)
            intent.putExtras(bundle)
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.e(
                TAG, "go: error while start activity: $targetActivityClass, " +
                        "e = $e"
            )
        }
    }

}