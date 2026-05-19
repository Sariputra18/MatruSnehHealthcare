package com.matrusneh.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val vm: AppViewModel = viewModel()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { SplashScreen(navController, vm) }
                        composable("setup") { SetupScreen(navController, vm) }
                        composable("home") { HomeScreen(navController, vm) }
                        composable("kicks") { KickCounterScreen(navController, vm) }
                        composable("emergency") { EmergencyScreen(navController, vm) }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController, vm: AppViewModel) {
    val profile by vm.profile.collectAsState()
    LaunchedEffect(Unit) {
        delay(2000)
        if (profile != null) navController.navigate("home") { popUpTo("splash") { inclusive = true } }
        else navController.navigate("setup") { popUpTo("splash") { inclusive = true } }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Matru-Sneh", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(navController: NavHostController, vm: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var asha by remember { mutableStateOf("") }
    var edd by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isKannada by remember { mutableStateOf(false) }
    val canPop by remember { mutableStateOf(navController.previousBackStackEntry != null) }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        // Top Controls
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canPop) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
            TextButton(onClick = { isKannada = !isKannada }) {
                Text(if (isKannada) "English" else "ಕನ್ನಡ", fontWeight = FontWeight.Bold)
            }
        }

        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isKannada) "ಪ್ರೊಫೈಲ್ ಸೆಟಪ್" else "Profile Setup",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isKannada) "ತಾಯಿಯ ಹೆಸರು" else "Mother's Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(edd)),
                onValueChange = {},
                label = { Text(if (isKannada) "ನಿರೀಕ್ಷಿತ ದಿನಾಂಕ" else "Due Date") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = asha,
                onValueChange = { asha = it },
                label = { Text(if (isKannada) "ಆಶಾ ಕಾರ್ಯಕರ್ತರ ಫೋನ್" else "ASHA Worker Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    vm.saveProfile(name, edd, asha)
                    navController.navigate("home") { popUpTo("setup") { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && asha.isNotBlank()
            ) {
                Text(if (isKannada) "ಉಳಿಸಿ ಮತ್ತು ಮುಂದುವರಿಸಿ" else "Save & Continue")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = edd)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, 
            confirmButton = { TextButton(onClick = { edd = datePickerState.selectedDateMillis ?: edd; showDatePicker = false }) { Text("OK") } }) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, vm: AppViewModel) {
    val profile by vm.profile.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showNutrition by remember { mutableStateOf(false) }
    var showDanger by remember { mutableStateOf(false) }
    var isKannada by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isKannada) "ಮಾತೃ-ಸ್ನೇಹ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್" else "Matru-Sneh Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { isKannada = !isKannada }) {
                        Text(if (isKannada) "English" else "ಕನ್ನಡ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text(
                if (isKannada) "ನಮಸ್ತೆ, ${profile?.name ?: "ತಾಯಿ"}" else "Namaste, ${profile?.name ?: "Mother"}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeCard(if (isKannada) "ಕಿಕ್ ಕೌಂಟರ್" else "Kick Counter", Icons.Default.Favorite, Modifier.weight(1f)) { navController.navigate("kicks") }
                HomeCard(if (isKannada) "ಪೋಷಣೆ" else "Nutrition", Icons.Default.List, Modifier.weight(1f)) { showNutrition = true }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HomeCard(if (isKannada) "ಅಪಾಯದ ಚಿಹ್ನೆಗಳು" else "Danger Signs", Icons.Default.Warning, Modifier.weight(1f)) { showDanger = true }
                HomeCard(if (isKannada) "ತುರ್ತು ಪರಿಸ್ಥಿತಿ" else "Emergency", Icons.Default.Call, Modifier.weight(1f), Color.Red) { navController.navigate("emergency") }
            }
        }
    }

    if (showNutrition) {
        ModalBottomSheet(onDismissRequest = { showNutrition = false }, sheetState = sheetState) {
            NutritionSheet(vm)
        }
    }
    if (showDanger) {
        ModalBottomSheet(onDismissRequest = { showDanger = false }, sheetState = sheetState) {
            DangerSignsSheet()
        }
    }
}

