package com.example.cocktailapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cocktailapp.ui.theme.CocktailAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SplashActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensorX by mutableStateOf(0f)
    private var sensorY by mutableStateOf(0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {
            CocktailAppTheme {
                val context = LocalContext.current
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }
                val scale = remember { Animatable(0f) }
                val rotation = remember { Animatable(0f) }
                val bgColor = remember { Animatable(Color(0xFFFFA500)) }
                val scope = rememberCoroutineScope()

                // Start animations
                LaunchedEffect(Unit) {
                    scope.launch {
                        scale.animateTo(1f, animationSpec = tween(1000))
                    }
                    scope.launch {
                        rotation.animateTo(360f, animationSpec = tween(1500))
                    }
                    scope.launch {
                        while (true) {
                            val nextColor = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                            bgColor.animateTo(nextColor, animationSpec = tween(2000))
                        }
                    }
                }

                /*LaunchedEffect(sensorX, sensorY) {
                    offsetX = sensorX * 8  // Żyroskop Y wpływa na przesunięcie X
                    offsetY = sensorY * 8  // Żyroskop X wpływa na przesunięcie Y
                }*/

                val time = remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        time.value += 0.016f
                        delay(16)
                    }
                }

                val shakeX = sin(time.value * 3f) * 5f
                val shakeY = cos(time.value * 2f) * 5f

                offsetX = sensorX * 12 + shakeX
                offsetY = sensorY * 12 + shakeY


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor.value),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.local_bar),
                        contentDescription = "Cocktail Icon",
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                            .scale(scale.value)
                            .rotate(rotation.value),
                        contentScale = ContentScale.Fit
                    )
                }

                LaunchedEffect(Unit) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        context.startActivity(Intent(context, MainActivity::class.java))
                        finish()
                    }, 3000)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            sensorX = it.values[1] // obrót w lewo/prawo
            sensorY = it.values[0] // przód/tył
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
