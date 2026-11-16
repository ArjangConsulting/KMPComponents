package io.github.maniramezan.kmpcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

/** Direction in which an [ExpandableFAB] reveals its actions. Start and end follow layout direction. */
public enum class FABExpansionDirection {
    UP,
    DOWN,
    START,
    END,
}

/**
 * An icon and caller-supplied [label] for an [ExpandableFAB] action.
 *
 * [id] must be unique within the menu and stable across updates. [label] must not be blank: it is
 * the action's accessible name even when labels are hidden, and the [icon] is decorative. This is a
 * plain class rather than a data class so properties can be added without breaking binary
 * compatibility.
 *
 * @throws IllegalArgumentException if [label] is blank.
 */
@Immutable
public class FABAction(
    public val id: String,
    public val label: String,
    public val icon: Painter,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
) {
    init {
        require(label.isNotBlank()) { "FABAction label must not be blank" }
    }
}

/**
 * A floating action button that reveals icon actions along one axis.
 *
 * Hoist [expanded] and apply [onExpandedChange] to accept toggle and dismissal requests. Selecting
 * an enabled action requests collapse before invoking its callback when [collapseOnAction] is
 * `true`. An empty [actions] list leaves a plain toggle FAB that reports no expanded or collapsed
 * state. The caller handles outside taps and Back if dismissal is wanted.
 *
 * Set [showActionLabels] to `false` for icon-only actions; [FABAction.label] stays their accessible
 * name. A non-blank [triggerLabel] shows an extended trigger with visible text; otherwise the
 * trigger is icon-only and named by [contentDescription].
 *
 * Place it in an aligned container, such as a `Scaffold`'s `floatingActionButton` slot, so the
 * trigger stays anchored as the menu changes size. [FABExpansionDirection.UP] and
 * [FABExpansionDirection.START] place the actions before the trigger; `DOWN` and `END` after it.
 * When space is short the actions scroll along the expansion axis, starting at the end nearest the
 * trigger. In a parent with unbounded space along that axis, such as a scrolling container, the
 * menu takes its full size.
 *
 * Actions fade and grow over `MaterialTheme.kmpMotion.standardMillis`, and appear and disappear at
 * once when the system reduces motion. Exiting actions immediately stop taking input and leave the
 * accessibility tree.
 *
 * @param contentDescription localized name of the icon-only trigger.
 * @param expandedStateDescription localized state announced on the trigger while expanded.
 * @param collapsedStateDescription localized state announced on the trigger while collapsed.
 * @param showActionLabels whether action labels are visible. Hidden labels remain accessible.
 * @param triggerLabel localized visible trigger title; `null` or blank keeps the trigger icon-only.
 * @throws IllegalArgumentException if two [actions] share an [FABAction.id].
 */
@Composable
public fun ExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    actions: List<FABAction>,
    icon: Painter,
    contentDescription: String,
    expandedStateDescription: String,
    collapsedStateDescription: String,
    modifier: Modifier = Modifier,
    direction: FABExpansionDirection = FABExpansionDirection.UP,
    collapseOnAction: Boolean = true,
    showActionLabels: Boolean = true,
    triggerLabel: String? = null,
) {
    require(hasUniqueFABActionIds(actions)) { "FABAction ids must be unique" }
    val horizontal = direction.isHorizontal
    val actionsFirst = direction.placesActionsFirst
    val hasActions = actions.isNotEmpty()
    val measurePolicy = remember(horizontal, actionsFirst) { FABMeasurePolicy(horizontal, actionsFirst) }
    Layout(
        content = {
            Box {
                FABActionMenu(
                    expanded = expanded,
                    actions = actions,
                    horizontal = horizontal,
                    actionsFirst = actionsFirst,
                    showActionLabels = showActionLabels,
                    onAction = { action ->
                        if (collapseOnAction) onExpandedChange(false)
                        action.onClick()
                    },
                )
            }
            Box {
                val triggerModifier = Modifier.semantics {
                    if (hasActions) {
                        stateDescription = if (expanded) expandedStateDescription else collapsedStateDescription
                    }
                }
                if (triggerLabel.isNullOrBlank()) {
                    FAB(
                        icon = icon,
                        contentDescription = contentDescription,
                        onClick = { onExpandedChange(!expanded) },
                        modifier = triggerModifier,
                    )
                } else {
                    ThemeExtendedFloatingActionButton(
                        text = triggerLabel,
                        icon = icon,
                        onClick = { onExpandedChange(!expanded) },
                        modifier = triggerModifier,
                    )
                }
            }
        },
        modifier = modifier,
        measurePolicy = measurePolicy,
    )
}

/** Whether no two of [actions] share an id. */
internal fun hasUniqueFABActionIds(actions: List<FABAction>): Boolean {
    val seen = HashSet<String>(actions.size)
    return actions.all { seen.add(it.id) }
}

/** Whether the menu opens along the horizontal axis. */
internal val FABExpansionDirection.isHorizontal: Boolean
    get() = this == FABExpansionDirection.START || this == FABExpansionDirection.END

/** Whether the actions sit before the trigger (above it, or at its start). */
internal val FABExpansionDirection.placesActionsFirst: Boolean
    get() = this == FABExpansionDirection.UP || this == FABExpansionDirection.START

/**
 * Measures the trigger first so it is never squeezed out, then gives the menu the remaining space
 * along the expansion axis. When that axis is unbounded the menu is limited to its own intrinsic
 * size, which keeps its scroll container from receiving infinite constraints.
 */