@Composable
fun HomeCard(title: String, icon: ImageVector, modifier: Modifier, color: Color = MaterialTheme.colorScheme.primaryContainer, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(150.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KickCounterScreen(navController: NavHostController, vm: AppViewModel) {
    val todayCount by vm.kicksToday.collectAsState()
    val lastHourCount by vm.kicksLastHour.collectAsState()
    val allKicks by vm.allKicks.collectAsState()

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { navController.popBackStack() }, Modifier.align(Alignment.Start)) { Icon(Icons.Default.ArrowBack, null) }
        Text("Kick Counter", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Box(Modifier.size(200.dp).background(MaterialTheme.colorScheme.primary, CircleShape).clickable { vm.addKick() }, contentAlignment = Alignment.Center) {
            Text("ಒತ್ತಿ", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CountBox("Today", todayCount)
            CountBox("Last Hour", lastHourCount)
        }
        Spacer(Modifier.height(24.dp))
        LazyColumn(Modifier.fillMaxWidth()) {
            items(allKicks) { kick ->
                ListItem(headlineContent = { Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(kick.timestamp))) }, 
                    supportingContent = { Text(kick.date) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun CountBox(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(count.toString(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EmergencyScreen(navController: NavHostController, vm: AppViewModel) {
    val profile by vm.profile.collectAsState()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(Color.Red).padding(24.dp)) {
        IconButton(onClick = { navController.popBackStack() }, Modifier.statusBarsPadding()) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                "ತಕ್ಷಣ ಸಹಾಯ ಪಡೆಯಿರಿ",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile?.ashaPhone}"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red)
            ) {
                Icon(Icons.Default.Call, null)
                Spacer(Modifier.width(8.dp))
                Text("Call ASHA Worker")
            }
            Spacer(Modifier.height(48.dp))
            SafetyTip("ಶಾಂತವಾಗಿರಿ ಮತ್ತು ಆಳವಾದ ಉಸಿರು ತೆಗೆದುಕೊಳ್ಳಿ")
            SafetyTip("ನಿಮ್ಮ ಹತ್ತಿರದ ಆಸ್ಪತ್ರೆಗೆ ಹೋಗಿ")
            SafetyTip("ಕುಟುಂಬದ ಸದಸ್ಯರಿಗೆ ತಕ್ಷಣ ತಿಳಿಸಿ")
        }
    }
}

@Composable
fun SafetyTip(text: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))) {
        Text(text, Modifier.padding(16.dp), color = Color.Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NutritionSheet(vm: AppViewModel) {
    val foods = listOf("Leafy Greens", "Milk/Curd", "Fruits", "Iron Tablet", "Protein", "Water (3L)")
    val logs by vm.getNutrition(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())).collectAsState(initial = emptyList())
    
    LazyColumn(Modifier.fillMaxWidth().padding(16.dp)) {
        item { Text("Daily Nutrition Checklist", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(16.dp)) }
        items(foods) { food ->
            val log = logs.find { it.food == food } ?: NutritionLog(date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), food = food, isChecked = false)
            Row(Modifier.fillMaxWidth().clickable { vm.toggleNutrition(log) }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = log.isChecked, onCheckedChange = { vm.toggleNutrition(log) })
                Text(food)
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DangerSignsSheet() {
    val signs = listOf("Severe Headache", "Blurred Vision", "Swelling", "Vaginal Bleeding", "Reduced Kicks", "High Fever")
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Danger Signs (ತಕ್ಷಣ ವೈದ್ಯರನ್ನು ಭೇಟಿ ಮಾಡಿ)", style = MaterialTheme.typography.headlineSmall, color = Color.Red)
        Spacer(Modifier.height(16.dp))
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            signs.forEach { sign ->
                FilterChip(selected = true, onClick = {}, label = { Text(sign) }, 
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFEBEE), selectedLabelColor = Color.Red))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
