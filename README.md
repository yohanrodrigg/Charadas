# Charadas (Android nativo, Kotlin + Jetpack Compose)

Juego de charadas estilo "Heads Up": pones el teléfono en la frente, tus amigos
hacen mímica y tú adivinas la película antes de que se acabe el tiempo.

## Cómo abrirlo

1. Abre **Android Studio** (Koala o más reciente) → *Open* → selecciona la carpeta `CharadasApp`.
2. Espera a que termine el *Gradle Sync* (Android Studio descarga el wrapper y las dependencias).
3. Ejecuta en un teléfono real o emulador (`Run ▶`). Requiere Android 7.0 (API 24) o superior.

## Cómo se juega

- Elige el tema (por ahora solo **Películas**) y pulsa **¡Jugar!**.
- Cuenta regresiva de 3 segundos y comienza la ronda de 60 segundos.
- **Inclina hacia abajo** = acertaste. **Inclina hacia arriba** = pasar.
- Los botones de la pantalla hacen lo mismo (útil en el emulador, que no tiene acelerómetro por defecto).
- Al final ves tu puntaje y el resumen de palabras.

## Estructura

```
app/src/main/java/com/example/charadas/
├── MainActivity.kt     # Activity y navegación entre pantallas
├── GameViewModel.kt    # Estado, temporizador, puntaje y mazo de palabras
├── Categories.kt       # Temas y listas de palabras (solo Películas)
├── Screens.kt          # Menú, cuenta regresiva, juego, resultados y detección de inclinación
└── Theme.kt            # Colores y tema
```

## Agregar más temas

En `Categories.kt` crea otra `Category(...)` y agrégala a `Categories.all`.
El menú la mostrará automáticamente.

## Ajustes rápidos

- Duración de la ronda: `ROUND_SECONDS` en `GameViewModel.kt`.
- Sensibilidad de la inclinación: `NEUTRAL_LIMIT` y `TILT_LIMIT` al final de la sección de partida en `Screens.kt`.
