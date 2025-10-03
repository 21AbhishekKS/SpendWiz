package com.spendwiz.app.AppStyle

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.spendwiz.app.R

// In a file like ui/theme/MyTheme.kt or ui/components/MySwitch.kt

object AppColors {
    @Composable
    fun customSwitchColors(): SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = colorResource(id = R.color.toggle_thumb),
        checkedTrackColor = colorResource(id = R.color.toggle_track),
        uncheckedThumbColor = colorResource(id = R.color.unchecked_thumb),
    )

    @Composable
    fun customCardColors() : CardColors = CardDefaults.cardColors(
        containerColor = colorResource(id = R.color.my_disabled_card_container),
        contentColor = colorResource(id = R.color.my_card_content),
        disabledContainerColor = colorResource(id = R.color.my_disabled_card_container),
        disabledContentColor = colorResource(id = R.color.my_disabled_card_content)
    )
    @Composable
    fun customButtonColors() : ButtonColors = ButtonDefaults.buttonColors(
        containerColor = colorResource(id = R.color.button_color),
        contentColor = colorResource(id = R.color.button_text_color),
    )

}