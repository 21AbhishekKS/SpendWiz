package com.abhi.expencetracker.utils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhi.expencetracker.helper.OnBoarding.LoaderIntro
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.abhi.expencetracker.R
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment


@Preview(showBackground = true)
@Composable
fun AnimatedIconCard(){

    Row( Modifier
        .padding(horizontal = 15.dp )
        .padding(bottom = 10.dp)
        .fillMaxWidth()
        .wrapContentHeight()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp ,color = Color.Black , shape = RoundedCornerShape(16.dp))
        .padding(start = 15.dp)
        , horizontalArrangement = Arrangement.SpaceBetween
        , verticalAlignment = Alignment.CenterVertically
){
        
        Text(text = "Ready to Manage \n Your Money!")
        
        LoaderIntro(modifier = Modifier
            .fillMaxWidth()
            .size(120.dp)
            , image = R.raw.money_blue)
    }
   

}