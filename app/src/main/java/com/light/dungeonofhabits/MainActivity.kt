package com.light.dungeonofhabits

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import com.light.dungeonofhabits.databinding.ActivityMainBinding
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.utils.Constants
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val profileUpdateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.getProfile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI(savedInstanceState)
        setupObservers()

        val prefs = getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(Constants.LOGGED_IN_TODAY, true) }
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_shop -> startActivity(Intent(this, ShopActivity::class.java))
                R.id.nav_profile -> profileUpdateLauncher.launch(Intent(this, ProfileActivity::class.java))
                R.id.nav_inventory -> startActivity(Intent(this, InventoryActivity::class.java))
                R.id.nav_battle -> startActivity(Intent(this, BattleActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> logoutUser(this)
            }
            binding.main.closeDrawers()
            true
        }

        binding.menuButton.setOnClickListener {
            binding.main.openDrawer(GravityCompat.START)
        }

        binding.bottomNav.background = null
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, TaskFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.nav_tasks -> TaskFragment()
                R.id.nav_dailies -> DailyFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, it)
                    .commit()
                true
            } ?: false
        }

        binding.fabAdd.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container)
            val intent = Intent(this, AddTaskActivity::class.java).apply {
                val type = if (currentFragment is TaskFragment) Constants.TYPE_TASK else Constants.TYPE_DAILY
                putExtra(Constants.EXTRA_TYPE, type)
            }
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.profile.observe(this) { profile ->
            if (profile != null) {
                updateProfileUI(profile)
                showProfileDialogsSequentially(profile.petLeft, profile.hpLost, profile.resetOccurred)
            } else {
                startActivity(Intent(this, CharacterActivity::class.java))
                finish()
            }
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProfileUI(profile: Profile) {
        val user = profile.user
        if (user.selectedCharacter.isNullOrEmpty()) {
            startActivity(Intent(this, CharacterActivity::class.java))
            finish()
            return
        }

        val resId = resources.getIdentifier("${user.selectedCharacter}_idle", "drawable", packageName)
        if (resId != 0) {
            binding.characterImage.setImageResource(resId)
        } else {
            binding.characterImage.setImageResource(R.drawable.warrior_idle) // Fallback image
            Log.e(Constants.TAG_CHARACTER_DEBUG, "Drawable not found for: ${user.selectedCharacter}")
        }

        val selectedPet = user.selectedPet?.species
        if (selectedPet.isNullOrEmpty()) {
            binding.petImage.visibility = View.GONE
        } else {
            binding.petImage.visibility = View.VISIBLE
            binding.petImage.setImageResource(resources.getIdentifier(selectedPet, "drawable", packageName))
        }

        binding.statsText.text = getString(R.string.stats_text, user.level, user.hp, user.maxHp, user.atk, user.def, user.xp, user.gold)
    }

    fun refresh() {
        viewModel.getProfile()
    }

    fun showProfileDialogsSequentially(petLeft: Boolean, hpLost: Int, resetOccurred: Boolean) {
        val dialogs = mutableListOf<AlertDialog.Builder>()

        if (petLeft) {
            dialogs.add(AlertDialog.Builder(this)
                .setTitle(getString(R.string.pet_left_title))
                .setMessage(getString(R.string.pet_left_message))
                .setPositiveButton(getString(R.string.ok), null))
        }

        if (hpLost > 0) {
            dialogs.add(AlertDialog.Builder(this)
                .setTitle(getString(R.string.hp_lost_title))
                .setMessage(getString(R.string.hp_lost_message, hpLost))
                .setPositiveButton(getString(R.string.ok), null))
        }

        if (resetOccurred) {
            dialogs.add(AlertDialog.Builder(this)
                .setTitle(getString(R.string.daily_reset_title))
                .setMessage(getString(R.string.daily_reset_message))
                .setPositiveButton(getString(R.string.lets_go), null))
        }

        fun showDialogAt(index: Int) {
            if (index >= dialogs.size) return
            dialogs[index].setOnDismissListener { showDialogAt(index + 1) }.create().show()
        }

        showDialogAt(0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProfile()
    }

    private fun logoutUser(context: Context) {
        val prefs = context.getSharedPreferences(Constants.USER_PREFS, MODE_PRIVATE)
        prefs.edit { clear() }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
