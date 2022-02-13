package com.example.todo

import android.app.Application
import android.text.format.DateUtils
import com.example.todo.repo.ToDoDatabase
import com.example.todo.repo.ToDoRepository
import com.example.todo.report.RosterReport
import com.example.todo.ui.SingleModelViewModel
import com.example.todo.ui.roster.RosterViewModel
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Instant

class ToDoApp : Application() {
    private val koinModule = module {
        single(named("appScope")) { CoroutineScope(SupervisorJob()) }
        single { ToDoDatabase.newInstance(androidContext()) }
        single {
            ToDoRepository(
                get<ToDoDatabase>().todoStore(),
                get(named("appScope"))
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

        viewModel { RosterViewModel(get(), get()) }
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
    }
}