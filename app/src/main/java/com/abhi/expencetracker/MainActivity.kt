package com.abhi.expencetracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Surface

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.abhi.expencetracker.navigation.BottomNav
import com.abhi.expencetracker.ViewModels.AddScreenViewModel

import com.abhi.expencetracker.ui.theme.ExpenceTrackerTheme


class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val  moneyViewModel  = ViewModelProvider(this)[AddScreenViewModel::class.java]

        installSplashScreen()

        setContent {
            ExpenceTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController  = rememberNavController()


                        BottomNav(navController , moneyViewModel)




                   // NavGraph(navController = navController, viewModel = moneyViewModel )

                }
            }
        }
    }}


