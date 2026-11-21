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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel =
            ViewModelProvider(this)[HealthMateViewModel::class.java]

        setContent {
            HealthMateTheme {

                var currentScreen by remember {
                    mutableStateOf("login")
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F7FA)
                ) {

                    when (currentScreen) {

                        "login" -> LoginScreen(
                            onLogin = {
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
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun HealthMateDashboard(
    viewModel: HealthMateViewModel,
    onLogout: () -> Unit
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
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HealthMate",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF176B5B)
                )

                Button(
                    onClick = {
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your personal health dashboard",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Today's Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
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
            onClick = { showAddRecordForm = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Health Record")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Daily Health Goals",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(16.dp))

        GoalCard(
            title = "Walking Goal",
            description = "Target: 10,000 steps per day"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GoalCard(
            title = "Water Goal",
            description = "Target: 2,000 ml per day"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GoalCard(
            title = "Calorie Tracking",
            description = "Monitor your daily activity"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Health Records History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(16.dp))

        var selectedDate by remember { mutableStateOf("") }

        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            label = { Text("Search by date (YYYY-MM-DD)") },
            placeholder = { Text("2026-11-21") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (records.isEmpty()) {

            Text(
                text = "No health records available.",
                color = Color.Gray,
                fontSize = 14.sp
            )

        } else {

            records.filter{ record ->
                selectedDate.isBlank() || record.date.contains(selectedDate)
            }.forEach { record ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
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
                            color = Color(0xFF176B5B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Steps: ${record.steps}")

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Calories: ${record.calories} kcal")

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Water: ${record.water} ml")

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

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF176B5B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = unit,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun GoalCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF176B5B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
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

                        errorMessage =
                            "Please enter a valid date (YYYY-MM-DD)."

                    } else if (
                        stepValue == null ||
                        calorieValue == null ||
                        waterValue == null
                    ) {

                        errorMessage =
                            "Please enter valid numbers in all fields."

                    } else if (
                        stepValue < 0 ||
                        calorieValue < 0 ||
                        waterValue < 0
                    ) {

                        errorMessage =
                            "Values cannot be negative."

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
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
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
        title = { Text("Edit Health Record") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") }
                )

                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Steps") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = water,
                    onValueChange = { water = it },
                    label = { Text("Water (ml)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red
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
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
