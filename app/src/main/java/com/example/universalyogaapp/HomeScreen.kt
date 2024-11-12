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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.viewmodels.HomeViewModel
import com.example.universalyogaapp.viewmodels.CourseViewModel
import com.example.universalyogaapp.viewmodels.ClassViewModel
import com.example.universalyogaapp.data.CourseWithClassCount
import androidx.compose.material.icons.filled.Schedule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.universalyogaapp.data.YogaClass


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
            item { CoursesList(navController) }
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
    object Bookings : StatType()
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
                    StatType.Bookings -> navController.navigate(Routes.Bookings.route)
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
    val viewModel: HomeViewModel = viewModel()
    val statistics by viewModel.statistics.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatItem(
                title = "Bookings",
                value = statistics.bookingsCount.toString(),
                painter = painterResource(id = R.drawable.ic_booking),
                modifier = Modifier.weight(1f),
                statType = StatType.Bookings,
                navController = navController
            )
            StatItem(
                title = "Courses",
                value = statistics.coursesCount.toString(),
                painter = painterResource(id = R.drawable.ic_course),
                modifier = Modifier.weight(1f),
                statType = StatType.Courses,
                navController = navController
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatItem(
                title = "Instructors",
                value = statistics.instructorsCount.toString(),
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                statType = StatType.Instructors,
                navController = navController
            )
            StatItem(
                title = "Classes",
                value = statistics.classesCount.toString(),
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                statType = StatType.Classes,
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
fun CourseCard(courseWithCount: CourseWithClassCount, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_course),
                    contentDescription = "Course Icon",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = courseWithCount.course.daysOfWeek,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${courseWithCount.course.timeOfCourse} | ${courseWithCount.course.duration / 60} Hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = courseWithCount.course.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Text(
                    text = "capacity: ${courseWithCount.course.capacity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${courseWithCount.classCount} ${if (courseWithCount.classCount > 1) "Classes" else "Class"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price
            Text(
                text = "£${courseWithCount.course.pricePerClass}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CoursesList(navController: NavController) {
    val courseViewModel: CourseViewModel = viewModel()
    val classViewModel: ClassViewModel = viewModel()
    val courses by courseViewModel.firebaseCourses.collectAsState()
    val classes by classViewModel.classes.collectAsState()

    LaunchedEffect(Unit) {
        courseViewModel.loadCoursesFromFirebase()
    }

    if (courses.isEmpty()) {
        Text(
            text = "No courses available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        // Show last 2 courses instead of first 2
        courses.takeLast(2).forEach { course ->
            val classCount = classes.count { it.courseName == course.courseName }
            CourseCard(
                courseWithCount = CourseWithClassCount(
                    course = course,
                    classCount = classCount
                ),
                onClick = { navController.navigate("course_detail/${course.id}") }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ClassesList() {
    val classViewModel: ClassViewModel = viewModel()
    val classes by classViewModel.classes.collectAsState()

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
    }

    if (classes.isEmpty()) {
        Text(
            text = "No classes available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        // Show last 2 classes
        classes.takeLast(2).forEach { yogaClass ->
            ClassCard(yogaClass = yogaClass)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ClassCard(yogaClass: YogaClass) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Calendar Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date: ${formatDate(yogaClass.date)}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = yogaClass.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = yogaClass.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Instructor: ${yogaClass.instructorName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!yogaClass.comment.isNullOrBlank()) {
                    Text(
                        text = "Note: ${yogaClass.comment}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateString
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

data class Participant(val name: String, val details: String, val imageResId: Int)

data class BottomNavItem(
    val title: String,
    val icon: ImageVector? = null,
    val iconResId: Int? = null,
    val route: String
)

