package com.example.cocktailapp

import CocktailViewModel
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cocktailapp.ui.theme.CocktailAppTheme
fun Dp.toPx(density: Float): Float = this.value * density
fun androidx.compose.ui.unit.Dp.toPx(): Float = this.value * Resources.getSystem().displayMetrics.density

class CocktailDetailActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels()
    private val cocktailViewModel: CocktailViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("id") ?: ""
        val context = this

        setContent {
            CocktailAppTheme {
                val scrollState = rememberScrollState()
                val expandedHeight = 240.dp
                val collapsedHeight = 76.dp
                val collapseRangePx = expandedHeight.toPx() - collapsedHeight.toPx()
                val scrollOffset = scrollState.value.toFloat().coerceAtMost(collapseRangePx)
                val collapseFraction = scrollOffset / collapseRangePx
                val currentHeaderHeight = lerp(expandedHeight, collapsedHeight, collapseFraction)

                val cocktailState = cocktailViewModel.selectedCocktail.collectAsState()
                val favorites = cocktailViewModel.favorites.collectAsState()

                LaunchedEffect(Unit) { cocktailViewModel.loadCocktail(id) }
                //LaunchedEffect(id) { timerViewModel.loadDurationForDrink(id) }
                val selectedCocktail by cocktailViewModel.selectedCocktail.collectAsState()

                LaunchedEffect(selectedCocktail?.id) {
                    selectedCocktail?.let { cocktail ->
                        timerViewModel.loadDurationForDrink(cocktail.id, cocktail.preparationTime)
                    }
                }


                cocktailState.value?.let { currentCocktail ->
                    var isFavorite by remember(currentCocktail.id) { mutableStateOf(false) }

                    LaunchedEffect(favorites.value) {
                        isFavorite = favorites.value.contains(currentCocktail.id)
                    }

                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:")
                                        putExtra("sms_body", "Składniki: ${currentCocktail.ingredients}")
                                    }
                                    context.startActivity(intent)
                                },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Wyślij SMS")
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .verticalScroll(scrollState)
                                    .padding(top = expandedHeight)
                                    .fillMaxSize()
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (currentCocktail.isAlcoholic) Color(0xFFFF7043) else Color(0xFF81C784)
                                        ),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start // Wyrównanie do lewej
                                        ) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (currentCocktail.isAlcoholic) "Alkoholowy 🍹" else "Bezalkoholowy 🚫",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = if (currentCocktail.isAlcoholic) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                            )
                                        }
                                    }

                                    //Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Czas przygotowania ⏳", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "${currentCocktail.preparationTime} min",  // Convert the number to string with " min"
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Składniki 🍋", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(currentCocktail.ingredients, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                    //Text(text = "Składniki: ${currentCocktail.ingredients}", style = MaterialTheme.typography.bodyLarge)
                                    //Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Przygotowanie 🍹", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(currentCocktail.preparation, style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                    //Text(text = "Przygotowanie: ${currentCocktail.preparation}", style = MaterialTheme.typography.bodyLarge)
                                    //Spacer(modifier = Modifier.height(16.dp))
                                    //TimerScreen(timerViewModel, drinkId = currentCocktail.id)
                                    //Spacer(modifier = Modifier.height(400.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Odmierz czas przygotowania ⏱️", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TimerScreen(timerViewModel, drinkId = currentCocktail.id, preparationTime = currentCocktail.preparationTime)
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(currentHeaderHeight)
                                    .align(Alignment.TopCenter)
                            ) {
                                AsyncImage(
                                    model = currentCocktail.imageUrl,
                                    contentDescription = currentCocktail.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.5f)
                                                )
                                            )
                                        )
                                )
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Powrót",
                                        tint = Color.White
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        //.fillMaxSize()
                                        //.padding(horizontal = 16.dp, vertical = 8.dp),
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                        .padding(start = 72.dp, end = 16.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentCocktail.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        cocktailViewModel.toggleFavorite(currentCocktail)
                                        Toast.makeText(
                                            context,
                                            if (isFavorite) "Usunięto z ulubionych" else "Dodano do ulubionych",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Ulubione",
                                            tint = if (isFavorite) Color.Red else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
