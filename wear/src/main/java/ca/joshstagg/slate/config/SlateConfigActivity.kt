package ca.joshstagg.slate.config

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import ca.joshstagg.slate.R

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class SlateConfigActivity : Activity() {

    private lateinit var recyclerView : WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slate_configuration)

        recyclerView = findViewById(R.id.config_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isCircularScrollingGestureEnabled = true
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.setHasFixedSize(true)
    }
}