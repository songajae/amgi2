package com.songajae.amgi.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.songajae.amgi.R
import com.songajae.amgi.databinding.FragmentSignupBinding
import com.songajae.amgi.util.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private var _vb: FragmentSignupBinding? = null
    private val vb get() = _vb!!
    private val vm: AuthViewModel by viewModels()
    private var successDialog: AlertDialog? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _vb = FragmentSignupBinding.inflate(i, c, false); return vb.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        vb.etSignupEmail.doOnTextChanged { text, _, _, _ ->
            val email = text?.toString()?.trim().orEmpty()
            vb.tilEmail.error = when {
                email.isEmpty() -> null
                Patterns.EMAIL_ADDRESS.matcher(email).matches() -> null
                else -> getString(R.string.error_invalid_email)
            }
        }
        val passwordWatcher: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
            updatePasswordConfirmationState()
        }
        vb.etSignupPassword.doOnTextChanged(passwordWatcher)
        vb.etSignupPasswordConfirm.doOnTextChanged(passwordWatcher)
        vb.btnDoSignup.setOnClickListener { attemptSignup() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    vb.progress.isVisible = state is Result.Loading
                    vb.btnDoSignup.isEnabled = state !is Result.Loading
                    when (state) {
                        is Result.Success -> {
                            vm.resetState()
                            successDialog?.dismiss()
                            successDialog = MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.signup_success_title)
                                .setMessage(R.string.signup_success_message)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    successDialog = null
                                    findNavController().navigate(R.id.action_signup_to_login)
                                }
                                .show()
                        }
                        is Result.Error -> {
                            vb.tilPassword.error = state.message
                        }
                        else -> {
                            vb.tilPassword.error = null
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        successDialog?.dismiss()
        successDialog = null
        _vb = null
        super.onDestroyView()
    }

    private fun attemptSignup() {
        val name = vb.etName.text?.toString().orEmpty().trim()
        val email = vb.etSignupEmail.text?.toString().orEmpty().trim()
        val password = vb.etSignupPassword.text?.toString().orEmpty()
        val confirmPassword = vb.etSignupPasswordConfirm.text?.toString().orEmpty()
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
        if (confirmPassword.isBlank()) {
            vb.tilPasswordConfirm.error = getString(R.string.error_required)
            valid = false
        } else if (confirmPassword != password) {
            vb.tilPasswordConfirm.error = getString(R.string.error_password_mismatch)
            valid = false
        } else {
            vb.tilPasswordConfirm.error = null
        }
        if (!valid) return
        vm.signup(name, email, password)
    }

    private fun updatePasswordConfirmationState() {
        val password = vb.etSignupPassword.text?.toString().orEmpty()
        val confirm = vb.etSignupPasswordConfirm.text?.toString().orEmpty()
        vb.tilPasswordConfirm.error = when {
            confirm.isEmpty() -> null
            confirm == password -> null
            else -> getString(R.string.error_password_mismatch)
        }
    }
}
