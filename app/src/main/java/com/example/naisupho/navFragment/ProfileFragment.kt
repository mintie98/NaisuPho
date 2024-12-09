package com.example.naisupho.navFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.naisupho.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.naisupho.databinding.FragmentProfileBinding
import com.example.naisupho.userInfo.AddressActivity
import com.example.naisupho.userInfo.CouponActivity
import com.example.naisupho.userInfo.EditInfoActivity
import com.example.naisupho.PaymentActivity
import com.example.naisupho.userInfo.ReviewActivity
import com.example.naisupho.SettingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var database: DatabaseReference

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val currentUser = auth.currentUser

        if (currentUser != null) {
            database.child("Users").child(currentUser.uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userName = dataSnapshot.getValue(String::class.java)
                        binding.userName.text = userName
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })

            database.child("Users").child(currentUser.uid).child("photoUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val photoUrl = dataSnapshot.getValue(String::class.java)
                        if (photoUrl != null && isAdded) {
                            Glide.with(this@ProfileFragment)
                                .load(photoUrl)
                                .into(binding.userImage)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
        }

        // Set an OnClickListener for the userEmail and userImage
        val clickListener = View.OnClickListener {
            val intent = Intent(activity, EditInfoActivity::class.java)
            startActivity(intent)
        }
        binding.userName.setOnClickListener(clickListener)
        binding.userImage.setOnClickListener(clickListener)

        // Setting button
        binding.settingsIcon.setOnClickListener {
            val intent = Intent(requireActivity(), SettingActivity::class.java)
            requireActivity().startActivity(intent)
        }

        // Change Address
        binding.address.setOnClickListener {
            val intent = Intent(requireActivity(), AddressActivity::class.java)
            requireActivity().startActivity(intent)
        }
        binding.payment.setOnClickListener {
            val intent = Intent(requireActivity(), PaymentActivity::class.java)
            requireActivity().startActivity(intent)
        }

        // Coupons button
        binding.coupon.setOnClickListener {
            val intent = Intent(requireActivity(), CouponActivity::class.java)
            requireActivity().startActivity(intent)
        }

        // Review button
        binding.reviews.setOnClickListener {
            val intent = Intent(requireActivity(), ReviewActivity::class.java)
            requireActivity().startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}