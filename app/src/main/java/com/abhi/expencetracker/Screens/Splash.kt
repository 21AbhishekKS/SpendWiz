package com.abhi.expencetracker.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.abhi.expencetracker.R
import com.abhi.expencetracker.navigation.Routes
import kotlinx.coroutines.delay

@Composable
    fun SplashScreen(navController: NavController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(R.drawable.gross) , "")
            Text(text = "SpendWiz", style = MaterialTheme.typography.headlineSmall)  // Add your text here
        }
        LaunchedEffect(Unit) {
            delay(2000)
            navController.navigate(Routes.HomeScreen.route)
        }

}