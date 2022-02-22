package com.example.todo.ui.roster

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
// lifecycle aware scope, outstanding coroutines get cancelled when ViewModel gets cleared
// when is view model cleared?
import androidx.lifecycle.viewModelScope
import com.example.todo.AppConstants.LOGGING_TAG
import com.example.todo.BuildConfig
import com.example.todo.repo.FilterMode
import com.example.todo.repo.PrefsRepository
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import com.example.todo.report.RosterReport
import com.example.todo.ui.ErrorScenario
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.lang.RuntimeException

data class RosterViewState(
    val items: List<ToDoModel> = listOf(),
    val isLoaded: Boolean = false,
    val filterMode: FilterMode = FilterMode.ALL
)

// similar to an enum, but multiple instances of a given sub class can exist
// and the containing data is not constant; sealed class can not be instantiated (abstract)
sealed class NavEvent {
    data class ViewReport(val documentURI: Uri) : NavEvent()
    data class ShareReport(val documentURI: Uri) : NavEvent()
}

class RosterViewModel(
    private val repo: ToDoRepository,
    private val report: RosterReport,
    private val context: Application,
    private val appScope: CoroutineScope,
    private val prefs: PrefsRepository
) : ViewModel() {
    // offering the View State via a Flow, so the actual view doesn't need to subscribe
    // to different flows depending on the filter mode (new flow for each filter mode)
    private val _states = MutableStateFlow(RosterViewState())

    // read only version of _states; represents the public flow (that can be subscribed to),
    // while the flows of the repository are kept hidden and are handled by load(). changes of
    // of repo flows are forwarded to _states via emit()
    val states = _states.asStateFlow()

    // shared flow is similar to state flow, but better for events
    private val _navEvents = MutableSharedFlow<NavEvent>()
    val navEvents = _navEvents.asSharedFlow()

    private val _errorEvents = MutableSharedFlow<ErrorScenario>()
    val errorEvents = _errorEvents.asSharedFlow()

    private val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
    private var job: Job? = null

    // the default behaviour is to load all entities
    init {
        load(FilterMode.ALL)
    }

    fun load(filterMode: FilterMode) {
        // cancel previous query (no new emits for the previous filter mode)
        job?.cancel()
        // start new query for given filter mode, and emit new view state into the states flow
        job = viewModelScope.launch {
            repo.items(filterMode).collect {
                _states.emit(RosterViewState(it, true, filterMode))
            }
        }
    }

    fun save(model: ToDoModel) {
        viewModelScope.launch {
            repo.save(model)
        }
    }

    fun saveReport(documentURI: Uri) {
        // still running in main thread, but can be suspended
        viewModelScope.launch {
            report.generate(_states.value.items, documentURI)
            _navEvents.emit(NavEvent.ViewReport(documentURI))
        }
    }

    fun shareReport() {
        viewModelScope.launch {
            saveForSharing()
        }
    }

    private suspend fun saveForSharing() {
        // run generation of report in background IO thread
        withContext(Dispatchers.IO + appScope.coroutineContext) {
            val shared = File(context.cacheDir, "shared").also { it.mkdirs() }
            val reportFile = File(shared, "report.html")
            val contentURI = FileProvider.getUriForFile(context, AUTHORITY, reportFile)
            _states.value.let { report.generate(it.items, contentURI) }
            _navEvents.emit(NavEvent.ShareReport(contentURI))
        }
    }

    fun importItems() {
        viewModelScope.launch {
            try {
                repo.importItems(prefs.loadWebServiceUrl())
            }
            catch (ex: IOException) {
                Log.e(LOGGING_TAG, "Exception importing items", ex)
                _errorEvents.emit(ErrorScenario.Import)
            }
        }
    }
}