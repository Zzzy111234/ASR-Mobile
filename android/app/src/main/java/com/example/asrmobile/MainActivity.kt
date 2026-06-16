package com.example.asrmobile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private val modelFileManager by lazy { ModelFileManager(this) }
    private val modelRepository by lazy { ModelRepository(this) }
    private val whisperEngine by lazy { WhisperEngine() }
    private val audioRecorder by lazy { AudioRecorder(this) }
    private val benchmarkRunner by lazy { BenchmarkRunner(this, whisperEngine) }

    private lateinit var statusText: TextView
    private lateinit var transcriptText: TextView
    private lateinit var metricsText: TextView

    private var selectedModelPath: String? = null
    private var latestRecording: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 加载新布局
        setContentView(R.layout.activity_main)

        // 绑定精美组件与后端业务逻辑
        setupViewsAndListeners()
        
        requestMicrophonePermissionIfNeeded()
        updateStatus("Ready. Select a built-in model or pick a model file.")
    }

    /**
     * 连接 XML 布局与后台逻辑的桥梁
     */
    private fun setupViewsAndListeners() {
        // 1. 绑定文本和状态显示框
        statusText = findViewById(R.id.tv_status)
        transcriptText = findViewById(R.id.tv_transcript)
        metricsText = findViewById(R.id.tv_metrics)

        // 2. 绑定外部模型选择按钮
        findViewById<Button>(R.id.btn_select_model).setOnClickListener { 
            selectModelFile() 
        }

        // 3. 绑定内置轻量模型按钮
        findViewById<Button>(R.id.btn_use_bundled).setOnClickListener { 
            val builtInModel = modelRepository.getBundledModels().firstOrNull()
            if (builtInModel != null) {
                selectBundledModel(builtInModel)
            } else {
                updateStatus("No built-in models found in assets.")
            }
        }

        // 4. 绑定加载模型按钮
        findViewById<Button>(R.id.btn_load_model).setOnClickListener { 
            loadSelectedModel() 
        }

        // 5. 绑定录音按钮
        findViewById<Button>(R.id.btn_record).setOnClickListener { 
            recordShortClip() 
        }

        // 6. 绑定转换文本按钮
        findViewById<Button>(R.id.btn_transcribe).setOnClickListener { 
            transcribeLatestRecording() 
        }

        // 7. 绑定性能测试按钮
        findViewById<Button>(R.id.btn_benchmark).setOnClickListener { 
            runBenchmark() 
        }

        // 额外处理：原代码的“播放录音”功能
        findViewById<Button>(R.id.btn_play)?.setOnClickListener {
            playLatestRecording()
        }
    }

    // ══════════════════════════════════════════
    //  模型选择
    // ══════════════════════════════════════════

    private fun selectBundledModel(model: BundledModel) {
        updateStatus("Preparing ${model.displayName}...")
        selectedModelPath = null
        Thread {
            val result = runCatching { modelRepository.deployModel(model).absolutePath }
            runOnUiThread {
                result.onSuccess { path ->
                    selectedModelPath = path
                    updateStatus("Selected built-in model: ${model.displayName}")
                }.onFailure {
                    updateStatus("Failed to prepare model: ${it.message}")
                }
            }
        }.start()
    }

    private fun selectModelFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_MODEL_FILE)
    }

    @Deprecated("Deprecated in AndroidX Activity Result API, but kept simple for this teaching scaffold.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MODEL_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val path = modelFileManager.copyModelUriToAppStorage(uri).absolutePath
                selectedModelPath = path
                updateStatus("Selected external model file: $path")
            }
        }
    }

    // ══════════════════════════════════════════
    //  模型加载 / 推理 / 评测
    // ══════════════════════════════════════════

    private fun loadSelectedModel() {
        val path = selectedModelPath
        if (path == null) {
            updateStatus("No model selected. Pick a built-in model or a model file first.")
            return
        }

        updateStatus("Loading model...")
        Thread {
            val result = runCatching { whisperEngine.loadModel(path) }
            runOnUiThread {
                result.onSuccess { updateStatus("Model loaded successfully.") }
                    .onFailure { updateStatus("Model load failed: ${it.message}") }
            }
        }.start()
    }

    private fun recordShortClip() {
        if (!hasMicrophonePermission()) {
            requestMicrophonePermissionIfNeeded()
            return
        }

        updateStatus("Recording 10 seconds...")
        Thread {
            val recording = audioRecorder.recordBlocking(seconds = 10)
            latestRecording = recording
            runOnUiThread { updateStatus("Recording saved: ${recording.absolutePath}") }
        }.start()
    }

    private fun transcribeLatestRecording() {
        val recording = latestRecording
        if (recording == null) {
            updateStatus("No recording yet.")
            return
        }

        updateStatus("Transcribing ${recording.name}...")
        Thread {
            val result = runCatching { whisperEngine.transcribe(recording.absolutePath) }
            runOnUiThread {
                result.onSuccess { transcriptText.text = it }
                    .onFailure { transcriptText.text = "Transcription failed: ${it.message}" }
                updateStatus("Transcription finished.")
            }
        }.start()
    }

    private fun playLatestRecording() {
        val recording = latestRecording
        if (recording == null || !recording.exists()) {
            updateStatus("No recording yet.")
            return
        }

        updateStatus("Playing ${recording.name}...")
        MediaPlayer().apply {
            try {
                setDataSource(recording.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    release()
                    runOnUiThread { updateStatus("Playback finished.") }
                }
                // 🛠️ 修复点 1：去掉了多余的 "On"，改回正确的原生的 Android 监听器名
                setOnErrorListener { _, what, extra ->
                    release()
                    runOnUiThread { updateStatus("Playback error: $what / $extra") }
                    true
                }
            } catch (e: Exception) {
                release()
                updateStatus("Playback failed: ${e.message}")
            }
        }
    }

    private fun runBenchmark() {
        val recording = latestRecording
        if (recording == null) {
            updateStatus("Record a clip before benchmarking.")
            return
        }

        Thread {
            // 🛠️ 修复点 2：为 selectedModelPath 提供了 ?: "" 降级处理，防止因 Nullable 导致类型不匹配报错
            val result = benchmarkRunner.benchmark(recording, selectedModelPath ?: "")
            runOnUiThread { metricsText.text = result.toDisplayText() }
        }.start()
    }

    // ══════════════════════════════════════════
    //  权限与工具函数
    // ══════════════════════════════════════════

    private fun requestMicrophonePermissionIfNeeded() {
        if (!hasMicrophonePermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun updateStatus(message: String) {
        statusText.text = message
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1001
        private const val REQUEST_MODEL_FILE = 1002
    }
}
