package com.example.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.presentation.ui.theme.PresentationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

//this is our mock fetching No need to understand
suspend fun fetchDataFromServer(pin: String): DataState {
    delay(timeMillis = 1000)
    if (pin == "1234")
        return Success(secret = "\uD83D\uDE09") //send some secret data
    return Error
}

interface DataState

class Success(val secret: String) : DataState
object Error : DataState
object Loading : DataState
object EnterCode : DataState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PresentationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    //this represents data state
                    val UIState: MutableState<DataState> = remember {
                        mutableStateOf(EnterCode)
                    }
                    var code by remember {
                        mutableStateOf("")
                    }
                    val onCodeChane = { it: String ->
                        code = it
                    }
                    val scope = rememberCoroutineScope()

                    when (UIState.value) { //switches Screeens by our State
                        is EnterCode -> InputScreen(
                            UIState = UIState,
                            code = code,
                            onCodeChange = onCodeChane
                        )

                        is Loading -> {
                            LoadingScreen()
                            LaunchedEffect(key1 = Unit) {
                                UIState.value =
                                    fetchDataFromServer(code) //fetches data from s
                            }
                        }
                        is Success -> SuccessScreen((UIState.value as Success).secret)
                        is Error -> FailScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(UIState: MutableState<DataState>, code: String, onCodeChange: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "To show secret data you have to enter pin",
            style = MaterialTheme.typography.h6
        )
        OutlinedTextField(
            value = code,
            label = { Text(text = "Pin") },
            onValueChange = onCodeChange,
            keyboardActions = KeyboardActions(
                onDone = {
                    UIState.value = Loading
                }
            ),
            singleLine = true
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FailScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Fail Screen", color = Color.Red, style = MaterialTheme.typography.h1)
    }
}

@Composable
fun SuccessScreen(secret: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(secret, style = MaterialTheme.typography.h1)
    }
}


