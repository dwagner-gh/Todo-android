package com.example.todo.ui.roster

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.AppConstants
import com.example.todo.R
import com.example.todo.databinding.TodoRosterBinding
import com.example.todo.repo.FilterMode
import com.example.todo.repo.ToDoModel
import com.example.todo.ui.ErrorDialogFragment
import com.example.todo.ui.ErrorScenario
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class RosterListFragment : Fragment() {

    private var binding: TodoRosterBinding? = null
    private val rosterViewModel: RosterViewModel by viewModel()
    private val menuMap = mutableMapOf<FilterMode, MenuItem>()
    private val createDoc = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        rosterViewModel.saveReport(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = TodoRosterBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RosterAdapter(
            layoutInflater,
            onCheckboxToggle = { model -> rosterViewModel.save(model.copy(isCompleted = !model.isCompleted)) },
            onRowClicked = ::display
        )

        // accessing recyclerview
        binding?.items?.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // lambda runs as a coroutine when views are visible, suspended when not visible
            // collection of new states
            rosterViewModel.states.collect { state ->
                // collects new states, as long as this coroutine runs
                // (how is the collect call connected to the coroutine?)
                adapter.submitList(state.items)

                binding?.apply {
                    loading.visibility = if (state.isLoaded) View.GONE else View.VISIBLE

                    when {
                        state.items.isEmpty() && state.filterMode == FilterMode.ALL -> {
                            empty.visibility = View.VISIBLE
                            empty.setText(R.string.msg_empty)
                        }
                        // no items, and items are filtered
                        state.items.isEmpty() -> {
                            empty.visibility = View.VISIBLE
                            empty.setText(R.string.msg_empty_filtered)
                        }
                        else -> empty.visibility = View.GONE
                    }
                }
                // TODO save filter in SharedPrefs
                // mark the right filter option as checked on newly emitted view states
                menuMap[state.filterMode]?.isChecked = true
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // collection of new events
            rosterViewModel.navEvents.collect { navEvent ->
                when (navEvent) {
                    // report was saved, open it
                    is NavEvent.ViewReport -> viewReport(navEvent.documentURI)
                    is NavEvent.ShareReport -> shareReport(navEvent.documentURI)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            rosterViewModel.errorEvents.collect { error ->
                when (error) {
                    ErrorScenario.Import -> handleImportError()
                    else -> {}
                }
            }
        }

        // live data is used for both java and kotlin as part of jetpack
        findNavController()
            .getBackStackEntry(R.id.rosterListFragment)
            .savedStateHandle
            .getLiveData<ErrorScenario>(ErrorDialogFragment.KEY_RETRY)
            .observe(viewLifecycleOwner) { retryScenario ->
                when (retryScenario) {
                    ErrorScenario.Import -> {
                        clearImportError()
                        rosterViewModel.importItems()
                    }
                    else -> {}
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_roster, menu)
        // TODO handle with shared prefs
        menuMap.apply {
            put(FilterMode.ALL, menu.findItem(R.id.all))
            put(FilterMode.COMPLETED, menu.findItem(R.id.completed))
            put(FilterMode.OUTSTANDING, menu.findItem(R.id.outstanding))
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.addItem -> {
            add(); true
        }
        R.id.all -> {
            item.isChecked = true
            rosterViewModel.load(FilterMode.ALL)
            true
        }
        R.id.completed -> {
            item.isChecked = true
            rosterViewModel.load(FilterMode.COMPLETED)
            true
        }
        R.id.outstanding -> {
            item.isChecked = true
            rosterViewModel.load(FilterMode.OUTSTANDING)
            true
        }
        R.id.save -> {
            saveReport()
            true
        }
        R.id.share -> {
            rosterViewModel.shareReport()
            true
        }
        R.id.importItems -> {
            rosterViewModel.importItems()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun saveReport() {
        createDoc.launch("report.html")
    }

    private fun safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (t: ActivityNotFoundException) {
            Log.e(AppConstants.LOGGING_TAG, "Exception starting $intent", t)
            Toast.makeText(requireActivity(), R.string.oops, Toast.LENGTH_LONG).show()
        }
    }

    private fun viewReport(uri: Uri) {
        safeStartActivity(
            Intent(Intent.ACTION_VIEW, uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    // are there other intents for sharing?
    private fun shareReport(documentURI: Uri) {
        safeStartActivity(
            Intent(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setType("text/html")
                .putExtra(Intent.EXTRA_STREAM, documentURI)
        )
    }

    private fun display(model: ToDoModel) {
        findNavController().navigate(RosterListFragmentDirections.displayModel(model.id))
    }

    private fun add() {
        findNavController().navigate(RosterListFragmentDirections.addTodoItem(null))
    }

    private fun handleImportError() {
        findNavController().navigate(
            RosterListFragmentDirections.showError(
                getString(R.string.import_error_title),
                getString(R.string.import_error_message),
                ErrorScenario.Import
            )
        )
    }

    private fun clearImportError() {
        findNavController()
            .getBackStackEntry(R.id.rosterListFragment)
            .savedStateHandle
            .set(ErrorDialogFragment.KEY_RETRY, ErrorScenario.None)
    }
}