package com.example.universalyogaapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import res.drawable.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.clickable
import com.example.universalyogaapp.components.CommonScaffold
import androidx.compose.material3.MaterialTheme


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    CommonScaffold(
        navController = navController,
        title = "Home"
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { Header(navController) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ImageSlider() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Statistics(navController) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Courses", navController) }
            item { CoursesList() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Classes", navController) }
            item { ClassesList() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("Registered Participants", navController) }
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
                .padding(16.dp),
            activeColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun Header(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
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
        
        IconButton(
            onClick = { navController.navigate(Routes.Profile.route) }
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.DarkGray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

sealed class StatType {
    object Courses : StatType()
    object Classes : StatType()
    object Instructors : StatType()
    object Participants : StatType()
}

@Composable
fun StatItem(
    title: String, 
    value: String, 
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    statType: StatType,
    navController: NavController  // Add navController parameter
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable {
                when (statType) {
                    StatType.Courses -> navController.navigate(Routes.Courses.route)
                    StatType.Classes -> navController.navigate(Routes.Classes.route)
                    StatType.Instructors -> navController.navigate(Routes.Instructors.route)
                    StatType.Participants -> navController.navigate(Routes.Participants.route)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (painter != null) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun Statistics(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatItem(
                title = "Courses",
                value = "10",
                painter = painterResource(id = R.drawable.ic_course),
                modifier = Modifier.weight(1f),
                statType = StatType.Courses,
                navController = navController
            )
            StatItem(
                title = "Classes",
                value = "45",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                statType = StatType.Classes,
                navController = navController
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatItem(
                title = "Instructors",
                value = "12",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                statType = StatType.Instructors,
                navController = navController
            )
            StatItem(
                title = "Participants",
                value = "150",
                painter = painterResource(id = R.drawable.ic_participants),
                modifier = Modifier.weight(1f),
                statType = StatType.Participants,
                navController = navController
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    navController: NavController  // Add navController parameter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "See all",
            style = MaterialTheme.typography.bodySmall.copy(
                textDecoration = TextDecoration.Underline
            ),
            color = Color.Gray,
            modifier = Modifier.clickable {
                when (title) {
                    "Courses" -> navController.navigate(Routes.Courses.route)
                    "Classes" -> navController.navigate(Routes.Classes.route)
                    "Registered Participants" -> navController.navigate(Routes.Participants.route)
                }
            }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CoursesList() {
    val courses = listOf(
        Course("Morning Yoga", "Duration: 60 mins, Level: Beginner", R.drawable.ic_course),
        Course("Evening Relaxation", "Duration: 45 mins, Level: Intermediate", R.drawable.ic_course)
    )
    courses.forEach { course ->
        CourseItem(course)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CourseItem(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = course.iconResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,  // Light gray background
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
        Participant("Olivia Brown", "Registered: 2023-09-25, Contact: ...", R.drawable.ic_participants),
        Participant("Noah Wilson", "Registered: 2023-09-26, Contact: ...", R.drawable.ic_participants)
    )
    participants.forEach { participant ->
        ParticipantItem(participant)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ParticipantItem(participant: Participant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,  // Light gray background
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = participant.imageResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = participant.name, style = MaterialTheme.typography.titleMedium)
                Text(text = participant.details, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

data class Course(val name: String, val details: String, val iconResId: Int)
data class YogaClass(val name: String, val details: String)
data class Participant(val name: String, val details: String, val imageResId: Int)

data class BottomNavItem(
    val title: String,
    val icon: ImageVector? = null,
    val iconResId: Int? = null,
    val route: String
)

