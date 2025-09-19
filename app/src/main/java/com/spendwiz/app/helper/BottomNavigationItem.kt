package com.spendwiz.app.helper

import androidx.compose.ui.graphics.vector.ImageVector

class BottomNavigationItem(
    val title :String ,
    val selectedIcon : ImageVector ,
    val unSelectedIcon : ImageVector ,
    val hasNews : Boolean = false ,
    val badgeCount : Int  ?= null
)