package com.msa.seeyoulater

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.msa.seeyoulater.data.local.database.LinkDatabase
import com.msa.seeyoulater.data.preferences.ThemePreferencesRepository
import com.msa.seeyoulater.data.repository.LinkRepository
import com.msa.seeyoulater.data.repository.LinkRepositoryImpl
import com.msa.seeyoulater.ui.screens.main.MainViewModel
import com.msa.seeyoulater.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class LinkManagerApp : Application() {

    // Create a scope for background tasks that should survive configuration changes
     val applicationScope = CoroutineScope(SupervisorJob())


    // Database instance (lazy initialization)
    val database: LinkDatabase by lazy { LinkDatabase.getDatabase(this) }

    // Repository instance (lazy initialization, depends on database and scope)
    val repository: LinkRepository by lazy {
        LinkRepositoryImpl(
            linkDao = database.linkDao(),
            tagDao = database.tagDao(),
            collectionDao = database.collectionDao(),
            externalScope = applicationScope
        )
    }

    // Theme preferences repository
    val themePreferencesRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepository(this)
    }

     // Simple ViewModel Factory (replace with Hilt/Dagger in a real app)
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                        MainViewModel(repository) as T
                    }
                     modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                        SettingsViewModel(repository, themePreferencesRepository) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        // Initialize any other app-wide components here (e.g., Logging, Analytics, DI)
        instance = this // Provide static access if absolutely needed (use DI preferably)
    }

     companion object {
        // Provide a static way to access the factory if needed (use DI preferably)
        lateinit var instance: LinkManagerApp
            private set
        val factory: ViewModelProvider.Factory
            get() = instance.viewModelFactory
    }
}
