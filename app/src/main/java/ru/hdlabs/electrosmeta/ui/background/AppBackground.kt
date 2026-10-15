package ru.hdlabs.electrosmeta.ui.background

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.hdlabs.electrosmeta.ElectrosmetaApplication
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.data.settings.BackgroundTheme

@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )
    when (settings.backgroundTheme) {
        BackgroundTheme.Space -> SpaceBackground(modifier)
        BackgroundTheme.Spring -> SpringBackground(modifier)
        BackgroundTheme.Summer -> SummerBackground(modifier)
        BackgroundTheme.Autumn -> AutumnBackground(modifier)
        BackgroundTheme.Winter -> WinterBackground(modifier)
    }
}
