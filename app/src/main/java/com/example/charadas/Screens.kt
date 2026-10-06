package com.example.charadas

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// ---------------------------------------------------------------------------
// Menú
// ---------------------------------------------------------------------------

@Composable
fun MenuScreen(onPlay: (Category) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val categories = Categories.all

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Columna izquierda: título e instrucciones
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎭 Charadas", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = Accent)
            Spacer(Modifier.height(12.dp))
            Text(
                "Pon el teléfono en tu frente, con la pantalla hacia tus amigos. " +
                    "Ellos hacen mímica para que adivines.",
                color = TextLight,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))
            Text("⬇️  Inclina hacia abajo: ¡acertaste!", color = TextMuted, fontSize = 15.sp)
            Text("⬆️  Inclina hacia arriba: pasar", color = TextMuted, fontSize = 15.sp)
            Text("También puedes usar los botones.", color = TextMuted, fontSize = 15.sp)
        }

        // Columna derecha: categorías y botón jugar
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Elige un tema", color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            categories.forEachIndexed { index, category ->
                val isSelected = index == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NightCard)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Accent else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selected = index }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.emoji, fontSize = 32.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(category.name, color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${category.words.size} palabras", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onPlay(categories[selected]) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("¡Jugar!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cuenta regresiva
// ---------------------------------------------------------------------------

@Composable
fun CountdownScreen(state: GameUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ponte el teléfono en la frente", color = TextMuted, fontSize = 20.sp)
        Text(
            "${state.countdown}",
            color = Accent,
            fontSize = 120.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ---------------------------------------------------------------------------
// Partida
// ---------------------------------------------------------------------------

@Composable
fun PlayingScreen(state: GameUiState, onAnswer: (Boolean) -> Unit) {
    val haptic = LocalHapticFeedback.current

    val answerCorrect = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAnswer(true)
    }
    val answerPass = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAnswer(false)
    }

    TiltEffect(onCorrect = answerCorrect, onPass = answerPass)

    val background = when (state.feedback) {
        Feedback.CORRECT -> Correct
        Feedback.PASS -> Pass
        Feedback.NONE -> Night
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        // Puntaje (arriba izquierda)
        Text(
            "✔ ${state.score}",
            modifier = Modifier.align(Alignment.TopStart),
            color = TextLight,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Tiempo (arriba derecha)
        Text(
            "${state.timeLeft}",
            modifier = Modifier.align(Alignment.TopEnd),
            color = if (state.timeLeft <= 10) Pass else Accent,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )

        // Palabra / feedback (centro)
        Text(
            text = when (state.feedback) {
                Feedback.CORRECT -> "¡Correcto!"
                Feedback.PASS -> "Pasada"
                Feedback.NONE -> state.currentWord
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp),
            color = TextLight,
            fontSize = 56.sp,
            lineHeight = 62.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        // Botones de respaldo (abajo)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = answerPass,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("⬆ Pasar", color = TextLight, fontSize = 16.sp)
            }
            Button(
                onClick = answerCorrect,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Correct, contentColor = Color.White)
            ) {
                Text("⬇ ¡Acerté!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Detecta la inclinación del teléfono con el acelerómetro.
 *
 * Con el teléfono en la frente (pantalla hacia delante) el eje Z está cerca de 0.
 *  - Inclinar hacia abajo (pantalla al suelo)  -> Z muy negativo -> acierto.
 *  - Inclinar hacia arriba (pantalla al techo) -> Z muy positivo -> pasar.
 * Hay que volver a la posición neutra antes de que se detecte otro gesto.
 */
@Composable
private fun TiltEffect(onCorrect: () -> Unit, onPass: () -> Unit) {
    val context = LocalContext.current
    val currentOnCorrect by rememberUpdatedState(onCorrect)
    val currentOnPass by rememberUpdatedState(onPass)

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var armed = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val z = event.values[2]
                if (abs(z) < NEUTRAL_LIMIT) {
                    armed = true
                } else if (armed) {
                    if (z < -TILT_LIMIT) {
                        armed = false
                        currentOnCorrect()
                    } else if (z > TILT_LIMIT) {
                        armed = false
                        currentOnPass()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }
}

private const val NEUTRAL_LIMIT = 3f
private const val TILT_LIMIT = 6f

// ---------------------------------------------------------------------------
// Resultados
// ---------------------------------------------------------------------------

@Composable
fun ResultsScreen(state: GameUiState, onPlayAgain: () -> Unit, onMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¡Tiempo!", color = TextMuted, fontSize = 20.sp)
            Text(
                "${state.score}",
                color = Accent,
                fontSize = 96.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                if (state.score == 1) "acierto" else "aciertos",
                color = TextLight,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Jugar de nuevo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Menú", color = TextLight, fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Resumen", color = TextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            if (state.results.isEmpty()) {
                Text("No jugaste ninguna palabra.", color = TextLight, fontSize = 16.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.results) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NightCard)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (result.correct) "✔" else "✖",
                                color = if (result.correct) Correct else Pass,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(result.word, color = TextLight, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
