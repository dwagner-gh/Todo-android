package com.example.todo

import android.app.Application
import android.text.format.DateUtils
import androidx.work.*
import com.example.todo.repo.*
import com.example.todo.report.RosterReport
import com.example.todo.ui.SingleModelViewModel
import com.example.todo.ui.roster.RosterViewModel
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Instant
import java.util.concurrent.TimeUnit

private const val TAG_IMPORT_WORK = "doPeriodicImport"

class ToDoApp : Application(), KoinComponent {
    private val koinModule = module {
        single(named("appScope")) { CoroutineScope(SupervisorJob()) }
        single { ToDoDatabase.newInstance(androidContext()) }
        single {
            ToDoRepository(
                get<ToDoDatabase>().todoStore(),
                get(named("appScope")),
                get()
            )
        }
        // Handlebars
        single {
            Handlebars().apply {
                registerHelper("dateFormat", Helper<Instant> { value, _ ->
                    DateUtils.getRelativeDateTimeString(
                        androidContext(),
                        value.toEpochMilli(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.WEEK_IN_MILLIS, 0
                    )
                })
            }
        }
        single { RosterReport(androidContext(), get(), get(named("appScope"))) }
        single { OkHttpClient.Builder().build() }
        single { ToDoRemoteDataSource(get()) }
        single { PrefsRepository(androidContext()) }

        viewModel {
            RosterViewModel(
                get(),
                get(),
                androidApplication(),
                get(named("appScope")),
                get()
            )
        }
        viewModel { (modelId: String) -> SingleModelViewModel(get(), modelId) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ToDoApp)
            // androidLogger() crashes; current workaround:
            // https://github.com/InsertKoinIO/koin/issues/1188#issuecomment-970240532
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            modules(koinModule)
        }
        scheduleWork()
    }

    private fun scheduleWork() {
        val prefs: PrefsRepository by inject()
        val appScope: CoroutineScope by inject(named("appScope"))
        val workManager = WorkManager.getInstance(this)

        appScope.launch {
            // while app is running, listen for changes to the import periodically option
            prefs.observeImportChanges().collect {
                if (it) {
                    // what is needed by the worker
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    // defines what and when it should be done
                    val request =
                        PeriodicWorkRequestBuilder<ImportWorker>(15, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .addTag(TAG_IMPORT_WORK)
                            .build()

                    // actually scheduling the work
                    // (unique means, if work is already present, act according to given policy)
                    workManager.enqueueUniquePeriodicWork(
                        TAG_IMPORT_WORK,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                } else {
                    workManager.cancelAllWorkByTag(TAG_IMPORT_WORK)
                }
            }
        }
    }
}