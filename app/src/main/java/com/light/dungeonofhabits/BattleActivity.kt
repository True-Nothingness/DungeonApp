package com.light.dungeonofhabits

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.light.dungeonofhabits.databinding.ActivityBattleBinding
import com.light.dungeonofhabits.models.User

class BattleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleBinding
    private val viewModel: BattleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.startBattleButton.setOnClickListener {
            viewModel.startBattle()
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let { updateUserUI(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.startBattleButton.isEnabled = !isLoading
        }

        viewModel.highestFloor.observe(this) { floor ->
            if (floor == -1) {
                binding.highestFloorText.text = getString(R.string.highest_floor_placeholder)
            } else {
                binding.highestFloorText.text = getString(R.string.highest_floor, floor)
            }
        }

        viewModel.battleResult.observe(this) { result ->
            result?.let {
                showBattleResults(it.xp, it.gold, it.highestFloorReached)
                viewModel.consumeBattleResult() // Reset after showing
            }
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserUI(user: User) {
        // Character and Pet Images
        val characterResId = resources.getIdentifier("${user.selectedCharacter}_idle", "drawable", packageName)
        if (characterResId != 0) {
            binding.characterImage.setImageResource(characterResId)
        } else {
            binding.characterImage.setImageResource(R.drawable.warrior_idle)
        }

        if (user.selectedPet != null) {
            val petResId = resources.getIdentifier(user.selectedPet.species, "drawable", packageName)
            binding.petImage.setImageResource(petResId)
            binding.petImage.visibility = View.VISIBLE
        } else {
            binding.petImage.visibility = View.GONE
        }

        // HP Stat Bar
        binding.hpStatBar.statLabel.text = "HP"
        binding.hpStatBar.statProgress.max = user.maxHp
        binding.hpStatBar.statProgress.progress = user.hp
        binding.hpStatBar.statValue.text = "${user.hp}/${user.maxHp}"

        // ATK Stat Bar
        binding.atkStatBar.statLabel.text = "ATK"
        binding.atkStatBar.statProgress.max = 100 // Example max value
        binding.atkStatBar.statProgress.progress = user.atk
        binding.atkStatBar.statValue.text = user.atk.toString()

        // DEF Stat Bar
        binding.defStatBar.statLabel.text = "DEF"
        binding.defStatBar.statProgress.max = 100 // Example max value
        binding.defStatBar.statProgress.progress = user.def
        binding.defStatBar.statValue.text = user.def.toString()
    }

    private fun showBattleResults(xp: Int, gold: Int, floor: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.battle_results))
            .setMessage(getString(R.string.battle_results_message, floor, xp, gold))
            .setPositiveButton(getString(R.string.awesome)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
