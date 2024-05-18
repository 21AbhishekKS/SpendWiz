package com.abhi.expencetracker.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun TransparentTextField (
    text:String,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    isSingleLine: Boolean = false,
    onFocusChange: (FocusState) ->  Unit
){

    val annotatedString = AnnotatedString(hint)

    Box(modifier = modifier){
        BasicTextField(value = text ,
            onValueChange = onValueChange,
            singleLine = isSingleLine,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it) },
            textStyle = textStyle

        )
        if (isHintVisible){
            Text(text = annotatedString.toString() , style = textStyle
                )
        }
    }
}