package com.kenzo.logicbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kenzo.logicbase.ui.theme.LogicBaseTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogicBaseTheme {
                var logicData by remember { mutableStateOf(createLogicData(DataSize)) }
                GameScreen(
                    logicData = logicData,
                    nextGame = { logicData = createLogicData(DataSize) }
                )
            }
        }
    }
}

const val DataSize = 3
fun createLogicData(dataSize: Int): List<List<Boolean>> = List(dataSize) { List(dataSize) { Random.nextInt(0, 4) > 1 } }