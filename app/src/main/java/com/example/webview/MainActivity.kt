package com.example.webview

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        toolBar.setTitle(R.string.app_name)
        setSupportActionBar(toolBar)
        //
        val listView = findViewById<ListView>(R.id.listView)
        val data = arrayOf("https://m.iqiyi.com", "https://m.bilibili.com", "https://www.baidu.com", "https://www.hao123.com")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            BrowserActivity.launch(this, data[position])
        }
    }

}