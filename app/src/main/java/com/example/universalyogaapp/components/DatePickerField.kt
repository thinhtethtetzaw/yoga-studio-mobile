package com.example.universalyogaapp.components

import android.app.DatePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date"
) {
    val context = LocalContext.current
    
    // Parse the initial date
    val initialDate = try {
        LocalDate.parse(value)
    } catch (e: Exception) {
        LocalDate.now()
    }
    
    // Create date picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(selectedDate.format(DateTimeFormatter.ISO_DATE))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).apply {
        // Set theme for the date picker
        setOnShowListener {
            getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.rgb(103, 80, 164))
            getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.rgb(103, 80, 164))
        }
    }

    OutlinedTextField(
        value = formatDateWithDayOfWeek(value),
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            IconButton(
                onClick = { datePickerDialog.show() }
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        interactionSource = remember { MutableInteractionSource() }
    )
}

private fun formatDateWithDayOfWeek(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        "$dayOfWeek, $formattedDate"
    } catch (e: Exception) {
        dateString
    }
} 