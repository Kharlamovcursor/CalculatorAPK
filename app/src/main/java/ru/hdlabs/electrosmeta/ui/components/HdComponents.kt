package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.hdlabs.electrosmeta.ui.theme.HdBlack
import ru.hdlabs.electrosmeta.ui.theme.HdBlue
import ru.hdlabs.electrosmeta.ui.theme.HdOrange
import ru.hdlabs.electrosmeta.ui.theme.HdPanel
import ru.hdlabs.electrosmeta.ui.theme.HdPanelElevated
import ru.hdlabs.electrosmeta.ui.theme.HdTextPrimary
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary
import ru.hdlabs.electrosmeta.ui.motion.LocalHdMotion

val HdQuickUnits: List<String> = listOf("шт.", "м", "м.п.", "компл.")

@Composable
fun HdUnitChoiceRow(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Единица",
) {
    Text(label, color = HdTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdQuickUnits.forEach { unit ->
            val active = selected == unit
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(unit) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (active) HdBlue.copy(alpha = 0.35f) else HdPanel,
                ),
                border = BorderStroke(
                    1.dp,
                    if (active) HdOrange.copy(alpha = 0.8f) else HdTextSecondary.copy(alpha = 0.35f),
                ),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        unit,
                        color = if (active) HdTextPrimary else HdTextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun HdScreenRoot(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

/** Узкая колонка действий — удобнее дотягиваться большим пальцем на телефоне. */
fun Modifier.hdThumbActionWidth(): Modifier =
    fillMaxWidth().widthIn(max = 400.dp)

/**
 * Основной блок по вертикали по центру видимой области; при переполнении — прокрутка.
 * [footer] (например, версия) остаётся внизу экрана.
 */
@Composable
fun HdThumbFriendlyBody(
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    Modifier
                        .hdThumbActionWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    content()
                }
            }
        }
        footer?.invoke()
    }
}

@Composable
fun HdCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HdPanelElevated.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, HdBlue.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun HdPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val motion = LocalHdMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) motion.buttonPressScale else 1f,
        animationSpec = tween(motion.buttonPressDurationMs),
        label = "btnScale",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed && enabled) HdOrange.copy(alpha = 0.22f) else Color.Transparent,
            disabledContainerColor = HdPanel,
        ),
        border = if (pressed && enabled) {
            BorderStroke(2.5.dp, HdOrange)
        } else {
            BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(HdBlue, HdOrange)))
        },
    ) {
        Text(text, color = HdTextPrimary)
    }
}

@Composable
fun HdSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val motion = LocalHdMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) motion.buttonPressScale else 1f,
        animationSpec = tween(motion.buttonPressDurationMs),
        label = "btnScaleSec",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed && enabled) HdOrange.copy(alpha = 0.35f) else HdPanel,
        ),
        border = BorderStroke(
            if (pressed && enabled) 2.5.dp else 1.dp,
            if (pressed && enabled) HdOrange else HdBlue.copy(alpha = 0.4f),
        ),
    ) {
        Text(text, color = HdTextPrimary)
    }
}

enum class HdSavePhase { Idle, Saving, Saved }

@Composable
fun HdSaveButton(
    idleText: String,
    phase: HdSavePhase,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val motion = LocalHdMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when (phase) {
            HdSavePhase.Saved -> 1.05f
            HdSavePhase.Saving -> 0.98f
            HdSavePhase.Idle -> if (pressed) motion.buttonPressScale else 1f
        },
        animationSpec = if (phase == HdSavePhase.Saved) {
            spring(dampingRatio = 0.5f, stiffness = 420f)
        } else {
            tween(90)
        },
        label = "saveBtnScale",
    )
    val label = when (phase) {
        HdSavePhase.Saving -> "Сохранение…"
        HdSavePhase.Saved -> "Сохранено"
        HdSavePhase.Idle -> idleText
    }
    val container = when (phase) {
        HdSavePhase.Saved -> HdBlue.copy(alpha = 0.45f)
        HdSavePhase.Saving -> HdPanel
        HdSavePhase.Idle -> if (pressed) HdOrange.copy(alpha = 0.55f) else HdPanel
    }
    val border = when (phase) {
        HdSavePhase.Saved -> BorderStroke(3.dp, HdBlue)
        HdSavePhase.Saving -> BorderStroke(2.dp, HdTextSecondary.copy(alpha = 0.45f))
        HdSavePhase.Idle -> BorderStroke(
            if (pressed) 3.dp else 1.dp,
            if (pressed) HdOrange else HdBlue.copy(alpha = 0.4f),
        )
    }
    Button(
        onClick = onClick,
        enabled = phase != HdSavePhase.Saving,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            disabledContainerColor = container,
        ),
        border = border,
    ) {
        Text(label, color = HdTextPrimary)
    }
}

@Composable
fun HdDialogFieldColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .imePadding()
            .verticalScroll(rememberScrollState()),
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    scope.launch { bringIntoViewRequester.bringIntoView() }
                }
            },
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HdBlue,
            unfocusedBorderColor = HdTextSecondary.copy(alpha = 0.4f),
            focusedTextColor = HdTextPrimary,
            unfocusedTextColor = HdTextPrimary,
            focusedLabelColor = HdBlue,
            unfocusedLabelColor = HdTextSecondary,
            cursorColor = HdOrange,
            focusedContainerColor = HdBlack.copy(alpha = 0.5f),
            unfocusedContainerColor = HdBlack.copy(alpha = 0.35f),
        ),
    )
}
