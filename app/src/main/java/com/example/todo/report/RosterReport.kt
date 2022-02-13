package com.example.todo.report

import android.content.Context
import android.net.Uri
import com.example.todo.R
import com.example.todo.repo.ToDoModel
import com.github.jknack.handlebars.Handlebars
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RosterReport(
    private val context: Context,
    engine: Handlebars,
    private val appScope: CoroutineScope
) {
    private val template =
        engine.compileInline(context.getString(R.string.report_template))

    suspend fun generate(content: List<ToDoModel>, documentURI: Uri) {
        // off loading file access to IO background thread
        withContext(Dispatchers.IO + appScope.coroutineContext) {
            // use {} automatically closes the writer when finished
            context.contentResolver.openOutputStream(documentURI, "rwt")?.writer()?.use { osw ->
                osw.write(template.apply(content))
                osw.flush()
            }
        }
    }
}