private class FABMeasurePolicy(private val horizontal: Boolean, private val actionsFirst: Boolean) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val menu = measurables[0]
        val trigger = measurables[1]
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val triggerPlaceable = trigger.measure(loose)
        val menuConstraints = if (horizontal) {
            val available = if (loose.hasBoundedWidth) {
                loose.maxWidth - triggerPlaceable.width
            } else {
                menu.maxIntrinsicWidth(loose.maxHeight)
            }
            loose.copy(maxWidth = available.coerceAtLeast(0))
        } else {
            val available = if (loose.hasBoundedHeight) {
                loose.maxHeight - triggerPlaceable.height
            } else {
                menu.maxIntrinsicHeight(loose.maxWidth)
            }
            loose.copy(maxHeight = available.coerceAtLeast(0))
        }
        val menuPlaceable = menu.measure(menuConstraints)
        val width = if (horizontal) {
            triggerPlaceable.width + menuPlaceable.width
        } else {
            maxOf(triggerPlaceable.width, menuPlaceable.width)
        }
        val height = if (horizontal) {
            maxOf(triggerPlaceable.height, menuPlaceable.height)
        } else {
            triggerPlaceable.height + menuPlaceable.height
        }
        val layoutWidth = constraints.constrainWidth(width)
        val layoutHeight = constraints.constrainHeight(height)
        return layout(layoutWidth, layoutHeight) {
            if (horizontal) {
                val menuX = if (actionsFirst) 0 else triggerPlaceable.width
                val triggerX = if (actionsFirst) menuPlaceable.width else 0
                menuPlaceable.placeRelative(menuX, layoutHeight - menuPlaceable.height)
                triggerPlaceable.placeRelative(triggerX, layoutHeight - triggerPlaceable.height)
            } else {
                val menuY = if (actionsFirst) 0 else triggerPlaceable.height
                val triggerY = if (actionsFirst) menuPlaceable.height else 0
                menuPlaceable.placeRelative(layoutWidth - menuPlaceable.width, menuY)
                triggerPlaceable.placeRelative(layoutWidth - triggerPlaceable.width, triggerY)
            }
        }
    }
}

@Composable
private fun FABActionMenu(
    expanded: Boolean,
    actions: List<FABAction>,
    horizontal: Boolean,
    actionsFirst: Boolean,
    showActionLabels: Boolean,
    onAction: (FABAction) -> Unit,
) {
    val animate = !rememberReduceMotionEnabled()
    val duration = MaterialTheme.kmpMotion.standardMillis
    val horizontalAlignment = if (actionsFirst) Alignment.End else Alignment.Start
    val verticalAlignment = if (actionsFirst) Alignment.Bottom else Alignment.Top
    val enter = when {
        !animate -> EnterTransition.None
        horizontal -> expandHorizontally(tween(duration), expandFrom = horizontalAlignment) + fadeIn(tween(duration))
        else -> expandVertically(tween(duration), expandFrom = verticalAlignment) + fadeIn(tween(duration))
    }
    val exit = when {
        !animate -> ExitTransition.None

        horizontal -> shrinkHorizontally(tween(duration), shrinkTowards = horizontalAlignment) +
            fadeOut(tween(duration))

        else -> shrinkVertically(tween(duration), shrinkTowards = verticalAlignment) + fadeOut(tween(duration))
    }
    AnimatedVisibility(visible = expanded && actions.isNotEmpty(), enter = enter, exit = exit) {
        val gap = MaterialTheme.kmpSpacing.small
        val scrollState = rememberScrollState()
        LaunchedEffect(actionsFirst, scrollState.maxValue) {
            if (actionsFirst) scrollState.scrollTo(scrollState.maxValue)
        }
        // While the menu animates out it stays composed, so stop it taking input or being read.
        val interaction = if (expanded) Modifier else Modifier.clearAndSetSemantics { }.consumeAllPointerInput()
        val content: @Composable () -> Unit = {
            actions.forEach { action ->
                key(action.id) {
                    FABActionButton(action = action, showLabel = showActionLabels, onClick = { onAction(action) })
                }
            }
        }
        if (horizontal) {
            Row(
                modifier = interaction
                    .padding(if (actionsFirst) PaddingValues(end = gap) else PaddingValues(start = gap))
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) { content() }
        } else {
            Column(
                modifier = interaction
                    .padding(if (actionsFirst) PaddingValues(bottom = gap) else PaddingValues(top = gap))
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(gap),
                horizontalAlignment = Alignment.End,
            ) { content() }
        }
    }
}

private fun Modifier.consumeAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

@Composable
private fun FABActionButton(action: FABAction, showLabel: Boolean, onClick: () -> Unit) {
    val spacing = MaterialTheme.kmpSpacing
    FilledTonalButton(
        onClick = onClick,
        enabled = action.enabled,
        modifier = Modifier
            .keyboardFocusOutline(CircleShape)
            .sizeIn(minWidth = minimumTouchTargetSize(), minHeight = minimumTouchTargetSize())
            .semantics { if (!showLabel) contentDescription = action.label },
        contentPadding = PaddingValues(
            horizontal = if (showLabel) spacing.large else spacing.small,
            vertical = spacing.small,
        ),
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Decorative: the button is named by the visible label or its content description.
            Icon(painter = action.icon, contentDescription = null, modifier = Modifier.size(glyphSize()))
            if (showLabel) Text(text = action.label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
