package com.sinby.asagao.qrreader

import android.Manifest
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

	//----------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission( this.requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
			val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
				if (result) {
					Toast.makeText(
						this.requireContext(),
						"Permission Accepted.",
						Toast.LENGTH_SHORT
					).show()
					//bluetoothLeScanner?.startScan(scanCallback)
				} else {
					Toast.makeText(this.requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show()
				}
			}

			requestPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
/*
{
                STATE_INIT -> {
                    (context as Activity).runOnUiThread {
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

						requestPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
					}
				}

                STATE_HAS_BLUETOOTH_SCAN_PERMISSION -> {
                    getString(R.string.button_has_scan)
                }
                STATE_HAS_BLUETOOTH_CONNECT_PERMISSION -> {
                    getString(R.string.button_start_scan)
                }
                else -> {
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
            }
            */
