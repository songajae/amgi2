package com.songajae.amgi.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.songajae.amgi.R
import com.songajae.amgi.core.sync.SyncManager
import com.songajae.amgi.data.local.DeviceIdStore
import com.songajae.amgi.databinding.FragmentLoginBinding
import com.songajae.amgi.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _vb: FragmentLoginBinding? = null
    private val vb get() = _vb!!
    private val vm: AuthViewModel by viewModels()
    private var syncJob: Job? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _vb = FragmentLoginBinding.inflate(i, c, false); return vb.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vb.etEmail.doOnTextChanged { text, _, _, _ ->
            val email = text?.toString()?.trim().orEmpty()
            vb.tilEmail.error = when {
                email.isEmpty() -> null
                Patterns.EMAIL_ADDRESS.matcher(email).matches() -> null
                else -> getString(R.string.error_invalid_email)
            }
        }
        vb.btnLogin.setOnClickListener { attemptLogin() }
        vb.tvGoSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    vb.progress.isVisible = state is Result.Loading
                    vb.btnLogin.isEnabled = state !is Result.Loading
                    when (state) {
                        is Result.Success -> {
                            handleLoginSuccess()
                        }
                        is Result.Error -> {
                            vb.tilPassword.error = state.message
                        }
                        else -> vb.tilPassword.error = null
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        syncJob?.cancel()
        syncJob = null
        _vb = null
        super.onDestroyView()
    }

    private fun attemptLogin() {
        val email = vb.etEmail.text?.toString().orEmpty().trim()
        val password = vb.etPassword.text?.toString().orEmpty()
        var valid = true
        if (email.isBlank()) {
            vb.tilEmail.error = getString(R.string.error_required)
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            vb.tilEmail.error = getString(R.string.error_invalid_email)
            valid = false
        } else {
            vb.tilEmail.error = null
        }
        if (password.isBlank()) {
            vb.tilPassword.error = getString(R.string.error_required)
            valid = false
        } else {
            vb.tilPassword.error = null
        }
        if (!valid) return
        vm.login(email, password)
    }

    private fun handleLoginSuccess() {
        vm.resetState()
        syncJob?.cancel()
        syncJob = viewLifecycleOwner.lifecycleScope.launch {
            vb.tilPassword.error = null
            vb.progress.isVisible = true
            vb.btnLogin.isEnabled = false
            try {
                val ctx = requireContext()
                val deviceId = DeviceIdStore.getOrCreate(ctx)
                when (val result = SyncManager.sync(ctx, deviceId)) {
                    is SyncManager.SyncResult.Success -> {
                        if (!isAdded) return@launch
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    SyncManager.SyncResult.DeviceLimitReached -> {
                        if (!isAdded) return@launch
                        Snackbar.make(vb.root, R.string.splash_device_limit, Snackbar.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_login_to_deviceManageDialogFragment)
                    }
                    is SyncManager.SyncResult.Failure -> {
                        if (!isAdded) return@launch
                        vb.tilPassword.error = result.message
                    }
                }
            } catch (_: Throwable) {
                if (!isAdded) return@launch
                Snackbar.make(vb.root, R.string.home_sync_failed, Snackbar.LENGTH_LONG).show()
            } finally {
                if (isAdded) {
                    vb.progress.isVisible = false
                    vb.btnLogin.isEnabled = true
                }
            }
        }
    }
}
