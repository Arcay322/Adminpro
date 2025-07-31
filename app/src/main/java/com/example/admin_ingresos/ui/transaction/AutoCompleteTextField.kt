package com.example.admin_ingresos.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit = {},
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    val focusManager = LocalFocusManager.current
    
    // Update textFieldValue when value changes externally
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(value, selection = textFieldValue.selection)
        }
    }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = { /* Don't change expanded state on click */ }
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                    expanded = newValue.text.isNotEmpty() && suggestions.isNotEmpty()
                },
                label = label,
                placeholder = placeholder,
                isError = isError,
                supportingText = supportingText,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = false)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && textFieldValue.text.isNotEmpty()) {
                            expanded = suggestions.isNotEmpty()
                        } else if (!focusState.isFocused) {
                            expanded = false
                        }
                    }
            )
            
            if (expanded && suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = true,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    suggestions.take(5).forEach { suggestion ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            onClick = {
                                textFieldValue = TextFieldValue(suggestion)
                                onValueChange(suggestion)
                                onSuggestionSelected(suggestion)
                                expanded = false
                                focusManager.clearFocus()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}