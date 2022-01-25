package com.example.todo.ui.roster

import androidx.lifecycle.ViewModel
// lifecycle aware scope, outstanding coroutines get cancelled when ViewModel gets cleared
// when is view model cleared?
import androidx.lifecycle.viewModelScope
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RosterViewState(
    val items: List<ToDoModel> = listOf()
)

class RosterViewModel(private val repo: ToDoRepository) : ViewModel() {
    // original version contains a bug, saves() creates a new list in the repository
    // items would however always point to the old list, with this getter, the view model
    // always gets the new and updated list of items even after a save
    val states= repo.items()
            // here the flow is still cold, no actual observers, flow is still lazy, not holding
            // on to a state and not sending updates to observers
            .map { RosterViewState(it) }
            // makes the flow "hot", it holds on to current state and gives it to new
            // observers. In addition it emits new states to current observers (flow starts
            // immediately because of "Eagerly")
            .stateIn(viewModelScope, SharingStarted.Eagerly, RosterViewState())

//    init {
//        viewModelScope.launch {
//            newsRepository.favoriteLatestNews
//                // Update View with the latest favorite news
//                // Writes to the value property of MutableStateFlow,
//                // adding a new element to the flow and updating all
//                // of its collectors
//                .collect { favoriteNews ->
//                    _uiState.value = LatestNewsUiState.Success(favoriteNews)
//                }
//        }
//    }


    fun save(model: ToDoModel) {
        repo.save(model)
    }
}