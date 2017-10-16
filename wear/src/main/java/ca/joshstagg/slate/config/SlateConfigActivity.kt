package ca.joshstagg.slate.config

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import ca.joshstagg.slate.R
import ca.joshstagg.slate.Slate

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class SlateConfigActivity : Activity() {

    private lateinit var recyclerView : WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate_configuration)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.recycleChildrenOnDetach = true

        recyclerView = findViewById(R.id.config_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.isCircularScrollingGestureEnabled = true
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = SlateConfigAdapter(Slate.instance.configService.configItems)
        Slate.instance.configService.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Slate.instance.configService.disconnect()
    }
}