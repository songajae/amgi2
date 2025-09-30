package com.songajae.amgi.ui.home

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.songajae.amgi.R
import com.songajae.amgi.core.net.NetworkGate
import com.songajae.amgi.core.sync.SyncManager
import com.songajae.amgi.data.local.DeviceIdStore
import com.songajae.amgi.data.local.LeaseCache
import com.songajae.amgi.data.packs.PackRepository
import com.songajae.amgi.data.remote.AuthService
import com.songajae.amgi.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import java.text.DateFormat

class HomeFragment : Fragment() {
    private var _vb: FragmentHomeBinding? = null
    private val vb get() = _vb!!
    private val adapter = PackAdapter()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _vb = FragmentHomeBinding.inflate(i, c, false); return vb.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val ctx = requireContext()
        vb.rvPacks.layoutManager = LinearLayoutManager(ctx)
        vb.rvPacks.adapter = adapter

        vb.btnManageDevices.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_deviceManageDialogFragment)
        }
        vb.btnLogout.setOnClickListener {
            AuthService.logout()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
        vb.btnRefresh.setOnClickListener { refreshContent() }

        lifecycleScope.launch { populateDeviceInfo() }
        lifecycleScope.launch { loadCachedPacks() }
    }

    private suspend fun populateDeviceInfo() {
        val ctx = requireContext()
        val deviceId = DeviceIdStore.getOrCreate(ctx)
        vb.tvDeviceId.text = getString(R.string.home_device_info, deviceId)
        val lastSeen = LeaseCache.getLastSeen(ctx, deviceId)
        vb.tvLastSynced.text = if (lastSeen != null) {
            val formatted = DateFormat.getDateTimeInstance().format(lastSeen)
            getString(R.string.home_last_synced, formatted)
        } else {
            getString(R.string.home_last_synced, "-")
        }
    }

    private suspend fun loadCachedPacks() {
        val ctx = requireContext()
        val packs = PackRepository.loadCachedPacks(ctx)
        adapter.submitList(packs)
        vb.tvEmpty.isVisible = packs.isEmpty()
    }

    private fun refreshContent() {
        lifecycleScope.launch {
            val ctx = requireContext()
            if (!NetworkGate.isOnline(ctx) || !AuthService.isLoggedIn()) {
                Snackbar.make(vb.root, R.string.home_sync_failed, Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            vb.btnRefresh.isEnabled = false
            when (val result = SyncManager.sync(ctx, DeviceIdStore.getOrCreate(ctx))) {
                is SyncManager.SyncResult.Success -> {
                    adapter.submitList(result.packs)
                    vb.tvEmpty.isVisible = result.packs.isEmpty()
                    populateDeviceInfo()
                }
                SyncManager.SyncResult.DeviceLimitReached -> {
                    Snackbar.make(vb.root, R.string.splash_device_limit, Snackbar.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_homeFragment_to_deviceManageDialogFragment)
                }
                is SyncManager.SyncResult.Failure -> {
                    Snackbar.make(vb.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
            vb.btnRefresh.isEnabled = true
        }
    }
    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}
