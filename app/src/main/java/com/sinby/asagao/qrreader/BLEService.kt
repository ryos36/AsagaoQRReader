package com.sinby.asagao.qrreader

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

private const val SERVICE_UUID = "64251f3a-60b5-47a5-87b8-fe974bb0b89c"
private const val CHARACTERISTIC_UUID = "2390a218-6837-4982-9bb0-83dd08554104"

class BLEService : Service() {
    private var connectionState = STATE_DISCONNECTED
	val status
        get() = this.connectionState

    private val binder = LocalBinder()
    private val callback = object : BluetoothGattCallback() {
		override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED
                gatt?.discoverServices()
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED
			}
		}

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered received: " + status)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if ((status == BluetoothGatt.GATT_SUCCESS) && (characteristic != null)) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
			Log.i(TAG, "onCharacteristicRead received: " + status)
        }

        /*
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if ((status == BluetoothGatt.GATT_SUCCESS) && (characteristic != null)) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }
        */
	}

    private var adapter: BluetoothAdapter? = null
    private var gadd: BluetoothGatt? = null

	//----------------------------------------------------------------
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        val data = characteristic.value
        data?.let {
            if ( data.size == 24 ) {
                var fa = FloatArray(6)
                for( i in 0..5 ) {
                    var v: Int = 0
                    for( j in 0..3 ) {
                        v = v shl 8
                        v = v or data.get(i*4+(3-j)).toInt()
                    }
                    fa.set(i, Float.fromBits(v))
                    //fa.set(i, 0.0F)
                }
                intent.putExtra(FLOAT_ARRAY_DATA, fa)
                sendBroadcast(intent)
            }
            //val stringBuilder = StringBuilder(data.size)
            /*
            for (byteChar in data)
                stringBuilder.append(String.format("%02X ", byteChar))
            intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
            */
        }
    }

	//----------------------------------------------------------------
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        close()
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BLEService {
            return this@BLEService
        }
    }

	//----------------------------------------------------------------
    fun initialize(): Boolean {
        // getDefaultAdapter is deprecated
        // adapter = BluetoothAdapter.getDefaultAdapter()

        if ( adapter == null ) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            adapter = bluetoothManager.getAdapter()
        }
       
        if (adapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    fun connect(address: String): Boolean {
		adapter?.let {
			try {
				val device = adapter!!.getRemoteDevice(address)
				gadd = device.connectGatt(this, false, callback)
                gadd?.let {
                    connectionState = STATE_CONNECTING
                }
                return true
			} catch (exception: IllegalArgumentException) {
				Log.w(TAG, "Device not found with provided address.")
                return false
			}
		} ?: run {
            Log.e(TAG, "BluetoothAdapter not initialized")
            return false
		}
	}
    
    fun disconnect() {
        gadd?.let {
            gadd?.disconnect()
		}
    }
    
    fun close() {
        gadd?.let {
            gadd?.close()
            gadd = null
        }
    }

    fun writeCharacteristic(uuidCharacteristic: UUID, value: ByteArray, writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
        gadd?.let { g->
            g.getService(UUID_S)?.getCharacteristic(uuidCharacteristic)?.let { wrchar ->
                wrchar.setValue(value)
                wrchar.writeType = writeType
                g.writeCharacteristic(wrchar)
            }
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
		gadd?.readCharacteristic(characteristic)
    }

    fun readCharacteristic() {
		gadd?.let { g ->
            g.getService(UUID_S)?.let { s ->
                s.getCharacteristic(UUID_C)?.let { characteristic ->
                    g.readCharacteristic(characteristic)
                }
            }
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED = "com.sinby.asagao.qrreader.BLEService.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.sinby.asagao.qrreader.BLEService.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.sinby.asagao.qrreader.BLEService.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.sinby.asagao.qrreader.BLEService.ACTION_DATA_AVAILABLE"

        const val EXTRA_DATA = "com.sinby.asagao.qrreader.BLEService.EXTRA_DATA"
        const val FLOAT_ARRAY_DATA = "com.sinby.asagao.qrreader.BLEService.FLOAT_ARRAY_DATA"

        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2


        private val TAG = BLEService::class.java.simpleName
        val UUID_S = UUID.fromString(SERVICE_UUID)
        val UUID_C = UUID.fromString(CHARACTERISTIC_UUID)
    }
}
