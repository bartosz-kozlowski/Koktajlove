package com.example.cocktailapp

import CocktailViewModel
import androidx.compose.ui.Alignment
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cocktailapp.ui.theme.CocktailAppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.lerp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.TabDefaults

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CocktailAppTheme {
                AnonymousLogin()
                val viewModel: CocktailViewModel = viewModel()
                CocktailAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun isTablet(): Boolean {
    val context = LocalContext.current
    return (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

@Composable
fun AnonymousLogin() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Toast.makeText(context, "Zalogowano", Toast.LENGTH_SHORT).show()
                    } else {
                        //Toast.makeText(context, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

@Composable
fun CocktailAppRoot(viewModel: CocktailViewModel) {
    val context = LocalContext.current
    if (isTablet()) {
        CocktailTabbedScreen(viewModel = viewModel, isTablet = true) { cocktail ->
            viewModel.loadCocktail(cocktail.id)
        }
    } else {
        Log.d("Telefon", "telefon")
        CocktailTabbedScreen(viewModel = viewModel) { cocktail ->
            val intent = Intent(context, CocktailDetailActivity::class.java).apply {
                putExtra("name", cocktail.name)
                putExtra("ingredients", cocktail.ingredients)
                putExtra("preparation", cocktail.preparation)
                putExtra("imageUrl", cocktail.imageUrl)
                putExtra("isAlcoholic", cocktail.isAlcoholic)
                putExtra("isFavorite", cocktail.isFavorite)
                putExtra("id", cocktail.id)
                putExtra("preparationTime", cocktail.preparationTime)
            }
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailTabbedScreen(
    viewModel: CocktailViewModel,
    isTablet: Boolean = false,
    onCocktailClick: (Cocktail) -> Unit
) {
    val scope = rememberCoroutineScope()
    val timerViewModel: TimerViewModel = viewModel()

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 4 }
    )

    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectedCocktail by viewModel.selectedCocktail.collectAsState()
    val tabs = listOf("Wszystkie", "Alkoholowe", "Bezalkoholowe", "Ulubione")

    LaunchedEffect(pagerState, isTablet) {
        //if (!isTablet) {
            snapshotFlow { pagerState.currentPage }
                .collect { page ->
                    selectedTabIndex = page
                }
        //}
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Kategorie",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                tabs.forEachIndexed { index, title ->
                    NavigationDrawerItem(
                        label = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = {
                            scope.launch {
                                focusManager.clearFocus()
                                selectedTabIndex = index
                                pagerState.scrollToPage(index)
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        }
    ) {
        // <<< Klikający BOX na całość
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Column {
                        TopAppBar(
                            title = { Text("Koktajlove 🍹") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Szukaj po nazwie lub opisie") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            singleLine = true
                        )
                    }
                },
                floatingActionButton = {
                    if (isTablet && selectedCocktail != null) {
                        val context = LocalContext.current
                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:")
                                    putExtra("sms_body", "Składniki: ${selectedCocktail!!.ingredients}")
                                }
                                context.startActivity(intent)
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Wyślij SMS")
                        }
                    }
                }
            ) { innerPadding ->
                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        // Lista koktajli po lewej
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            val allCocktails = viewModel.cocktails.collectAsState().value
                            val favorites by viewModel.favorites.collectAsState()

                            val filteredCocktails = allCocktails.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.preparation.contains(searchQuery, ignoreCase = true)
                            }
                            val cocktails = when (selectedTabIndex) {
                                0 -> filteredCocktails
                                1 -> filteredCocktails.filter { it.isAlcoholic }
                                2 -> filteredCocktails.filter { !it.isAlcoholic }
                                3 -> filteredCocktails.filter { favorites.contains(it.id) }
                                else -> emptyList()
                            }

                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        text = { Text(title) },
                                        selected = selectedTabIndex == index,
                                        onClick = {
                                            focusManager.clearFocus()
                                            selectedTabIndex = index
                                            //if (!isTablet) {
                                                scope.launch { pagerState.scrollToPage(index) }
                                           // }
                                        },
                                        selectedContentColor = MaterialTheme.colorScheme.onPrimary, // Kolor dla aktywnej zakładki
                                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Kolor dla nieaktywnej zakładki
                                    )
                                }
                            }
                            HorizontalPager(state = pagerState) { page ->
                                val cocktails = when (page) {
                                    0 -> filteredCocktails
                                    1 -> filteredCocktails.filter { it.isAlcoholic }
                                    2 -> filteredCocktails.filter { !it.isAlcoholic }
                                    3 -> filteredCocktails.filter { favorites.contains(it.id) }
                                    else -> emptyList()
                                }

                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(cocktails) { cocktail ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    focusManager.clearFocus()
                                                    onCocktailClick(cocktail) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Column {
                                                AsyncImage(
                                                    model = cocktail.imageUrl,
                                                    contentDescription = cocktail.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(150.dp)
                                                )
                                                Text(
                                                    text = cocktail.name,
                                                    modifier = Modifier.padding(12.dp),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Separator
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Szczegóły koktajlu po prawej
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                        ) {
                            selectedCocktail?.let { cocktail ->
                                TabletCocktailDetailScreen(
                                    cocktail = cocktail,
                                    timerViewModel = timerViewModel,
                                    viewModel = viewModel,
                                    onToggleFavorite = { updatedCocktail ->
                                        viewModel.toggleFavorite(updatedCocktail)
                                    }
                                )
                            } ?: LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                item {
                                    Text(
                                        "\uD83C\uDF78 Gotowy na pysznego drinka? Wybierz z menu! \uD83C\uDF4D",
                                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 2.sp),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Tryb telefonu - swipe
                    Column(modifier = Modifier.padding(innerPadding)) {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    text = { Text(title) },
                                    selected = selectedTabIndex == index,
                                    onClick = {
                                        focusManager.clearFocus()
                                        selectedTabIndex = index
                                        scope.launch { pagerState.scrollToPage(index) }
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onPrimary, // Kolor dla aktywnej zakładki
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Kolor dla nieaktywnej zakładki
                                )
                            }
                        }
                        HorizontalPager(state = pagerState) { page ->
                            val allCocktails = viewModel.cocktails.collectAsState().value
                            val favorites by viewModel.favorites.collectAsState()

                            val filteredCocktails = allCocktails.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.preparation.contains(searchQuery, ignoreCase = true)
                            }
                            val cocktails = when (page) {
                                0 -> filteredCocktails
                                1 -> filteredCocktails.filter { it.isAlcoholic }
                                2 -> filteredCocktails.filter { !it.isAlcoholic }
                                3 -> filteredCocktails.filter { favorites.contains(it.id) }
                                else -> emptyList()
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cocktails) { cocktail ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                focusManager.clearFocus()
                                                viewModel.loadCocktail(cocktail.id)
                                                onCocktailClick(cocktail)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = cocktail.imageUrl,
                                                contentDescription = cocktail.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                            )
                                            Text(
                                                text = cocktail.name,
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.titleMedium
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletCocktailDetailScreen(
    cocktail: Cocktail,
    timerViewModel: TimerViewModel,
    viewModel: CocktailViewModel,
    onToggleFavorite: (Cocktail) -> Unit
) {
    val context = LocalContext.current
    var previousDrinkId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cocktail.id) {
        previousDrinkId?.let { oldId ->
            if (oldId != cocktail.id) {
                timerViewModel.pauseTimer(oldId)
            }
        }
        timerViewModel.loadDurationForDrink(cocktail.id, cocktail.preparationTime)
        previousDrinkId = cocktail.id
    }

    val scrollState = rememberScrollState()
    val expandedHeight = 240.dp
    val collapsedHeight = 56.dp
    val density = LocalContext.current.resources.displayMetrics.density
    val collapseRangePx = expandedHeight.toPx(density) - collapsedHeight.toPx(density)
    val scrollOffset = scrollState.value.toFloat().coerceAtMost(collapseRangePx)
    val collapseFraction = scrollOffset / collapseRangePx
    val currentHeaderHeight = lerp(expandedHeight, collapsedHeight, collapseFraction)

    val favorites by viewModel.favorites.collectAsState()
    var isFavorite by remember(cocktail.id, favorites) {
        mutableStateOf(favorites.contains(cocktail.id))
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(top = expandedHeight)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Alkoholowy / Bezalkoholowy Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (cocktail.isAlcoholic) Color(0xFFFF7043) else Color(0xFF81C784) // Pomarańczowy dla alkoholowego, zielony dla bezalkoholowego
                    ),
                    elevation = CardDefaults.cardElevation(4.dp) // Cień dla głębi
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start // Wyrównanie do lewej
                    ) {
                        Text(
                            text = if (cocktail.isAlcoholic) "Alkoholowy 🍹" else "Bezalkoholowy 🚫", // Tekst zmienia się w zależności od napoju
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (cocktail.isAlcoholic) Color.White else Color.Black, // Kolor tekstu: biały dla alkoholowego, czarny dla bezalkoholowego
                                fontWeight = FontWeight.Bold, // Pogrubienie tekstu
                                fontSize = 20.sp // Zwiększenie czcionki
                            )
                        )
                    }
                }
                // Czas przygotowania Card
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
                            text = "${cocktail.preparationTime} min", // Wyświetlenie czasu przygotowania
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Składniki Card
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
                        Text(cocktail.ingredients, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Przygotowanie Card
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
                        Text(cocktail.preparation, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Timer Card
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
                        TimerScreen(timerViewModel, drinkId = cocktail.id, preparationTime = cocktail.preparationTime)
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
                    model = cocktail.imageUrl,
                    contentDescription = cocktail.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = cocktail.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newFavoriteState = !favorites.contains(cocktail.id)
                            isFavorite = newFavoriteState
                            onToggleFavorite(cocktail.copy(isFavorite = newFavoriteState))
                            Toast.makeText(
                                context,
                                if (newFavoriteState) "Dodano do ulubionych" else "Usunięto z ulubionych",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
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
