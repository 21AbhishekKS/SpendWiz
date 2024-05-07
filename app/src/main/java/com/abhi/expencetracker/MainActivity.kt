package com.abhi.expencetracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Surface

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.abhi.expencetracker.Screens.BottomNav
import com.abhi.expencetracker.Screens.HomeScreen
import com.abhi.expencetracker.Database.money.ViewModels.AddScreenViewModel

import com.abhi.expencetracker.ui.theme.ExpenceTrackerTheme


class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val  moneyViewModel  = ViewModelProvider(this)[AddScreenViewModel::class.java]


        setContent {
            ExpenceTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {


                    val navController  = rememberNavController()
                    BottomNav(navController , moneyViewModel)






                }
            }
        }
    }}


