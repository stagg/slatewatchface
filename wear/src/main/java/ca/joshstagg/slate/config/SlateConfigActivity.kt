package ca.joshstagg.slate.config

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import ca.joshstagg.slate.R
import ca.joshstagg.slate.Slate
import ca.joshstagg.slate.config.items.ConfigItem


/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class SlateConfigActivity : Activity() {

    private var items: List<ConfigItem<*>> = emptyList()
        set(value) {
            field = value
            val adapter = SlateConfigAdapter(items)
            recyclerView.swapAdapter(adapter, false)
            adapter.notifyDataSetChanged()
        }

    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var recyclerView: WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate_configuration)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.recycleChildrenOnDetach = true

        recyclerView = findViewById(R.id.config_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = SlateConfigAdapter(emptyList())

        handlerThread = HandlerThread("SlateConfigActivity")
        handlerThread.start()
        handler = Handler(handlerThread.looper) {
            val configItems = Slate.instance.configService.configItems
            Handler(Looper.getMainLooper()).post {
                items = configItems
            }
            true
        }
        Slate.instance.configService.connect()
    }

    override fun onResume() {
        super.onResume()
        handler.obtainMessage().sendToTarget()
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quit()
        Slate.instance.configService.disconnect()
    }
}