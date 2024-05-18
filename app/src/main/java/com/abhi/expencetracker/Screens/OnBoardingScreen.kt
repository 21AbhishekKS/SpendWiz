package com.abhi.expencetracker.Screens

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.expencetracker.helper.OnBoarding.LoaderIntro
import com.abhi.expencetracker.helper.OnBoarding.OnBoardingData
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.abhi.expencetracker.R




    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun OnBoardingScreen(){
    val boardingItems = ArrayList<OnBoardingData>()




    boardingItems.add(
        OnBoardingData(R.raw.a5 ,
    "Track It ? Own It" ,
    " Reach your financial goals faster.\n" +
    " Our expense tracker empowers you\n" +
    " to make informed spending\n " +
    "decisions.")
    )


    boardingItems.add(
        OnBoardingData(
        R.raw.a4,
    "Own Your Money" ,
    "Track your income easily and \n" +
    " manage expenses effortlessly gain\n" +
    " financial insights and manage \n" +
    " money with confidence!")
    )

    boardingItems.add(
        OnBoardingData(R.raw.a8 ,
    "Spend Smarter" ,
    "Securely track your expenses and\n" +
    " build a healthier financial future. Our\n" +
    " app keeps your data safe and\n" +
    "organized.")
    )



    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { boardingItems.size } // Provide total number of pages here
    )





    Column(
        horizontalAlignment = Alignment.CenterHorizontally ,
    modifier = Modifier.fillMaxHeight()
    //.padding(top=30.dp)
    .background(Color.White))
    {
        OnBoardPager(boardingItems , pagerState , Modifier.fillMaxWidth())

// have to  be written outside of this Column
        Box(contentAlignment = Alignment.BottomCenter) {
            PageIndicator(boardingItems.size  , pagerState.currentPage)
        }


        Box(contentAlignment = Alignment.BottomCenter) {
            BottomSection(pagerState.currentPage)
        }
    }







}











@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardPager(item: ArrayList<OnBoardingData>, pagerState: PagerState, modifier: Modifier){

    Box(modifier = modifier){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            HorizontalPager(state = pagerState) {page ->

                Column(horizontalAlignment =  Alignment.CenterHorizontally , modifier = Modifier.fillMaxWidth())
                {

                    //  Image(painter = painterResource(id = item[page].image), contentDescription ="" )

                    LoaderIntro(modifier = Modifier.fillMaxWidth()
                        .padding(top = 25.dp)
                        .size(200.dp)
                        .align(alignment = Alignment.CenterHorizontally), item[page].image)

                    Text(text = item[page].title ,
                        Modifier.padding(top =12.dp),
                        fontSize = 20.sp ,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold)

                    Text(text = item[page].description ,
                        Modifier.padding(20.dp) ,
                        fontSize = 14.sp ,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraLight)


                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageIndicator(size: Int, pagerState: Int){
    Row(horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(bottom = 100.dp)){

        repeat(size){
            Indicator(isSelected = it == pagerState)
        }
    }
}


@Composable
fun Indicator( isSelected : Boolean){
    val width = animateDpAsState(targetValue = if(isSelected) 25.dp else 10.dp)

    Box(modifier = Modifier
        .padding(1.dp)
        .height(6.dp)
        .width(width.value)
        .clip(CircleShape)
        .background(if (isSelected) Color.Red else Color.LightGray)
    )
}

@Composable
fun BottomSection(currentPager : Int){

    Row(
        modifier = Modifier.padding(20.dp),
        horizontalArrangement = if (currentPager != 2) Arrangement.SpaceBetween else Arrangement.Center
    ){

        if(currentPager == 2){
            OutlinedButton(onClick = {



                                     }
                , shape = RoundedCornerShape(50)) {
                Text(text = "Get Started" , color = Color.DarkGray)
            }
        }
        else{

            SkipOrNext("Skip" ,)
            SkipOrNext("Next")
        }
    }
}

@Composable
fun SkipOrNext( text :String){

    Text(text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium ,
        modifier = Modifier.padding(20.dp).padding(horizontal = 25.dp)
    )
}




