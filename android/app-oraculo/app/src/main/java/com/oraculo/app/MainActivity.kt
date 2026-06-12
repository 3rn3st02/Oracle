package com.oraculo.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.oraculo.app.data.repository.OraculoRepository
import android.util.Log





class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val repo = OraculoRepository()

        lifecycleScope.launch {
            val healthResult = repo.health()

            healthResult
                .onSuccess { message ->
                    Log.d("ORACULO_API", "HEALTH OK: $message")
                }
                .onFailure { error ->
                    Log.e("ORACULO_API", "HEALTH ERROR: ${error.message}", error)
                }

            val askResult = repo.ask("¿Qué es ORACULO?")

            askResult
                .onSuccess { answer ->
                    Log.d("ORACULO_API", "ASK OK: $answer")
                }
                .onFailure { error ->
                    Log.e("ORACULO_API", "ASK ERROR: ${error.message}", error)
                }
        }
    }
}
