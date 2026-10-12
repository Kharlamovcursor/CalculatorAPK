package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.scale
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

val HdQuickUnits: List<String> = listOf("шт", "шт.", "м")

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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.9f else 1f,
        animationSpec = tween(90),
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.9f else 1f,
        animationSpec = tween(90),
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

@Composable
fun HdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
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
