package com.light.dungeonofhabits

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.light.dungeonofhabits.adapters.ShopAdapter
import com.light.dungeonofhabits.databinding.ActivityShopBinding

class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.shopRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.gold.observe(this) { gold ->
            binding.tvGold.text = gold.toString()
        }

        viewModel.shopItems.observe(this) { items ->
            binding.shopRecycler.adapter = ShopAdapter(items) { item ->
                viewModel.buyItem(item)
            }
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
