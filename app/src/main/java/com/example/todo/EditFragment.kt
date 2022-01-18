package com.example.todo

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.todo.databinding.TodoEditBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditFragment : Fragment() {

    private var binding: TodoEditBinding? = null
    // delegate get() to getValue() of Lazy object
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: SingleModelViewModel by viewModel { parametersOf(args.modelId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =  TodoEditBinding.inflate(inflater, container, false)
        .apply { binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getModel()?.let { model ->
            binding?.apply {
                isCompleted.isChecked = model.isCompleted
                desc.setText(model.description)
                notes.setText(model.notes)
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}