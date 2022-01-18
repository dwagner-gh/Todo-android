package com.example.todo

import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todo.databinding.TodoEditBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import androidx.core.content.getSystemService

class EditFragment : Fragment() {

    private var binding: TodoEditBinding? = null
    // delegate get() to getValue() of Lazy object
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: SingleModelViewModel by viewModel { parametersOf(args.modelId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =  TodoEditBinding.inflate(inflater, container, false)
        .apply { binding = this }
        .root

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.save -> { save(); hideKeyboard(); navToDisplay(); true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getModel()?.let { model ->
            binding?.apply {
                isCompleted.isChecked = model.isCompleted
                desc.setText(model.description)
                notes.setText(model.notes)
            }
        }
    }

    private fun save() {
        binding?.run {
            viewModel.getModel()?.copy( // create copy
                description = desc.text.toString(),
                isCompleted = isCompleted.isChecked,
                notes = notes.text.toString()
            ) ?: ToDoModel( // create new item
                description = desc.text.toString(),
                isCompleted = isCompleted.isChecked,
                notes = notes.text.toString()
            )
        } ?. let { model -> viewModel.save(model) } // save new or edited item
    }

    private fun navToDisplay() {
        findNavController().popBackStack()
    }

    private fun hideKeyboard() {
        view?.let {
            val imm = context?.getSystemService<InputMethodManager>()
            imm?.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}
