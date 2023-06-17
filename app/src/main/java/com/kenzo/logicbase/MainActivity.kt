package com.kenzo.logicbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kenzo.logicbase.ui.theme.LogicBaseTheme
import kotlin.random.Random

const val DataSize = 3
val LogicData: List<List<Boolean>> = List(DataSize) { List(DataSize) { Random.nextInt(0, 4) > 1 } }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogicBaseTheme {
                GameScreen()
            }
        }
    }
}