package com.example.todo.ui

import com.example.todo.MainDispatcherRule
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SingleModelViewModelTest {
    // add @Rule annotation to the get function of this property
    // annotation can only be used on field or getter method
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(paused = true)
    private val testModel = ToDoModel("this is a test")

    // mocking a repository to avoid database I/O, testing specific scenarios etc...
    private val repoMock: ToDoRepository = mock {
        // when find(testModel.id) is called, return testModel wrapped in a Flow
        // otherwise default behaviour of find() (I think)
        on { find(testModel.id) } doReturn flowOf(testModel)
    }

    // needed because threading not handled by MainDispatcherRule yet,
    // so setUp() needs to be called before every test run
    private lateinit var underTest: SingleModelViewModel

    @Before
    fun setUp() {
        underTest = SingleModelViewModel(repoMock, testModel.id)
    }

    @Test
    fun `initial state`() {
        mainDispatcherRule.dispatcher.runCurrent()

        runBlocking {
            // get first item emitted by flow and cancel collection
            val item = underTest.states.first().item
            assertEquals(testModel, item)
        }
    }

    @Test
    fun `actions pass through to repo`() {
        val replacement = testModel.copy("whatever")

        underTest.save(replacement)
        // runCurrent() ensures that the save() function was actually executed
        mainDispatcherRule.dispatcher.runCurrent()

        // mock is tracking calls that were made to it
        // with verify() u can check whether specific calls were made
        // runBlocking() needed, because repo functions are suspend functions
        runBlocking { verify(repoMock).save(replacement) }

        underTest.delete(replacement)
        mainDispatcherRule.dispatcher.runCurrent()

        runBlocking { verify(repoMock).delete(replacement) }
    }

}