package com.example.todo.ui.roster

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.example.todo.R
import com.example.todo.repo.ToDoDatabase
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRemoteDataSource
import com.example.todo.repo.ToDoRepository
import com.example.todo.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class RosterListFragmentTest {
    private lateinit var repo: ToDoRepository
    private val items = listOf(
        ToDoModel("this is a test"),
        ToDoModel("this is another test"),
        ToDoModel("this is... wait for it... yet another test")
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = ToDoDatabase.newTestInstance(context)
        val appScope = CoroutineScope(SupervisorJob())

        repo = ToDoRepository(db.todoStore(), appScope, ToDoRemoteDataSource(OkHttpClient()))

        // replace repository that would normally be used (this shows why dependency injection
        // is useful, the repository can be changed dynamically depending on the run context)
        loadKoinModules(module {
            // because coroutine app scope is only needed by repo, we don't need to set it here
            single { repo }
        })

        // force run on current thread; save() is suspend function
        runBlocking { items.forEach { repo.save(it) } }
    }

    @Test
    fun testListContents() {
        // launch() only returns when the UI is fully set up
        ActivityScenario.launch(MainActivity::class.java)
        // checks if RecyclerView displays 3 items
        onView(withId(R.id.items)).check(matches(hasChildCount(3)))
    }
}