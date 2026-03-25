package com.remote.desktopclient

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val prefKey = "server_url"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultUrl = "https://www.mnfclub.com/game-windows.html"
        val url = prefs.getString(prefKey, defaultUrl)
        if (url.isNullOrEmpty()) {
            setContentView(R.layout.activity_setup)
            val input = findViewById<EditText>(R.id.input_url)
            val btn = findViewById<Button>(R.id.btn_save)
            btn.setOnClickListener {
                val u = input.text.toString().trim()
                if (u.isNotEmpty()) {
                    prefs.edit().putString(prefKey, u).apply()
                    loadWeb(u)
                }
            }
        } else {
            loadWeb(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWeb(url: String) {
        setContentView(R.layout.activity_main)
        val wv = findViewById<WebView>(R.id.webview)
        val progress = findViewById<View>(R.id.progress)
        val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 OPR/108.0.0.0"
        val s = wv.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.userAgentString = ua
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.javaScriptCanOpenWindowsAutomatically = true
        s.setSupportMultipleWindows(true)
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)
        wv.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                newWebView.settings.javaScriptEnabled = true
                newWebView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (!url.isNullOrEmpty()) {
                            wv.loadUrl(url)
                        }
                        newWebView.destroy()
                    }
                }
                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }
        val jsSpoof = "Object.defineProperty(navigator,'userAgent',{get:function(){return '$ua'}});Object.defineProperty(navigator,'platform',{get:function(){return 'Win32'}});Object.defineProperty(navigator,'maxTouchPoints',{get:function(){return 0}});Object.defineProperty(navigator,'webdriver',{get:function(){return false}});"
        val jsRuffle = "(()=>{var s=document.createElement('script');s.src='https://unpkg.com/@ruffle-rs/ruffle@latest/ruffle.js';s.crossOrigin='anonymous';document.head.appendChild(s);function run(){if(!window.RufflePlayer){setTimeout(run,500);return;}var r=window.RufflePlayer.newest();document.querySelectorAll('embed,object').forEach(function(el){var src=el.getAttribute('src')||el.getAttribute('data')||el.data;if(src&&/\\.swf(\\?|$)/i.test(src)){var p=r.createPlayer();p.style.width=el.getAttribute('width')?el.getAttribute('width')+'px':'100%';p.style.height=el.getAttribute('height')?el.getAttribute('height')+'px':'100%';el.parentNode.insertBefore(p,el);p.load(src);el.remove();}});}document.addEventListener('DOMContentLoaded',run);run();})();"
        wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
                wv.evaluateJavascript(jsSpoof, null)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progress.visibility = View.GONE
                wv.evaluateJavascript(jsRuffle, null)
            }
        }
        wv.loadUrl(url)
    }

    override fun onBackPressed() {
        val wv = findViewById<WebView?>(R.id.webview)
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
