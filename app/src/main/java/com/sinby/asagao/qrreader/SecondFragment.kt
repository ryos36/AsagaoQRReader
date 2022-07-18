package com.sinby.asagao.qrreader

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.navigation.fragment.findNavController
import com.sinby.asagao.qrreader.databinding.FragmentSecondBinding
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private var service: BLEService? = null
	private val mutex = Mutex()

	private val requestPermission =  registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
				if (result) {
					checkPermission()
				} else {
					Toast.makeText(this.requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show()
				}
			}

    //----------------------------------------------------------------
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            binder: IBinder
        ) {
            service = (binder as BLEService.LocalBinder).getService()

            service?.let {
				if (!it.initialize()) {
					service = null
				}
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            service = null
        }
    }

	//----------------------------------------------------------------
    private fun initializeScanning() {
        if ( bluetoothLeScanner == null ) {
            val manager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = manager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        }
    }

    // ToDo: synchronized method
	@Synchronized
	private fun startScanning() {
		if (scanCallback != null) {
			Toast.makeText(requireContext(), getString(R.string.bt_scanning), Toast.LENGTH_LONG).show()
			return
		}

		val SERVICE_UUID: String = "64251f3a-60b5-47a5-87b8-fe974bb0b89c"
		val scanFilter: ScanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
            .build()

		val settings: ScanSettings  = ScanSettings.Builder()
			.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
			.build();
		scanCallback = AsagaoScanCallback()
		bluetoothLeScanner?.startScan(listOf(scanFilter), settings, scanCallback)

		handler?.postDelayed(runnable, SCAN_PERIOD_IN_MILLIS)
    }

	@Synchronized
    private fun stopScanning() {
		if ( scanCallback != null ) {
			bluetoothLeScanner?.stopScan(scanCallback)
			scanCallback = null
			Toast.makeText(requireContext(), getString(R.string.bt_stop_scanning), Toast.LENGTH_LONG).show()
		}
    }

    //----------------------------------------------------------------
	private fun checkPermission() {
        if (checkSelfPermission( this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
			return
		}
        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
			return
		}
        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
			return
        }
		startScanning()
	}

	//----------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bluetooth のサービスにコネクトする
		/*
        val gattServiceIntent = Intent(this.requireContext(), BLEService::class.java)
        val rv = this.requireContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!rv) {
            Toast.makeText(
                    this.requireContext(),
                    getString(R.string.no_bluetooth),
                    Toast.LENGTH_LONG).show()
        }
		*/

        handler = Handler(Looper.myLooper()!!)
        initializeScanning()

    }

	//----------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        checkPermission()
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
