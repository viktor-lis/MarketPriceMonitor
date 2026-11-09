package com.example.marketpricemonitor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(ThemeSettings.getMode(context)) }

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MaterialTheme(
                colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen(
                        themeMode = themeMode,
                        onThemeChange = { newMode ->
                            themeMode = newMode
                            ThemeSettings.setMode(context, newMode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    vm: PriceViewModel = viewModel()
) {
    var tab by remember { mutableStateOf(0) }
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        // Убираем стандартные отступы Scaffold, чтобы управлять ими сами
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(

            ) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                    label = { Text("Поиск") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Избранное") },
                    label = { Text("Избранное") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                    label = { Text("Настройки") }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues) // Место под нашу нижнюю панель
                .statusBarsPadding()    // Место под верхний статус-бар
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            when (tab) {
                0 -> SearchContent(vm, state)
                1 -> FavoritesContent()
                2 -> SettingsContent(themeMode, onThemeChange)
            }
        }
    }
    }


@Composable
fun SearchContent(vm: PriceViewModel, state: UiState) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                label = { Text("Что ищем на WB") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.search() }),
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.clear() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { vm.search() },
                modifier = Modifier
                    .height(56.dp)
                    .offset(y = 7.dp),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Найти")
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error
            )

            else -> Column {
                if (state.items.isEmpty()) {
                    // Пустое состояние поиска
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (state.query.isEmpty()) "Введите запрос для поиска"
                                else "По запросу «${state.query}» ничего не найдено",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Есть товары: сортировка и список
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.sortOrder == SortOrder.PRICE_ASC,
                            onClick = { vm.setSortOrder(SortOrder.PRICE_ASC) },
                            label = { Text("Дешёвые") }
                        )
                        FilterChip(
                            selected = state.sortOrder == SortOrder.PRICE_DESC,
                            onClick = { vm.setSortOrder(SortOrder.PRICE_DESC) },
                            label = { Text("Дорогие") }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val sortedItems = when (state.sortOrder) {
                        SortOrder.PRICE_ASC -> state.items.sortedBy { it.priceRub }
                        SortOrder.PRICE_DESC -> state.items.sortedByDescending { it.priceRub }
                        SortOrder.NONE -> state.items
                    }

                    // ВОТ ЗДЕСЬ ДОЛЖЕН БЫТЬ ПРАВИЛЬНЫЙ СПИСОК
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(sortedItems) { product ->
                            ProductCard(product = product)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun FavoritesContent() {
    val context = LocalContext.current
    var favorites by remember { mutableStateOf(Favorites.load(context)) }

    LaunchedEffect(Unit) {
        favorites = Favorites.load(context)
    }

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "В избранном пока пусто",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Нажмите на сердечко в поиске, чтобы сохранить товар",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(favorites) { product ->
                ProductCard(
                    product = product,
                    onFavoriteChanged = { favorites = Favorites.load(context) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onFavoriteChanged: () -> Unit = {}) {
    val context = LocalContext.current
    var isFav by remember { mutableStateOf(Favorites.isFavorite(context, product.id)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val url = "https://www.wildberries.ru/catalog/${product.id}/detail.aspx"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Цена: ${"%.2f".format(product.priceRub)} ₽",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Артикул: ${product.id}", style = MaterialTheme.typography.bodySmall)
                Text("Продавец: ${product.supplier ?: "—"}")
                Text("Рейтинг: ${product.rating ?: "—"} | Отзывов: ${product.feedbacks ?: 0}")
            }
            IconButton(onClick = {
                isFav = Favorites.toggle(context, product)
                onFavoriteChanged()
            }) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "В избранное",
                    tint = if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SettingsContent(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Column {
        Text("Тема оформления", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        ThemeMode.values().forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = themeMode == mode,
                    onClick = { onThemeChange(mode) }
                )
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> "Как в системе"
                        ThemeMode.LIGHT -> "Светлая"
                        ThemeMode.DARK -> "Тёмная"
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}