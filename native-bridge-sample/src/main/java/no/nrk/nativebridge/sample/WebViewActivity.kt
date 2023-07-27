package no.nrk.nativebridge.sample

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import no.nrk.nativebridge.NativeBridgeWebView
import no.nrk.nativebridge.sample.topicdata.GaConfTopicData

class WebViewActivity : AppCompatActivity() {

    lateinit private var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getPreferences(android.content.Context.MODE_PRIVATE)

        val etUrl: EditText = findViewById(R.id.etUrl)
        val webview: NativeBridgeWebView = findViewById(R.id.webview)
        val btnSetUrl: Button = findViewById(R.id.btnSetUrl)

        val url = prefs.getString("url", "http://10.0.2.2:8080")
        etUrl.setText(url)

        webview.apply {
            connection.addHandler("gaConf", { _ : GaConfTopicData.In, connection ->
                    val gaConf = GaConfTopicData.Out("35009a79-1a05-49d7-b876-2b884d0f825b")
                    connection.send("gaConf", gaConf)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setWebContentsDebuggingEnabled(true)
            }

            webViewClient = WebViewClient()
            if (url != null) {
                loadUrl(url)
            }
        }

        btnSetUrl.setOnClickListener {
            val etUrl = etUrl.text.toString()
            prefs.edit().putString("url", etUrl).apply()

            webview.loadUrl(etUrl)
        }
    }
}
