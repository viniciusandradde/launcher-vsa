package com.viniciusandrade.kidslauncher.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.viniciusandrade.kidslauncher.MainActivity
import com.viniciusandrade.kidslauncher.util.TimeGreeting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Cores fixas (mesma paleta "Pequeno" do app). Evitamos GlanceTheme/glance-material3
// para manter o widget com dependências mínimas e API estável.
private val WidgetBackground = Color(0xFFFF7043)
private val WidgetOnBackground = Color(0xFFFFFFFF)

/**
 * Widget "glanceable" da tela inicial feito com Jetpack Glance (API estilo Compose
 * que vira RemoteViews). Mostra saudação + hora; ao tocar, abre o launcher.
 * Atualizado no período declarado em res/xml/greeting_widget_info.xml.
 */
class GreetingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // provideGlance roda a cada atualização; lemos a hora aqui para cada
        // refresh renderizar o valor atual.
        val now = Date()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

        provideContent {
            WidgetContent(greeting = "${TimeGreeting.forHour(hour)}!", time = time)
        }
    }
}

@Composable
private fun WidgetContent(greeting: String, time: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBackground))
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = time,
            style = TextStyle(
                color = ColorProvider(WidgetOnBackground),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = greeting,
            style = TextStyle(
                color = ColorProvider(WidgetOnBackground),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
