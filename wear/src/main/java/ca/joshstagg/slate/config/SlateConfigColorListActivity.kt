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
class SlateConfigColorListActivity : Activity(), OnColorSelectedListener {

    private lateinit var configColor: ConfigColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate_color_list)
        configColor = intent.getParcelableExtra("ITEM")

        val layoutManager = LinearLayoutManager(this)
        layoutManager.recycleChildrenOnDetach = true

        val recyclerView: WearableRecyclerView = findViewById(R.id.config_color_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.isCircularScrollingGestureEnabled = true
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = SlateConfigColorListAdapter(configColor, this)
        Slate.instance.configService.connect()
    }

    override fun onColorSelected(color: String) {
        Slate.instance.configService.sharedPreferences.edit().putString(configColor.key, color).apply()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Slate.instance.configService.disconnect()
    }
}