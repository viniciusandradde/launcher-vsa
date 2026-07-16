package com.viniciusandrade.kidslauncher.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver that binds the [GreetingWidget] to the AppWidget framework.
 * Declared in the manifest with the APPWIDGET_UPDATE intent filter.
 */
class GreetingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GreetingWidget()
}
