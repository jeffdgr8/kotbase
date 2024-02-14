package ui

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.*

fun tabFocus(focusManager: FocusManager, direction: FocusDirection): (KeyEvent) -> Boolean = {
    when {
        it.key == Key.Tab && it.type == KeyEventType.KeyDown -> {
            focusManager.moveFocus(direction)
            true
        }
        else -> false
    }
}

fun handleEnter(handler: () -> Unit): (KeyEvent) -> Boolean = {
    when {
        it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
            handler()
            true
        }
        else -> false
    }
}
