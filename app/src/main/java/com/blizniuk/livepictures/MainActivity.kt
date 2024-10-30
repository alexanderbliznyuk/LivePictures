package com.blizniuk.livepictures

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blizniuk.livepictures.databinding.ActivityMainBinding
import com.blizniuk.livepictures.util.RoundCornersOutlineProvider

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding?.apply {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            val cornerRadius = resources.getDimension(R.dimen.round_corners_radius)
            canvasBackground.clipToOutline = true
            canvasBackground.outlineProvider = RoundCornersOutlineProvider(cornerRadius)

            canvasView.clipToOutline = true
            canvasView.outlineProvider = RoundCornersOutlineProvider(cornerRadius)
        }
    }
}