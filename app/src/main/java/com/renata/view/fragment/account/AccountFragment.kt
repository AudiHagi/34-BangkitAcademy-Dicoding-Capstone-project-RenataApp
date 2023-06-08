package com.renata.view.fragment.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.renata.R
import com.renata.data.user.login.LoginPreferences
import com.renata.data.user.login.LoginResult
import com.renata.databinding.FragmentAccountBinding
import com.renata.view.activity.profile.ProfileActivity
import com.renata.view.activity.profile.ProfileViewModel
import com.renata.view.activity.setting.SettingActivity
import com.renata.view.activity.splash.SplashScreenActivity

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val accountBinding get() = _binding!!
    private var accountViewModel: ProfileViewModel = ProfileViewModel()
    private lateinit var loginPreference: LoginPreferences
    private lateinit var loginResult: LoginResult
//    private val PROFILE_ACTIVITY_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return accountBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginPreference = LoginPreferences(requireContext())
        loginResult = loginPreference.getUser()
        accountViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[ProfileViewModel::class.java]
        val token = loginResult.token

        getData(token)
        goToSetting()
        changeProfile(token)
        logout()
    }


    private fun getData(token: String?) {
        val getToken = "Bearer $token"
        showLoading(true)
        accountViewModel.userProfile(getToken)
        accountViewModel.getUserProfile().observe(viewLifecycleOwner) { response ->
            showLoading(false)
            if (response != null && response.success) {
                val data = response.data
                accountBinding.tvProfileEmail.text = data.email
                val image = data.avatar_link
                val name = data.full_name
                if (image == "" || name == "") {
                    accountBinding.tvProfileName.text = ""
                    accountBinding.profileImage.setImageResource(R.drawable.image_placeholder)
                } else {
                    accountBinding.tvProfileName.text = data.full_name
                    Glide.with(this@AccountFragment)
                        .load(data.avatar_link)
                        .into(accountBinding.profileImage)
                }
            }
            showLoading(false)
        }

    }


    private fun changeProfile(token: String?) {
        accountBinding.editProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            intent.putExtra("token", token)
            profileActivityResultLauncher.launch(intent)
//            startActivityForResult(intent, PROFILE_ACTIVITY_REQUEST_CODE)
        }
    }

    private val profileActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val name = data?.getStringExtra("name")
            val email = data?.getStringExtra("email")
            accountBinding.tvProfileName.text = name
            accountBinding.tvProfileEmail.text = email

            val tes = loginResult.token
            val tokenNow = "Bearer $tes"
            accountViewModel.userProfile(tokenNow)
            accountViewModel.getUserProfile().observe(viewLifecycleOwner) {
                val setData = it.data
                val image = setData.avatar_link
                if (image == "") {
                    accountBinding.profileImage.setImageResource(R.drawable.image_placeholder)
                } else {
                    Glide.with(requireActivity())
                        .load(image)
                        .into(accountBinding.profileImage)
                }
            }
        }
    }

    private fun goToSetting() {
        accountBinding.settingButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logout() {
        accountBinding.logoutButton.setOnClickListener {
            loginPreference.removeUser()
            val intentToSplash = Intent(requireContext(), SplashScreenActivity::class.java)
            startActivity(intentToSplash)
            activity?.finishAffinity()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        accountBinding.progressBar2?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}