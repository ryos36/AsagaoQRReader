package com.sinby.asagao.qrreader

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.navigation.fragment.findNavController
import com.sinby.asagao.qrreader.databinding.FragmentSecondBinding

/*
 *
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var handler: Handler? = null

	private var runnable = Runnable { stopScanning() }

	//----------------------------------------------------------------
    private fun initialize() {
        if ( bluetoothLeScanner == null ) {
            val manager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = manager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        }
    }

    // ToDo: synchronized method
    private fun startScanning() {
        if (scanCallback != null) {
            Toast.makeText(requireContext(), getString(R.string.bt_scanning), Toast.LENGTH_LONG).show()
            return
        }
        scanCallback = AsagaoScanCallback()
        bluetoothLeScanner?.startScan(scanCallback)

		handler?.postDelayed(runnable, SCAN_PERIOD_IN_MILLIS)
    }

    private fun stopScanning() {
		if ( scanCallback != null ) {
			bluetoothLeScanner?.stopScan(scanCallback)
			scanCallback = null
			Toast.makeText(requireContext(), getString(R.string.bt_stop_scanning), Toast.LENGTH_LONG).show()
		}
    }

	//----------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.myLooper()!!)
        initialize()

        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
			val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
				if (result) {
                    startScanning()
				} else {
					Toast.makeText(this.requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show()
				}
			}

			requestPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
		} else {
            startScanning()
        }
		
		/*
        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
				if (result) {
					Toast.makeText(
						this.requireContext(),
						"Permission Accepted.",
						Toast.LENGTH_SHORT
					).show()

					//bluetoothLeScanner?.startScan(scanCallback)
				} else {
					// リクエスト拒否時の処理
					Toast.makeText(this.requireContext(), "Permission Denied.", Toast.LENGTH_SHORT)
						.show()
				}
			}

			requestPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
		}
		*/
    }

	//----------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        handler?.hasCallbacks(runnable)?.let {
            handler?.removeCallbacks(runnable)
        }
        stopScanning()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        handler = null
        bluetoothLeScanner = null
        scanCallback = null
    }

    //----------------------------------------------------------------
    inner class AsagaoScanCallback : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let { 
            //scannerAdapter.setItems(it)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if ( result.device!= null ) if ( result.device.address != null ){
                //scannerAdapter?.addDevice(result.device)
            }

            (context as Activity).runOnUiThread {
                //scannerAdapter?.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(
                    requireContext(),
                    "${getString(R.string.bt_scan_failed)}:$errorCode",
                    Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val SCAN_PERIOD_IN_MILLIS: Long = 90_000
    }
}

