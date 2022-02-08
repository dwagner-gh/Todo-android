package com.example.todo.ui.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.todo.R

class PrefsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(state: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
    }
}