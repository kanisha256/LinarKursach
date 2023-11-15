package com.example.linarkursach

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linarkursach.db.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomBottomNavigation()
        }
    }

    @Composable
    fun CustomBottomNavigation() {
        var selectedTabIndex by remember { mutableStateOf(0) }

        MaterialTheme(
        ) {
            Scaffold(
                bottomBar = {
                    BottomNavigation {
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Поездки"
                                )
                            },
                            label = { Text("Поездки") },
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 }
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Add, contentDescription = "Создать") },
                            label = { Text("Создать") },
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 }
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                            label = { Text("Профиль") },
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 }
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Place, contentDescription = "Карта") },
                            label = { Text("Карта") },
                            selected = selectedTabIndex == 3,
                            onClick = { selectedTabIndex = 3 }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            Tripd()
                        }
                        1 -> {
                            CreateTripScreen()
                        }
                        2 -> {
                            Profile()
                        }
                        3 -> {
                            val intent = Intent(this@HomeActivity, Map::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Profile() {
        var name by remember { mutableStateOf("") }
        var ageF by remember { mutableStateOf("") }
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser!!.email
        intent = Intent(this, MainActivity::class.java)
        Thread {
            try {
                val driver: Driver = GraphDatabase.driver(
                    "neo4j+ssc://575bedd2.databases.neo4j.io:7687",
                    AuthTokens.basic("neo4j", "NDevwfL2okljy02sx25un68ggZK1tu8_aB3Y8z8WgUU")
                )

                val session: Session = driver.session()

                val query = """
                MATCH (u:User {email: "$currentUser"})
                RETURN u.fio AS fio, u.age AS age
                """
                val result = session.run(query)

                // Извлекаем значение fio из результата и устанавливаем его в переменную name
                if (result.hasNext()) {
                    val record = result.single()
                    val fio = record["fio"].asString()
                    val age = record["age"].asString()
                    name = fio
                    ageF = age
                }
            } catch (e: ClientException) {
            }
        }.start()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F3F3))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TopAppBar(
                    title = { Text("Профиль", fontSize = 20.sp) },
                    backgroundColor = Color.Transparent
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 8.dp,
                    backgroundColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ФИО Пользователя",
                            color = Color(0xFF2C3E50),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = name,
                            color = Color(0xFF2C3E50),
                            fontSize = 18.sp
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                        Text(
                            text = "Почта",
                            color = Color(0xFF2C3E50),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.toString(),
                            color = Color(0xFF2C3E50),
                            fontSize = 18.sp
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                        Text(
                            text = "Оценка",
                            color = Color(0xFF2C3E50),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "5", // Замените на свой текст
                            color = Color(0xFF2C3E50),
                            fontSize = 18.sp
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                        Text(
                            text = "Возраст",
                            color = Color(0xFF2C3E50),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ageF, // Замените на свой текст
                            color = Color(0xFF2C3E50),
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            Firebase.auth.signOut()
                            val intent = Intent(this@HomeActivity, MainActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Text("Выйти")
                    }

                    Button(
                        onClick = {
                            val youtubeVideoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

                            // Создаем Uri из строки URL
                            val uri = Uri.parse(youtubeVideoUrl)

                            // Создаем Intent для открытия браузера с данной ссылкой
                            val intent = Intent(Intent.ACTION_VIEW, uri)

                            // Запускаем активность браузера
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Text("Пользовательское соглашение")
                    }
                }
            }
        }
    }

    @Composable
    fun CreateTripScreen() {
        var departureText by remember { mutableStateOf("") }
        var destinationText by remember { mutableStateOf("") }
        var name2 by remember { mutableStateOf("") }
        var numberOfPeople by remember { mutableStateOf(1) }
        var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
        var isTimePickerVisible by remember { mutableStateOf(false) }
        var timeLabelText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name2,
                onValueChange = { name2 = it },
                label = { Text("Название") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = departureText,
                onValueChange = { departureText = it },
                label = { Text("Откуда") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                label = { Text("Куда") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    isTimePickerVisible = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text("Выбрать время")
            }

            Text(
                text = timeLabelText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(Color.White, RectangleShape)
                    .border(1.dp, Color.Gray, RectangleShape)
            )
            if (isTimePickerVisible) {
                val now = Calendar.getInstance()

                val datePicker = DatePickerDialog(
                    this@HomeActivity,
                    { _, year, month, dayOfMonth ->
                        val timePicker = TimePickerDialog(
                            this@HomeActivity,
                            { _, hourOfDay, minute ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute)
                                selectedTime = selectedCalendar
                                timeLabelText =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(selectedTime.time)
                                isTimePickerVisible = false
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                        )
                        timePicker.show()
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                )

                datePicker.show()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Количествр людей: $numberOfPeople")
                Slider(
                    value = numberOfPeople.toFloat(),
                    onValueChange = { numberOfPeople = it.toInt() },
                    valueRange = 1f..4f,
                    steps = 1
                )
            }

            Button(
                onClick = {
                    val name2 = name2
                    val departure = departureText
                    val destination = destinationText
                    val time = timeLabelText
                    val numberOfPeople2 = numberOfPeople

                    createTripRecord(name2,departure, destination, time, numberOfPeople2.toString())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text(text = "Создать поездку")
            }
        }
    }

    fun createTripRecord(
        name2: String,
        departure: String,
        destination: String,
        time: String,
        numberOfPeople: String
    ) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser!!.email
        Thread {
            try {
                val driver: Driver = GraphDatabase.driver(
                    "neo4j+ssc://575bedd2.databases.neo4j.io:7687",
                    AuthTokens.basic("neo4j", "NDevwfL2okljy02sx25un68ggZK1tu8_aB3Y8z8WgUU")
                )
                val session = driver.session()
                try {
                    val createQuery = """
            CREATE (trip:Trip {
                name: '$name2',
                departure: '$departure',
                destination: '$destination',
                time: '$time',
                numberOfPeople: '$numberOfPeople'
            })
            RETURN trip
        """.trimIndent()

                    val createRelationshipQuery = """
    MATCH (user:User {email: '$currentUser'}), (trip:Trip {name: '$name2', departure: '$departure', destination: '$destination', time: '$time', numberOfPeople: '$numberOfPeople'})
    CREATE (user)-[:USED]->(trip)
""".trimIndent()
                    session.writeTransaction {
                        it.run(createQuery)
                        it.run(createRelationshipQuery)
                    }
                } finally {
                    session.close()
                    driver.close()
                }
            } catch (e: ClientException) {
            }
        }.start()
    }

    fun getAllTripsFromNeo4j(callback: (List<Trip>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val driver: Driver = GraphDatabase.driver(
                "neo4j+ssc://575bedd2.databases.neo4j.io:7687",
                AuthTokens.basic("neo4j", "NDevwfL2okljy02sx25un68ggZK1tu8_aB3Y8z8WgUU")
            )
            val session = driver.session()
            val trips = mutableListOf<Trip>()
            try {
                val query = """
                MATCH (trip:Trip)
                RETURN trip.name AS name, trip.departure AS departure, trip.destination AS destination, trip.time AS time, trip.numberOfPeople AS numberOfPeople
            """.trimIndent()

                val result = session.run(query)

                while (result.hasNext()) {
                    val record = result.next()
                    val name2 = record.get("name").asString()
                    val departure = record.get("departure").asString()
                    val destination = record.get("destination").asString()
                    val time = record.get("time").asString()
                    val numberOfPeople = record.get("numberOfPeople").asString()

                    val trip = Trip(name2,departure, destination, time, numberOfPeople.toString())
                    trips.add(trip)
                }
            } finally {
                session.close()
                driver.close()
            }
            callback(trips)
        }
    }



    @Composable
    fun TripList(trips: List<Trip>) {
        LazyColumn {
            items(trips) { trip ->
                TripItem(trip)
            }
        }
    }

    @Composable
    fun TripItem(trip: Trip) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    val intent = Intent(this, ChatRoom::class.java)
                    intent.putExtra("Название", trip.name2)
                    intent.putExtra("Откуда", trip.departure)
                    intent.putExtra("Куда", trip.destination)
                    intent.putExtra("Время", trip.time)
                    intent.putExtra("Количество людей", trip.numberOfPeople)
                    startActivity(intent)
                },
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Название: ${trip.name2}")
                Text(text = "Откуда: ${trip.departure}")
                Text(text = "Куда: ${trip.destination}")
                Text(text = "Время: ${trip.time}")
                Text(text = "Количество людей: ${trip.numberOfPeople}")
            }
        }
    }

    @Composable
    fun Tripd() {
        var trips by remember { mutableStateOf(emptyList<Trip>()) }
        // Загружаем данные при старте
        LaunchedEffect(Unit) {
            getAllTripsFromNeo4j { loadedTrips ->
                trips = loadedTrips
            }
        }
        // Отображение списка
        TripList(trips)
    }

}
