package com.abhi.expencetracker.Screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abhi.expencetracker.helper.BottomNavigationItem
import com.abhi.expencetracker.navigation.Routes

val items = listOf(
    BottomNavigationItem(
        "Home",
        Icons.Filled.Home ,
        Icons.Outlined.Home ,
        hasNews = false,
        badgeCount = null
    ),
    BottomNavigationItem(
        "Trans",
        Icons.Filled.ExitToApp ,
        Icons.Outlined.ExitToApp ,
        hasNews = false,
        badgeCount = null
    ),
    BottomNavigationItem(
        "Add",
        Icons.Filled.AddCircle ,
        Icons.Outlined.AddCircle ,
        hasNews = false,
        badgeCount = null
    ),
    BottomNavigationItem(
        "Spent",
        Icons.Filled.ShoppingCart ,
        Icons.Outlined.ShoppingCart ,
        hasNews = false,
        badgeCount = null
    ),
    BottomNavigationItem(
        "Profile",
        Icons.Filled.Person ,
        Icons.Outlined.Person ,
        hasNews = false,
        badgeCount = null
    ),
)



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(){
Text(text = "Home")
}