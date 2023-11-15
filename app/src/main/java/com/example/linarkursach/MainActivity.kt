package com.example.linarkursach

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException

class MainActivity : ComponentActivity(){
    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                var tabIndex = rememberSaveable { mutableStateOf(0) }
                val pagerState = rememberPagerState()
                val scope = rememberCoroutineScope()
                val tabTitles = listOf<String>("Логин", "Регистрация")
                val white = Color(0xFFA3A3A3)
                val ash7a = Color(0xFF414141)
                val pearl = Color(0xFF000000)


                Column {
                    TabRow(selectedTabIndex = tabIndex.value,
                        backgroundColor = Color.White,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .background(color = Color.Transparent),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier
                                    .pagerTabIndicatorOffset(
                                        pagerState,
                                        tabPositions
                                    )
                                    .height(0.dp)
                                    .size(0.dp)
                            )
                        }) {
                        tabTitles.forEachIndexed { index, title ->
                            val tabColor = remember {
                                Animatable(white)
                            }

                            val textColor = remember {
                                Animatable(ash7a)
                            }

                            LaunchedEffect(key1 = pagerState.currentPage == index) {
                                tabColor.animateTo(if (pagerState.currentPage == index) pearl else white)
                                textColor.animateTo(if (pagerState.currentPage == index) white else ash7a)
                            }

                            Tab(
                                selected = pagerState.currentPage == index,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        color = tabColor.value
                                    ),
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }) {
                                Text(
                                    tabTitles[index],
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = TextStyle(
                                        color = textColor.value,

                                        )
                                )
                            }

                        }
                    }
                    HorizontalPager(
                        count = tabTitles.size,
                        state = pagerState,
                    ) { tabIndex ->
                        if (tabIndex == 1){
                            Register()
                        } else {
                            Login()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    @Composable
    fun Register() {
        var email by remember { mutableStateOf("") }
        var age by remember { mutableStateOf(25) }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ФИО") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = TextFieldValue(text = age.toString()),
                onValueChange = {
                    val newValue = it.text.toIntOrNull() ?: age
                    age = newValue.coerceIn(14, 99)
                },
                label = { Text("Возраст") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердждение Пароль") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    registerUser(
                        context, email = email,
                        name = name, password = password, confirmPassword = confirmPassword,age = age.toString()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Регистрация")
            }
        }
    }

    private fun registerUser(context: Context, email: String,name: String, password: String, confirmPassword: String, age: String) {
        val auth = FirebaseAuth.getInstance()
        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && name.isNotEmpty()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            registerUser2(name, email, onSuccess = {}, onError = {}, age = age)
                            Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(context, "Регистрация не успешна", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun Login() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Логин",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { loginUser(context, email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Вход")
            }
        }
    }

    private fun loginUser(context: Context, email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Вход успешен", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(context, "Вход не успешен", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    fun registerUser2(
        fio: String,
        email: String,
        age: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val driver: Driver = GraphDatabase.driver(
                    "neo4j+ssc://575bedd2.databases.neo4j.io:7687",
                    AuthTokens.basic("neo4j", "NDevwfL2okljy02sx25un68ggZK1tu8_aB3Y8z8WgUU")
                )
                // Создаем сессию и выполняем запрос для проверки существующего пользователя
                val session: Session = driver.session()
                val checkUserQuery = "MATCH (user:User {email: '$email', fio: '$fio', age: '$age'}) RETURN user"
                val result = session.run(checkUserQuery)
                val resultList = result.list()
                // Если результат запроса пустой, то регистрируем пользователя и связываем его с комнатой
                if (resultList.isEmpty()) {
                    val createUserQuery = """
                    CREATE (user:User {
                        email: '$email',
                        fio: '$fio', 
                        age: '$age'
                    })
                """.trimIndent()
                    session.run(createUserQuery)
                    onSuccess.invoke() // Вызываем колбэк успешной регистрации
                } else {
                    onError.invoke("Пользователь с таким email уже существует.")
                }
                session.close()
                driver.close()
            } catch (e: ClientException) {
                onError.invoke("Произошла ошибка при регистрации.")
            }
        }.start()
    }

}