package com.example.apk.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.apk.R
import com.example.apk.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth


    // Dentro de la clase MainActivity
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Configurar fragmento inicial
        replaceFragment(Home())

        // Configurar BottomNavigationView
        binding.botNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(Home())
                R.id.perfil -> replaceFragment(Perfil())
                R.id.ajustes -> replaceFragment(Ajustes())
            }
            true
        }

        // Configurar Navigation Drawer
        setupNavigationDrawer()
    }

    private fun setupNavigationDrawer() {
        val navView: NavigationView = binding.navView

        // Configurar el listener del menú lateral
        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.nav_eraseMarker -> {
                    replaceFragment(Perfil())
                    //saltar al fragment o insertar layout de borrar marcador
                    binding.botNavigationView.selectedItemId = R.id.perfil
                }
                R.id.nav_editMarker -> {
                    replaceFragment(Ajustes())
                    //saltar al fragment o insertar layout de editar marcador
                    binding.botNavigationView.selectedItemId = R.id.ajustes
                }

            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Actualizar header con datos del usuario
        updateNavHeader()
    }

    private fun updateNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvHeaderEmail)

        auth.currentUser?.email?.let { email ->
            tvEmail.text = email
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}