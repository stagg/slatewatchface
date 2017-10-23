package ca.joshstagg.slate.config

import android.os.Parcel
import android.os.Parcelable


/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigColor(key: String, title: String, default: String,
                  val defaultText: String,
                  val colorNames: Array<String>,
                  val colorValues: Array<String>)
    : ConfigItem<String>(key, title, default), Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.createStringArray(),
            source.createStringArray()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(key)
        writeString(title)
        writeString(default)
        writeString(defaultText)
        writeStringArray(colorNames)
        writeStringArray(colorValues)
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ConfigColor> = object : Parcelable.Creator<ConfigColor> {
            override fun createFromParcel(source: Parcel): ConfigColor = ConfigColor(source)
            override fun newArray(size: Int): Array<ConfigColor?> = arrayOfNulls(size)
        }
    }
}