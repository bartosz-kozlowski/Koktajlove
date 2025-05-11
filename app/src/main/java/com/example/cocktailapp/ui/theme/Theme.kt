package com.example.cocktailapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.cocktailapp.ui.theme.*

/*
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
*/
/*
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    surfaceVariant = PurpleGrey80,
    onSurface = Black,
    onTertiary = Red,
    surface = Creme
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
 */
/*
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color.White,
    //secondary = Color(0xFFFFC107),
    onSecondary = Color.Black,
    background = Color(0xFFFFF4DF),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFFFCD90 ),
    outline = Color(0xFF7B7B7B)
)
 */

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary, //glowny kolor aplikacji
    onPrimary = Color.White, //kolor na primary
    secondary = OrangeSec,
    onSecondary = Color.Black,
    background = LightBackground, //tło aplikacji
    onBackground = DarkText, // na tle aplikacji
    surface = LightSurface, //kolor tła kart
    onSurface = DarkText, //kolor na kartach
    //surfaceVariant = Color(0xFFFFCD90),
    outline = OutlineGray //granice/obramowania
)
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryOrange,
    onPrimary = Color.White,
    secondary = Mustard,
    //onSecondary = Color.Black,
    background = DarkBackground, // tło aplikacji
    onBackground = LightTextOnDark,    //na tle
    surface = DarkSurface, // kolor tła kart (szczegoly)
    onSurface = LightTextOnSurface, //kolor na kartach (szczegoly)
    //surfaceVariant = Color(0xFF5C4033),
    outline = OutlineBeige //granice
)


@Composable
fun CocktailAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = shapes
    )
}