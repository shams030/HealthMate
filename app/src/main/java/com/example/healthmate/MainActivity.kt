package com.example.healthmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.healthmate.ui.theme.HealthMateTheme
import java.time.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.core.view.WindowCompat
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextAlign


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        val viewModel =
            ViewModelProvider(this)[HealthMateViewModel::class.java]

        val themePrefs = getSharedPreferences(
            "healthmate_settings",
            MODE_PRIVATE
        )

        setContent {
            var isDarkTheme by remember {
                mutableStateOf(
                    themePrefs.getBoolean("dark_theme", false)
                )
            }

            SideEffect {
                window.statusBarColor = if (isDarkTheme) {
                    android.graphics.Color.rgb(15, 23, 42)
                } else {
                    android.graphics.Color.rgb(245, 247, 250)
                }

                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                ).isAppearanceLightStatusBars = !isDarkTheme
            }

            HealthMateTheme(
                darkTheme = isDarkTheme
            ) {

                val accountStorage = remember {
                    AccountStorage(this@MainActivity)
                }

                var loggedInEmail by remember {
                    mutableStateOf(accountStorage.getLoggedInEmail() ?: "")
                }

                var currentScreen by remember {
                    mutableStateOf(
                        if (loggedInEmail.isNotEmpty()) "dashboard" else "login"
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {

                    when (currentScreen) {

                        "login" -> LoginScreen(
                            onLogin = { email ->
                                loggedInEmail = email
                                accountStorage.saveLoginSession(email)
                                currentScreen = "dashboard"
                            },

                            onRegister = {
                                currentScreen = "register"
                            }
                        )

                        "register" -> RegisterScreen(
                            onRegister = {
                                currentScreen = "login"
                            },
                            onBackToLogin = {
                                currentScreen = "login"
                            }
                        )

                        "dashboard" -> HealthMateDashboard(
                            viewModel = viewModel,
                            onLogout = {
                                accountStorage.clearLoginSession()
                                loggedInEmail = ""
                                currentScreen = "login"
                            },

                            onProfileClick = {
                                currentScreen = "profile"
                            },
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { dark ->
                                isDarkTheme = dark
                                themePrefs.edit()
                                    .putBoolean("dark_theme", dark)
                                    .apply()
                            }
                        )

                        "profile" -> ProfileScreen(
                            name = AccountStorage(this@MainActivity).getName(loggedInEmail),
                            email = loggedInEmail,
                            onBack = {
                                currentScreen = "dashboard"
                            }
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun HealthMateDashboard(
        viewModel: HealthMateViewModel,
        onLogout: () -> Unit,
        onProfileClick: () -> Unit,
        isDarkTheme: Boolean,
        onThemeChange: (Boolean) -> Unit
    ) {
        val records by viewModel.records.collectAsState()

        var showAddRecordForm by remember {
            mutableStateOf(false)
        }

        var recordToDelete by remember {
            mutableStateOf<HealthRecord?>(null)
        }

        var recordToEdit by remember {
            mutableStateOf<HealthRecord?>(null)
        }

        var selectedDate by remember {
            mutableStateOf("")
        }

        val today = LocalDate.now().toString()

        val todayRecords = records.filter {
            it.date == today
        }

        val totalSteps = todayRecords.sumOf { it.steps }
        val totalCalories = todayRecords.sumOf { it.calories }
        val totalWater = todayRecords.sumOf { it.water }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HealthMate",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Stay healthy, stay happy!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Logout", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme Switch
            Button(
                onClick = {
                    onThemeChange(!isDarkTheme)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isDarkTheme) {
                        "☀ Switch to Light Mode"
                    } else {
                        "🌙 Switch to Dark Mode"
                    },
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onProfileClick
            ) {
                Text("My Profile")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your personal health dashboard",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Today's Overview
            Text(
                text = "Today's Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    title = "Steps",
                    value = totalSteps.toString(),
                    unit = "steps",
                    modifier = Modifier.weight(1f)
                )

                HealthCard(
                    title = "Calories",
                    value = totalCalories.toString(),
                    unit = "kcal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HealthCard(
                title = "Water Intake",
                value = totalWater.toString(),
                unit = "ml today",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    showAddRecordForm = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Health Record")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Daily Health Goals
            Text(
                text = "Daily Health Goals",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoalCard(
                title = "Walking Goal",
                description = "Target: 10,000 steps per day",
                current = totalSteps,
                target = 10000,
                progressColor = Color(0xFF16A085)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalCard(
                title = "Water Goal",
                description = "Target: 2,000 ml per day",
                current = totalWater,
                target = 2000,
                progressColor = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalCard(
                title = "Calorie Tracking",
                description = "Daily activity calories",
                current = totalCalories,
                target = 500,
                progressColor = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Health Records History
            Text(
                text = "Health Records History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedDate,
                onValueChange = {
                    selectedDate = it
                },
                label = {
                    Text("Search by date (YYYY-MM-DD)")
                },
                placeholder = {
                    Text("2026-11-21")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (records.isEmpty()) {
                Text(
                    text = "No health records available.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                records.filter { record ->
                    selectedDate.isBlank() ||
                            record.date == selectedDate.trim()
                }.forEach { record ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = record.date,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Steps: ${record.steps}",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Calories: ${record.calories} kcal",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Water: ${record.water} ml",
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        recordToEdit = record
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit")
                                }

                                Button(
                                    onClick = {
                                        recordToDelete = record
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version
            Text(
                text = "HealthMate v1.0.0",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add Health Record Dialog
        if (showAddRecordForm) {
            AddHealthRecordDialog(
                viewModel = viewModel,
                onDismiss = {
                    showAddRecordForm = false
                }
            )
        }

        // Delete Confirmation Dialog
        if (recordToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    recordToDelete = null
                },
                title = {
                    Text("Delete Health Record")
                },
                text = {
                    Text("Are you sure you want to delete this health record?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            recordToDelete?.let {
                                viewModel.deleteRecord(it)
                            }
                            recordToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            recordToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Health Record Dialog
        if (recordToEdit != null) {
            EditHealthRecordDialog(
                record = recordToEdit!!,
                viewModel = viewModel,
                onDismiss = {
                    recordToEdit = null
                }
            )
        }


    if (showAddRecordForm) {

            AddHealthRecordDialog(
                viewModel = viewModel,
                onDismiss = {
                    showAddRecordForm = false
                }
            )
        }

        if (recordToDelete != null) {

            AlertDialog(
                onDismissRequest = {
                    recordToDelete = null
                },
                title = {
                    Text("Delete Health Record")
                },
                text = {
                    Text("Are you sure you want to delete this health record?")
                },
                confirmButton = {
                    Button(
                        onClick = {

                            recordToDelete?.let {
                                viewModel.deleteRecord(it)
                            }

                            recordToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            recordToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (recordToEdit != null) {
            EditHealthRecordDialog(
                record = recordToEdit!!,
                viewModel = viewModel,
                onDismiss = {
                    recordToEdit = null
                }
            )
        }
    }





    @Composable
    fun HealthCard(
        title: String,
        value: String,
        unit: String,
        modifier: Modifier = Modifier
    ) {


        val accentColor = when (title) {
            "Steps" -> Color(0xFF34D399)
            "Calories" -> Color(0xFFFBBF24)
            "Water Intake" -> Color(0xFF38BDF8)
            else -> Color(0xFF34D399)
        }

        val icon = when (title) {
            "Steps" -> "🚶"
            "Calories" -> "🔥"
            "Water Intake" -> "💧"
            else -> "❤️"
        }

        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = icon,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = unit,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }




    @Composable
    fun GoalCard(
        title: String,
        description: String,
        current: Int = 0,
        target: Int = 0,
        progressColor: Color = Color(0xFF34D399)
    ) {
        val progress = if (target > 0) {
            (current.toFloat() / target).coerceIn(0f, 1f)
        } else {
            0f
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (target > 0) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$current / $target",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun AddHealthRecordDialog(
        viewModel: HealthMateViewModel,
        onDismiss: () -> Unit
    ) {

        var date by remember {
            mutableStateOf(LocalDate.now().toString())
        }

        var steps by remember {
            mutableStateOf("")
        }

        var calories by remember {
            mutableStateOf("")
        }

        var water by remember {
            mutableStateOf("")
        }

        var errorMessage by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = onDismiss,

            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

            title = {
                Text("Add Health Record")
            },

            text = {
                Column {

                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            date = it
                            errorMessage = ""
                        },
                        label = {
                            Text("Date (YYYY-MM-DD)")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )

                    )

                    OutlinedTextField(
                        value = steps,
                        onValueChange = {
                            steps = it
                        },
                        label = {
                            Text("Steps")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = calories,
                        onValueChange = {
                            calories = it
                        },
                        label = {
                            Text("Calories")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = water,
                        onValueChange = {
                            water = it
                        },
                        label = {
                            Text("Water (ml)")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    if (errorMessage.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {

                        val stepValue = steps.toIntOrNull()
                        val calorieValue = calories.toIntOrNull()
                        val waterValue = water.toIntOrNull()

                        val validDate = try {
                            LocalDate.parse(date)
                            true
                        } catch (_: Exception) {
                            false
                        }

                        if (!validDate) {
                            errorMessage = "Please enter a valid date (YYYY-MM-DD)."
                        } else if (
                            stepValue == null ||
                            calorieValue == null ||
                            waterValue == null
                        ) {
                            errorMessage = "Please enter valid numbers in all fields."
                        } else if (
                            stepValue < 0 ||
                            calorieValue < 0 ||
                            waterValue < 0
                        ) {
                            errorMessage = "Values cannot be negative."
                        } else {
                            viewModel.addRecord(
                                HealthRecord(
                                    date = date,
                                    steps = stepValue,
                                    calories = calorieValue,
                                    water = waterValue
                                )
                            )
                            onDismiss()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D9488)
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            },

            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155)
                    )
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }


    @Composable
    fun EditHealthRecordDialog(
        record: HealthRecord,
        viewModel: HealthMateViewModel,
        onDismiss: () -> Unit
    ) {
        var date by remember { mutableStateOf(record.date) }
        var steps by remember { mutableStateOf(record.steps.toString()) }
        var calories by remember { mutableStateOf(record.calories.toString()) }
        var water by remember { mutableStateOf(record.water.toString()) }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            textContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,

            title = {
                Text("Edit Health Record")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = steps,
                        onValueChange = { steps = it },
                        label = { Text("Steps") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = Color(0xFF34D399),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFF34D399)
                        )
                    )

                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = Color(0xFF34D399),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFF34D399)
                        )
                    )

                    OutlinedTextField(
                        value = water,
                        onValueChange = { water = it },
                        label = { Text("Water (ml)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = Color(0xFF34D399),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            cursorColor = Color(0xFF34D399)
                        )
                    )

                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFF87171)
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val stepValue = steps.toIntOrNull()
                        val calorieValue = calories.toIntOrNull()
                        val waterValue = water.toIntOrNull()

                        val validDate = try {
                            LocalDate.parse(date)
                            true
                        } catch (e: Exception) {
                            false
                        }

                        if (!validDate) {
                            errorMessage = "Invalid date."
                        } else if (
                            stepValue == null ||
                            calorieValue == null ||
                            waterValue == null
                        ) {
                            errorMessage = "Enter valid numbers."
                        } else if (
                            stepValue < 0 ||
                            calorieValue < 0 ||
                            waterValue < 0
                        ) {
                            errorMessage = "Values cannot be negative."
                        } else {
                            viewModel.updateRecord(
                                record.copy(
                                    date = date,
                                    steps = stepValue,
                                    calories = calorieValue,
                                    water = waterValue
                                )
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D9488)
                    )
                ) {
                    Text("Update", color = Color.White)
                }
            },

            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF334155)
                    )
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
