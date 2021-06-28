package com.halalin.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.halalin.NavigationAppDirections
import com.halalin.R
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.AuthRepository.AuthState
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.databinding.ActivityMainBinding
import com.halalin.util.EventObserver
import com.halalin.util.topLevelDestinationIdList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: ViewModel by viewModels()

    private lateinit var bottomNavMenuTransaction: MenuItem
    private lateinit var bottomNavMenuMessage: MenuItem
    private lateinit var bottomNavMenuFavorite: MenuItem

    private val authRepository: AuthRepository = FirebaseAuthRepository
//    private val userRepository: UserRepository = FirebaseUserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        viewModel.latestAppVersionCode.observe(this, { if (it != null) checkAppVersionCode(it) })
        viewModel.authState.observe(this, EventObserver { checkAuthState(it) })
    }

    private fun setupNavigation() {
        navController = findNavController(R.id.nav_host_fragment)

        val navGraph = navController.navInflater.inflate(R.navigation.app)
        navGraph.startDestination =
            if (viewModel.isAuthenticated) R.id.home_fragment
            else R.id.sign_in_fragment

        navController.graph = navGraph

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (topLevelDestinationIdList.contains(destination.id)) View.VISIBLE
                else View.GONE
        }

        binding.bottomNavigationView.let {
            bottomNavMenuTransaction = it.menu.findItem(R.id.transaction_list_fragment)
            bottomNavMenuMessage = it.menu.findItem(R.id.message_room_fragment)
            bottomNavMenuFavorite = it.menu.findItem(R.id.favorite_fragment)
            it.setupWithNavController(navController)
        }
    }

    private fun checkAppVersionCode(latestVersionCode: Long) {
        val needUpdate =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
                packageManager.getPackageInfo(packageName, 0).longVersionCode <
                        latestVersionCode
            else
                packageManager.getPackageInfo(packageName, 0).versionCode <
                        latestVersionCode.toInt()

        if (!needUpdate) return
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.activity_main_outdated_app_version_dialog_title)
            .setMessage(R.string.activity_main_outdated_app_version_dialog_message)
            .setPositiveButton(R.string.update) { _, _ ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://drive.google.com/file/d/1M-9BsIDl-c_Kos0dA73TK7G7R3ibIpAz/view?usp=sharing")
                    )
                )
                finish()
            }
            .setNegativeButton(R.string.later) { _, _ -> finish() }
            .show()
    }

    private fun checkAuthState(authState: AuthState) {
        var isGuest = false
        when (authState) {
            AuthState.AUTHENTICATED -> {
//                userRepository.startListening()
            }
            AuthState.GUEST -> {
                isGuest = true
            }
            AuthState.UNAUTHENTICATED -> {
//                userRepository.stopListening()
                navController.navigate(
                    NavigationAppDirections.actionAuthenticate()
                )
            }
        }
        bottomNavMenuTransaction.isVisible = !isGuest
        bottomNavMenuMessage.isVisible = !isGuest
        bottomNavMenuFavorite.isVisible = !isGuest
    }

    override fun onStart() {
        super.onStart()
        authRepository.startListening()
    }

    override fun onStop() {
        super.onStop()
        authRepository.startListening()
    }
}
