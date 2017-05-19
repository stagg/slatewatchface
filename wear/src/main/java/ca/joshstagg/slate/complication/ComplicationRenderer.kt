package ca.joshstagg.slate.complication

internal interface ComplicationRenderer {

    fun render(render: Render) {}

    fun ambientRender(render: Render) {}
}