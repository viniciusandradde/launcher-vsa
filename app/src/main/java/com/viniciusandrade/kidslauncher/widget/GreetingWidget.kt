package com.viniciusandrade.kidslauncher.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.viniciusandrade.kidslauncher.MainActivity
import com.viniciusandrade.kidslauncher.util.TimeGreeting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A "glanceable" home-screen widget built with Jetpack Glance (Compose-style API
 * that compiles down to RemoteViews). Shows a friendly greeting and the current
 * time; tapping it opens the launcher. Refreshed on the schedule declared in
 * res/xml/greeting_widget_info.xml.
 */
class GreetingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // provideGlance is called on each update; read the time here so every
        // refresh renders the current value.
        val now = Date()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

        provideContent {
            GlanceTheme {
                WidgetContent(greeting = "${TimeGreeting.forHour(hour)}!", time = time)
            }
        }
    }
}

@Composable
private fun WidgetContent(greeting: String, time: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = time,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = greeting,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
