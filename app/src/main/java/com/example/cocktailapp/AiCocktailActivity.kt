package com.example.cocktailapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cocktailapp.ui.theme.CocktailAppTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput

class AiCocktailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CocktailAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AiCocktailScreen()
                }
            }
        }
    }
}

data class AiCocktail(
    val name: String,
    val time: String,
    val ingredients: String,
    val preparation: String
)

fun parseAiResponse(text: String): AiCocktail {
    val nameRegex = Regex("""^(.*?)\s*\n\s*\*\*Czas przygotowania:""", RegexOption.DOT_MATCHES_ALL)
    val timeRegex = Regex("""\*\*Czas przygotowania:\*\*\s*(.+)""")
    val ingredientsRegex = Regex("""\*\*Składniki:\*\*\s*(.*?)\n\n""", RegexOption.DOT_MATCHES_ALL)
    val preparationRegex = Regex("""\*\*Przygotowanie:\*\*\s*(.+)""", RegexOption.DOT_MATCHES_ALL)

    val name = nameRegex.find(text)?.groupValues?.get(1)?.trim() ?: "Bez nazwy"
    val time = timeRegex.find(text)?.groupValues?.get(1)?.trim() ?: "Nieznany"
    val ingredients = ingredientsRegex.find(text)?.groupValues?.get(1)?.trim() ?: "Brak składników"
    val preparation = preparationRegex.find(text)?.groupValues?.get(1)?.trim() ?: "Brak instrukcji"

    return AiCocktail(name, time, ingredients, preparation)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCocktailScreen(viewModel: CocktailAiViewModel = viewModel()) {
    var input by rememberSaveable { mutableStateOf("") }
    val aiCocktail = parseAiResponse(viewModel.result.value)

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.connectionStatus.collectAsState(initial = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 40) { // przesunięcie w prawo
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Barman AI🍹") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        val context = LocalContext.current
                        IconButton(onClick = {
                            (context as? ComponentActivity)?.finish()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤖 Porozmawiaj z Barmanem AI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Wpisz składniki, które masz pod ręką, a nasz barman zaproponuje Ci wyjątkowy koktajl!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Np. wódka, limonka, sok jabłkowy") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                if (!isConnected) {
                    Text(
                        text = "⚠️ Brak połączenia z internetem",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                Button(
                    onClick = { viewModel.generateCocktail(input) },
                    enabled = input.isNotBlank() && !viewModel.loading.value && isConnected,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Generuj koktajl 🍸")
                }

                Spacer(modifier = Modifier.height(32.dp))

                when {
                    viewModel.loading.value -> {
                        CircularProgressIndicator()
                    }

                    viewModel.result.value.isNotBlank() -> {
                        Text(
                            text = "🍹 Propozycja Barmana AI:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        AiCard(title = "🍸 Nazwa koktajlu", content = aiCocktail.name)
                        Spacer(modifier = Modifier.height(12.dp))

                        AiCard(title = "⏳ Czas przygotowania", content = aiCocktail.time)
                        Spacer(modifier = Modifier.height(12.dp))

                        val formattedIngredients = aiCocktail.ingredients
                            .split("*")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .joinToString(", ")

                        AiCard(title = "🍋 Składniki", content = formattedIngredients)
                        Spacer(modifier = Modifier.height(12.dp))

                        AiCard(title = "🍹 Sposób przygotowania", content = aiCocktail.preparation)
                    }

                    viewModel.error.value != null -> {
                        Text(
                            text = "❗ Wystąpił błąd: ${viewModel.error.value}. Sprawdź połączenie z internetem.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AiCard(title: String, content: String) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content.replace("**", ""), style = MaterialTheme.typography.bodyLarge)
        }
    }
}



