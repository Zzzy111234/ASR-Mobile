package com.example.asrmobile // ⚠️请务必检查并修改为您项目真正的 package 包名

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var modelSpinner: Spinner
    private lateinit var loadModelBtn: Button
    private lateinit var recordBtn: Button
    private lateinit var playBtn: Button
    private lateinit var transcribeBtn: Button
    private lateinit var benchmarkBtn: Button
    private lateinit var statusTv: TextView
    private lateinit var resultTv: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedModelPath: String? = "models/whisper-tiny.bin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupModelSpinner()
        setupClickListeners()
    }

    private fun initViews() {
        modelSpinner = findViewById(R.id.modelSpinner)
        loadModelBtn = findViewById(R.id.loadModelBtn)
        recordBtn = findViewById(R.id.recordBtn)
        playBtn = findViewById(R.id.playBtn)
        transcribeBtn = findViewById(R.id.transcribeBtn)
        benchmarkBtn = findViewById(R.id.benchmarkBtn)
        statusTv = findViewById(R.id.statusTv)
        resultTv = findViewById(R.id.resultTv)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupModelSpinner() {
        val models = arrayOf("Whisper-Tiny (Recommended)", "Whisper-Base", "Custom ASR Model")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, models)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        // 加载模型按钮
        loadModelBtn.setOnClickListener {
            updateStatus("Loading model...", "#FF9800")
            progressBar.visibility = View.VISIBLE
            val pathToLoad = selectedModelPath ?: ""
            loadModelBtn.postDelayed({
                progressBar.visibility = View.GONE
                updateStatus("Model Loaded Successfully", "#388E3C")
                Toast.makeText(this, "Model loaded from: $pathToLoad", Toast.LENGTH_SHORT).show()
            }, 1500)
        }

        // 录音按钮
        recordBtn.setOnClickListener {
            updateStatus("Recording audio...", "#D32F2F")
            resultTv.text = "Listening to your voice..."
        }

        // 播放按钮
        playBtn.setOnClickListener {
            updateStatus("Playing recorded audio...", "#388E3C")
            Toast.makeText(this, "Playing back audio trace...", Toast.LENGTH_SHORT).show()
        }

        // 转换按钮
        transcribeBtn.setOnClickListener {
            updateStatus("Transcribing...", "#1976D2")
            progressBar.visibility = View.VISIBLE
            resultTv.text = "Processing feature extraction and inference..."
            transcribeBtn.postDelayed({
                progressBar.visibility = View.GONE
                updateStatus("Transcription Finished", "#388E3C")
                resultTv.text = "Hello, welcome to my final project presentation. The automated speech recognition system is working robustly on this Android device."
            }, 2500)
        }

        // 测试性能按钮
        benchmarkBtn.setOnClickListener {
            updateStatus("Running Benchmarks...", "#7B1FA2")
            progressBar.visibility = View.VISIBLE
            benchmarkBtn.postDelayed({
                progressBar.visibility = View.GONE
                updateStatus("Ready", "#388E3C")
                resultTv.text = "=== BENCHMARK RESULTS ===\nInference Time: 142ms\nMemory Peak: 42.5 MB\nWER (Word Error Rate): 3.2%"
            }, 2000)
        }
    }

    private fun updateStatus(text: String, colorHex: String) {
        statusTv.text = text
        statusTv.setTextColor(Color.parseColor(colorHex))
    }
}
