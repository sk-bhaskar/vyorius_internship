package com.example.vyorius_internship_kt

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoLayout: VLCVideoLayout
    private var isRecording = false

    private val rtspUrl = "rtsp://your_rtsp_stream_url" // <-- Replace with your actual RTSP URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize VLC
        val args = ArrayList<String>().apply {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("--rtsp-tcp") // Ensures more stable streaming
        }

        libVLC = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVLC)

        videoLayout = findViewById(R.id.vlc_video_layout)
        mediaPlayer.attachViews(videoLayout, null, false, false)

        val btnStream = findViewById<Button>(R.id.btnStream)
        val btnRecord = findViewById<Button>(R.id.btnRecord)

        btnStream.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                val media = Media(libVLC, rtspUrl)
                media.setHWDecoderEnabled(true, false)
                media.addOption(":network-caching=150")
                mediaPlayer.media = media
                media.release()
                mediaPlayer.play()
                Toast.makeText(this, "Streaming started", Toast.LENGTH_SHORT).show()
                btnStream.text = "Stop Stream"
            } else {
                mediaPlayer.stop()
                Toast.makeText(this, "Streaming stopped", Toast.LENGTH_SHORT).show()
                btnStream.text = "Start Stream"
            }
        }

        btnRecord.setOnClickListener {
            if (!isRecording) {
                startRecording()
                btnRecord.text = "Stop Recording"
            } else {
                stopRecording()
                btnRecord.text = "Start Recording"
            }
            isRecording = !isRecording
        }
    }

    private fun startRecording() {
        val path = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "vlc_record.mp4").absolutePath
        mediaPlayer.record(path)
        Toast.makeText(this, "Recording to: $path", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        mediaPlayer.record(null) // Stop recording
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        libVLC.release()
    }
}
