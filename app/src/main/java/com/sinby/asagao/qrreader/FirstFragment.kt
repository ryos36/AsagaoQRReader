package com.sinby.asagao.qrreader

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.navigation.fragment.findNavController
import com.sinby.asagao.qrreader.databinding.FragmentFirstBinding

/*
 *
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

	//----------------------------------------------------------------
	private var bluetoothPermissionStatus: Int = STATE_INIT
    private var bluetoothLeScanner: BluetoothLeScanner? = null

	//----------------------------------------------------------------
    private fun initialize() {
        if (bluetoothLeScanner == null) {
            val manager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = manager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        }
    }


	private fun checkPermission() {
		bluetoothPermissionStatus = STATE_INIT
		
        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
			return
		}
		bluetoothPermissionStatus = STATE_HAS_BLUETOOTH_SCAN_PERMISSION
        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			return
		}
		bluetoothPermissionStatus = STATE_HAS_BLUETOOTH_CONNECT_PERMISSION
	}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getButtonString() : String {
        return when(bluetoothPermissionStatus) {
            STATE_INIT -> {
                getString(R.string.button_init)
            }
            STATE_HAS_BLUETOOTH_SCAN_PERMISSION ->  {
                getString(R.string.button_has_scan)
            }
            STATE_HAS_BLUETOOTH_CONNECT_PERMISSION ->  {
                getString(R.string.button_start_scan)
            }
            else -> {
                getString(R.string.button_error)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val STATE_INIT = 0
        const val STATE_HAS_BLUETOOTH_SCAN_PERMISSION = 1
        const val STATE_HAS_BLUETOOTH_CONNECT_PERMISSION = 2
	}
}
