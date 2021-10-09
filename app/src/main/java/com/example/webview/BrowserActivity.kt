package com.example.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import java.io.ByteArrayOutputStream
import java.io.IOException

class BrowserActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URL = "url"

        // TAG
        private const val TAG = "BrowserActivity"
        fun launch(context: Context, url: String) {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }

    private var mIsLight = true
    private var mIsInject: Boolean? = null
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        initView()
    }

    override fun onDestroy() {
        this.mWebView?.let {
            val viewParent = it.parent
            if (viewParent is ViewGroup) {
                viewParent.removeView(it)
                it.destroy()
            }
            this.mWebView = null
        }
        super.onDestroy()
    }

    private fun initView() {
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        toolBar.setTitle(R.string.app_name)
        setSupportActionBar(toolBar)
        // WebView
        intent.getStringExtra(EXTRA_URL)?.let {
            val webView = createWebView(it)
            linearLayout.addView(webView)
            this.mWebView = webView
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(url: String): WebView {
        val webView = WebView(this)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                mIsInject?.let {
                    injectJs(view, mIsLight)
                    mIsInject = false
                }
            }

            override fun onLoadResource(view: WebView, url: String?) {
                mIsInject?.let {
                    if (it) return
                    injectJs(view, mIsLight)
                    mIsInject = true
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                Log.i(TAG, "onProgressChanged -> newProgress = $newProgress")
            }
        }
        webView.loadUrl(url)
        return webView
    }

    private fun injectJs(webView: WebView, isDayTheme: Boolean) {
        val resId = if (isDayTheme) {
            R.raw.style_light
        } else {
            R.raw.style_night
        }
        val inputStream = resources.openRawResource(resId)
        var outputStream: ByteArrayOutputStream? = null
        val code: String?
        try {
            outputStream = ByteArrayOutputStream()
            //
            var len: Int
            val buffer = ByteArray(1024)
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
            code = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        webView.evaluateJavascript(
            "javascript:(function() {var parent = document.getElementsByTagName('head').item(0);var style = document.createElement('style');style.type = 'text/css';style.innerHTML = window.atob('$code');parent.appendChild(style)})();",
            null
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (R.id.light == itemId) {
            val isLight = true
            this.mWebView?.let {
                injectJs(it, isLight)
            }
            this.mIsLight = isLight
        } else if (R.id.night == itemId) {
            val isLight = false
            this.mWebView?.let {
                injectJs(it, isLight)
            }
            this.mIsLight = isLight
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            this.mWebView?.let {
                if (it.canGoBack()) {
                    it.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}