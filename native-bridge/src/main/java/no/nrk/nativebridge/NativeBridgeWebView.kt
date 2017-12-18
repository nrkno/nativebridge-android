package no.nrk.nativebridge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

const val TAG = "NativeBridgeAndroid"
@SuppressLint("SetJavaScriptEnabled")
open class NativeBridgeWebView : WebView, JavascriptExecutor {

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        val connection = WebViewConnection(ObjectMapper().registerModule(KotlinModule())!!, this)
        settings?.javaScriptEnabled = true
        addJavascriptInterface(NativeBridgeAndroid(connection), "NativeBridgeAndroid")
    }

    private inner class NativeBridgeAndroid(val connection: WebViewConnection) {
        @JavascriptInterface
        fun send(json: String){
            try {
                val runnableCode = Runnable { connection.receive(json) }
                handler.post(runnableCode)
            } catch (e: Exception){
                Log.d(TAG, "Unable to handle incoming json. Ignoring.")
            }
        }
    }

    override fun executeJavascript(javascript: String){
        Log.d(TAG, "Passing $javascript to web view")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            evaluateJavascript(javascript, { /* no-op */ })
        } else {
            loadUrl(javascript)
        }
    }
}
