package com.example.universalyogaapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.universalyogaapp.R
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigation() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item { Header() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ImageSlider() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Statistics() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Courses") }
            item { CoursesList() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Classes") }
            item { ClassesList() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Registered Participants") }
            item { ParticipantsList() }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageSlider() {
    val pagerState = rememberPagerState(initialPage = 0)
    val imageSlider = listOf(
        painterResource(id = R.drawable.slider1),
        painterResource(id = R.drawable.slider2),
        painterResource(id = R.drawable.slider3)
    )

    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(2600)
            pagerState.animateScrollToPage(
                page = (pagerState.currentPage + 1) % (pagerState.pageCount)
            )
        }
    }

    Column {
        HorizontalPager(
            count = imageSlider.size,
            state = pagerState,
            modifier = Modifier
                .height(214.dp)
                .fillMaxWidth()
        ) { page ->
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .graphicsLayer {
                        val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also { scale ->
                            scaleX = scale
                            scaleY = scale
                        }

                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                Image(
                    painter = imageSlider[page],
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Yoga Illustration",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Yoga Studio Admin",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Statistics() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            title = "Classes",
            value = "45",
            icon = { 
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFFB47B84)
                )
            }
        )
        StatItem(
            title = "Participants",
            value = "120",
            icon = { 
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFB47B84)
                )
            }
        )
    }
}

@Composable
fun StatItem(title: String, value: String, icon: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "see all >", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CoursesList() {
    val courses = listOf(
        Course("Morning Yoga", "Duration: 60 mins, Level: Beginner", R.drawable.ic_launcher_foreground),
        Course("Evening Relaxation", "Duration: 45 mins, Level: Intermediate", R.drawable.ic_launcher_foreground)
    )
    courses.forEach { course ->
        CourseItem(course)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CourseItem(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = course.iconResId),
                contentDescription = null,
                tint = Color(0xFFB47B84),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = course.name, style = MaterialTheme.typography.titleMedium)
                Text(text = course.details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ClassesList() {
    val classes = listOf(
        YogaClass("Sunrise Flow", "Date: 2023-10-01, Time: 6:00 AM, Instructor: Emma"),
        YogaClass("Gentle Stretch", "Date: 2023-10-02, Time: 7:00 AM, Instructor: Lia")
    )
    classes.forEach { yogaClass ->
        ClassItem(yogaClass)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ClassItem(yogaClass: YogaClass) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color(0xFFB47B84),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = yogaClass.name, style = MaterialTheme.typography.titleMedium)
                Text(text = yogaClass.details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ParticipantsList() {
    val participants = listOf(
        Participant("Olivia Brown", "Registered: 2023-09-25, Contact: ...", R.drawable.ic_launcher_foreground),
        Participant("Noah Wilson", "Registered: 2023-09-26, Contact: ...", R.drawable.ic_launcher_foreground)
    )
    participants.forEach { participant ->
        ParticipantItem(participant)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ParticipantItem(participant: Participant) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = participant.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = participant.name, style = MaterialTheme.typography.titleMedium)
                Text(text = participant.details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun BottomNavigation() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = true,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ThumbUp, contentDescription = null) },
            label = { Text("Courses") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
            label = { Text("Classes") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Participants") },
            selected = false,
            onClick = { }
        )
    }
}

data class Course(val name: String, val details: String, val iconResId: Int)
data class YogaClass(val name: String, val details: String)
data class Participant(val name: String, val details: String, val imageResId: Int)
