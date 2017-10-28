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

    private lateinit var adapter : SlateConfigAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate_configuration)

        adapter = SlateConfigAdapter(Slate.instance.configService.configItems)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.recycleChildrenOnDetach = true

        val recyclerView: WearableRecyclerView = findViewById(R.id.config_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter
        Slate.instance.configService.connect()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        Slate.instance.configService.disconnect()
    }
}