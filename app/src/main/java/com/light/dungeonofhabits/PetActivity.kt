package com.light.dungeonofhabits

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.light.dungeonofhabits.adapters.PetAdapter
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.PickPetRequest
import com.light.dungeonofhabits.models.CharacterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener
import kotlin.math.abs

class PetActivity : AppCompatActivity() {
    private lateinit var petPager: ViewPager2
    private lateinit var petNameEditText: TextInputEditText
    private lateinit var selectBtn: MaterialButton

    private var pets = listOf<String>()
    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        petPager = findViewById(R.id.petPager)
        petNameEditText = findViewById(R.id.petEditText)
        selectBtn = findViewById(R.id.selectbtn)

        fetchPetsFromBackend()
    }

    private fun fetchPetsFromBackend() {
        ApiClient.apiService.getCharacters().enqueue(object : Callback<CharacterResponse> {
            override fun onResponse(call: Call<CharacterResponse>, response: Response<CharacterResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        pets = it.pets // Assuming backend returns list of pet names here
                        setupViewPager()
                    }
                } else {
                    Toast.makeText(this@PetActivity, "Error fetching pets: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CharacterResponse>, t: Throwable) {
                Toast.makeText(this@PetActivity, "Network failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupViewPager() {
        val adapter = PetAdapter(pets) { position ->
            selectedPosition = position
        }
        petPager.adapter = adapter

        // Center snapping & scaling effect
        petPager.offscreenPageLimit = 3
        val pageMargin = resources.getDimensionPixelOffset(R.dimen.page_margin)
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.page_offset)

        petPager.setPageTransformer { page, position ->
            val offset = position * -(2 * pageOffset + pageMargin)
            if (petPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(petPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
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

        // Select first pet initially
        selectedPosition = 0

        // Listen for page change
        petPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedPosition = position
            }
        })

        // Select button logic
        selectBtn.setOnClickListener {
            val petNameInput = petNameEditText.text?.toString()?.trim()
            val selectedPet = pets.getOrNull(selectedPosition)

            if (selectedPet == null) {
                Toast.makeText(this, "No pet selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (petNameInput.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter a name for your pet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            postPickPet(selectedPet, petNameInput)
        }
    }

    private fun postPickPet(petName: String, petNickname: String) {
        val request = PickPetRequest(species = petName, name = petNickname)

        ApiClient.apiService.pickPet(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Pet selected!"
                    Toast.makeText(this@PetActivity, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PetActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(this@PetActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@PetActivity, "Network failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
