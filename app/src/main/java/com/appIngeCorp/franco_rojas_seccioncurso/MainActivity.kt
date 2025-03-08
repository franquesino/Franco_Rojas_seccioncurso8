package com.appIngeCorp.franco_rojas_seccioncurso

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Solicitar permiso para notificaciones en Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val inputNumber = findViewById<EditText>(R.id.inputNumber)
        val btnConvertToBinary = findViewById<Button>(R.id.btnConvertToBinary)
        val btnConvertToDecimal = findViewById<Button>(R.id.btnConvertToDecimal)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnSubtract = findViewById<Button>(R.id.btnSubtract)
        val resultView = findViewById<TextView>(R.id.resultView)

        btnConvertToBinary.setOnClickListener {
            executeWorker("toBinary", inputNumber.text.toString(), resultView)
        }

        btnConvertToDecimal.setOnClickListener {
            executeWorker("toDecimal", inputNumber.text.toString(), resultView)
        }

        btnAdd.setOnClickListener {
            executeWorker("add", inputNumber.text.toString(), resultView)
        }

        btnSubtract.setOnClickListener {
            executeWorker("subtract", inputNumber.text.toString(), resultView)
        }
    }

    private fun executeWorker(operation: String, number: String, resultView: TextView) {
        val data = Data.Builder()
            .putString("operation", operation)
            .putString("number", number)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(CalculatorWorker::class.java)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    val result = workInfo.outputData.getString("result")
                    resultView.text = result ?: "Error en la operación"
                }
            }
    }
}
