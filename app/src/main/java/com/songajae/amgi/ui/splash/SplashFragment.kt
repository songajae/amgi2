package com.songajae.amgi.ui.splash

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.songajae.amgi.R
import com.songajae.amgi.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class SplashFragment : Fragment() {
    private var _vb: FragmentSplashBinding? = null
    private val vb get() = _vb!!
    private val vm: SplashViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _vb = FragmentSplashBinding.inflate(i, c, false)
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.bootstrap(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    vb.progress.isVisible = state == SplashViewModel.SplashState.Loading
                    when (state) {
                        SplashViewModel.SplashState.Idle, SplashViewModel.SplashState.Loading -> Unit
                        SplashViewModel.SplashState.NavigateLogin -> {
                            vm.markHandled()
                            findNavController().navigate(R.id.action_splash_to_login)
                        }

                        is SplashViewModel.SplashState.NavigateHome -> {
                            vb.tvStatus.text =
                                getString(R.string.splash_ready_message, state.packs.size)
                            vm.markHandled()
                            findNavController().navigate(R.id.action_splash_to_home)
                        }

                        SplashViewModel.SplashState.NavigateDeviceLimit -> {
                            vm.markHandled()
                            Snackbar.make(
                                vb.root,
                                R.string.splash_device_limit,
                                Snackbar.LENGTH_LONG
                            ).show()
                            findNavController().navigate(R.id.action_splash_to_deviceManage)
                        }

                        is SplashViewModel.SplashState.Error -> {
                            Snackbar.make(vb.root, state.message, Snackbar.LENGTH_LONG).show()
                            vm.markHandled()
                            findNavController().navigate(R.id.action_splash_to_login)
                        }
                    }
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (vm.state.value is SplashViewModel.SplashState.Idle) {
            vm.bootstrap(requireContext())
        }
    }

    override fun onDestroyView() { _vb = null; super.onDestroyView() }
}
