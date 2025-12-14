package com.light.dungeonofhabits

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.light.dungeonofhabits.adapters.CharacterAdapter
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.CharacterResponse
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.PickCharacterRequest
import com.light.dungeonofhabits.models.Stat
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class CharacterActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var hpTV: TextView
    private lateinit var atkTV: TextView
    private lateinit var defTV: TextView
    private lateinit var selectBtn: MaterialButton

    private var characters = listOf<CharacterAdapter.CharacterData>()
    private var selectedPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_character)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewPager = findViewById(R.id.characterPager)
        hpTV = findViewById(R.id.HP)
        atkTV = findViewById(R.id.ATK)
        defTV = findViewById(R.id.DEF)
        selectBtn = findViewById(R.id.selectbtn)

        // Fetch characters from backend
        fetchCharactersFromBackend()
    }
    private fun fetchCharactersFromBackend() {
        ApiClient.apiService.getCharacters().enqueue(object : retrofit2.Callback<CharacterResponse> {
            override fun onResponse(call: Call<CharacterResponse>, response: retrofit2.Response<CharacterResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        // Map backend characters to CharacterData with drawable resource ids
                        characters = data.characters.map { name ->
                            val stat = data.stats[name] ?: Stat(0, 0, 0)
                            CharacterAdapter.CharacterData(
                                name = name,
                                atk = stat.atk,
                                def = stat.def,
                                hp = stat.hp,
                                drawableResId = getDrawableIdForCharacter(name)
                            )
                        }
                        setupViewPager()

                    }
                } else {
                    println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CharacterResponse>, t: Throwable) {
                println("Failure: ${t.message}")
            }
        })
    }
    private fun setupViewPager() {
        val adapter = CharacterAdapter(characters) { position ->
            selectCharacter(position)
        }
        viewPager.adapter = adapter

        // Center snapping effect
        viewPager.offscreenPageLimit = 3
        val pageMargin = resources.getDimensionPixelOffset(R.dimen.page_margin)
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.page_offset)

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40)) // space between pages
            addTransformer { page, position ->
                val scale = 0.85f + (1 - Math.abs(position)) * 0.15f
                page.scaleY = scale
                page.scaleX = scale
                page.alpha = 0.5f + (1 - Math.abs(position)) * 0.5f
            }
        }
        viewPager.setPageTransformer(transformer)

        viewPager.setPageTransformer { page, position ->
            val offset = position * -(2 * pageOffset + pageMargin)
            if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            } else {
                page.translationY = offset
            }
            // Scale effect
            val scale = 0.85f + (1 - abs(position)) * 0.15f
            page.scaleY = scale
            page.scaleX = scale
        }

        // Initially select first
        updateStats(0)

        // Listen for page change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedPosition = position
                updateStats(position)
            }
        })

        selectBtn.setOnClickListener {
            val selectedCharacter = characters[selectedPosition].name
            postPickCharacter(selectedCharacter)
            Toast.makeText(this, "Selected: $selectedCharacter", Toast.LENGTH_SHORT).show()
        }
    }


    private fun selectCharacter(position: Int) {
        selectedPosition = position
        updateStats(position)
    }

    private fun updateStats(position: Int) {
        if (characters.isEmpty() || position !in characters.indices) return

        val character = characters[position]
        hpTV.text = "HP: ${character.hp}"
        atkTV.text = "ATK: ${character.atk}"
        defTV.text = "DEF: ${character.def}"
    }

    private fun getDrawableIdForCharacter(name: String): Int {
        return when (name.lowercase()) {
            "warrior" -> R.drawable.warrior_idle
            "wizard" -> R.drawable.wizard_idle
            "rogue" -> R.drawable.rogue_idle
            "angel" -> R.drawable.angel_idle
            "archer" -> R.drawable.archer_idle
            "dragonewt" -> R.drawable.dragonewt_idle
            else -> R.drawable.warrior_idle
        }
    }
    private fun postPickCharacter(characterName: String) {
        val request = PickCharacterRequest(character = characterName)

        ApiClient.apiService.pickCharacter(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Character selected!"
                    Toast.makeText(this@CharacterActivity, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@CharacterActivity, PetActivity::class.java))
                } else {
                    Toast.makeText(this@CharacterActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@CharacterActivity, "Network failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
