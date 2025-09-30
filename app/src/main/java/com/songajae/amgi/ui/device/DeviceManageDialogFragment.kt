package com.songajae.amgi.ui.device

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.songajae.amgi.R
import com.songajae.amgi.data.remote.AdminApi
import kotlinx.coroutines.launch

class DeviceManageDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = mutableListOf<String>()
        val adp = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

        suspend fun reload() {
            items.addAll(AdminApi.listDevicesPretty())
            adp.notifyDataSetChanged()
        }

        lifecycleScope.launch { reload() }


            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.device_manage_title)
                .setAdapter(adp) { _, which ->
                    lifecycleScope.launch {
                        AdminApi.deleteDeviceByIndex(which)
                        reload()
                    }
                }
                .setNegativeButton(R.string.dialog_close, null)
            .create()
    }
}
