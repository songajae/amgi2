package com.songajae.amgi.ui.auth

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.songajae.amgi.R
import com.songajae.amgi.databinding.FragmentSignupBinding
import com.songajae.amgi.util.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private var _vb: FragmentSignupBinding? = null
    private val vb get() = _vb!!
    private val vm: AuthViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _vb = FragmentSignupBinding.inflate(i, c, false); return vb.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        vb.btnDoSignup.setOnClickListener { attemptSignup() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    vb.progress.isVisible = state is Result.Loading
                    vb.btnDoSignup.isEnabled = state !is Result.Loading
                    when (state) {
                        is Result.Success -> {
                            Snackbar.make(vb.root, R.string.signup_success, Snackbar.LENGTH_SHORT).show()
                            vm.resetState()
                            findNavController().navigate(R.id.action_signup_to_login)
                        }
                        is Result.Error -> vb.tilPassword.error = state.message
                        else -> vb.tilPassword.error = null
                    }
                }
            }
        }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }

    private fun attemptSignup() {
        val name = vb.etName.text?.toString().orEmpty().trim()
        val email = vb.etSignupEmail.text?.toString().orEmpty().trim()
        val password = vb.etSignupPassword.text?.toString().orEmpty()
        var valid = true
        if (name.isBlank()) {
            vb.tilName.error = getString(R.string.error_required)
            valid = false
        } else {
            vb.tilName.error = null
        }
        if (email.isBlank()) {
            vb.tilEmail.error = getString(R.string.error_required)
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
        vm.signup(name, email, password)
    }
}
