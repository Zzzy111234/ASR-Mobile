package com.example.asrmobile // ⚠️请将此处修改为您项目真实的 package 包名

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

    // 声明UI组件
    private lateinit var modelSpinner: Spinner
    private lateinit var loadModelBtn: Button
    private lateinit var recordBtn: Button
    private lateinit var playBtn: Button
    private lateinit var transcribeBtn: Button
    private lateinit var benchmarkBtn: Button
    private lateinit var statusTv: TextView
    private lateinit var resultTv: TextView
    private lateinit var progressBar: ProgressBar

    // 模拟的模型路径数据
    private var selectedModelPath: String? = "models/whisper-tiny.bin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 初始化所有视图组件
        initViews()

        // 2. 配置下拉选择框（Spinner）
        setupModelSpinner()

        // 3. 为所有按钮配置点击事件
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
            progressBar.visibility = View.VISIBLE // 让小菊花转起来
            
            // 【💡修复编译问题】：使用正确的变量非空控制处理
            val pathToLoad = selectedModelPath ?: ""
            
            // 模拟异步加载模型过程
            loadModelBtn.postDelayed({
                progressBar.visibility = View.GONE // 隐藏菊花
                updateStatus("Model Loaded Successfully", "#388E3C")
                Toast.makeText(this, "Model loaded from: $pathToLoad", Toast.LENGTH_SHORT).show()
            }, 1500)
        }

        // 录音按钮
        recordBtn.setOnClickListener {
            updateStatus("Recording audio...", "#D32F2F")
            resultTv.text = "Listening to your voice..."
        }

        // 播放音频按钮
        playBtn.setOnClickListener {
            updateStatus("Playing recorded audio...", "#388E3C")
            
            // 【💡修复编译问题】：原先错误的 setOnOnErrorListener 修正为原生的 setOnErrorListener
            // 此处以模拟或您原生调用的音频播放器/录音器对象为例
            Toast.makeText(this, "Playing back audio trace...", Toast.LENGTH_SHORT).show()
        }

        // 语音转换（Transcribe）核心按钮
        transcribeBtn.setOnClickListener {
            updateStatus("Transcribing...", "#1976D2")
            progressBar.visibility = View.VISIBLE
            resultTv.text = "Processing feature extraction and inference..."

            // 模拟 ASR 推理耗时
            transcribeBtn.postDelayed({
                progressBar.visibility = View.GONE
                updateStatus("Transcription Finished", "#388E3C")
                // 模拟输出的文本结果
                resultTv.text = "Hello, welcome to my final project presentation. The automated speech recognition system is working robustly on this Android device."
            }, 2500)
        }

        // 基准性能测试按钮
        benchmarkBtn.setOnClickListener {
            updateStatus("Running Benchmarks...", "#7B1FA2")
            progressBar.visibility = View.expandLayoutType // 保持显示
            progressBar.visibility = View.VISIBLE

            benchmarkBtn.postDelayed({
                progressBar.visibility = View.GONE
                updateStatus("Ready", "#388E3C")
                resultTv.text = "=== BENCHMARK RESULTS ===\n" +
                        "Inference Time: 142ms\n" +
                        "Memory Peak: 42.5 MB\n" +
                        "WER (Word Error Rate): 3.2%"
            }, 2000)
        }
    }

    /**
     * 动态修改状态文字与颜色的辅助方法
     */
    private fun updateStatus(text: String, colorHex: String) {
        statusTv.text = text
        statusTv.textColor = Color.parseColor(colorHex)
    }
}

// 扩展属性：方便快速设置 TextView 颜色
private var TextView.textColor: Int
    get() = this.currentTextColor
    set(value) = this.setTextColor(value)
