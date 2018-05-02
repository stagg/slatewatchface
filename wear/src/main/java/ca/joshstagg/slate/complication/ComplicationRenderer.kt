package ca.joshstagg.slate.complication

import ca.joshstagg.slate.Ambient

internal interface ComplicationRenderer {

    fun render(render: Render) {}

    fun ambientRender(ambient: Ambient, render: Render) {}
}