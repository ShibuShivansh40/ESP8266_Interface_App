package com.realworld.esp8266_interfacing

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var connectButton: Button
    private lateinit var sendButton1: Button
    private lateinit var sendButton2: Button
    private var bluetoothSocket: BluetoothSocket? = null
    private val hc05Address: String = "00:18:E4:40:00:06"  // Replace with your HC-05 address

    private val isBluetoothEnabled : Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.btnConnect)
        sendButton1 = findViewById(R.id.btnSend1)
        sendButton2 = findViewById(R.id.btnSend2)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        connectButton.setOnClickListener {
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(hc05Address)
            val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Bluetooth Permission Not Granted", Toast.LENGTH_LONG).show()
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            }

            bluetoothSocket = device?.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket?.connect()
            sendButton1.isEnabled = true
            sendButton2.isEnabled = true
        }

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ perms ->
            val canEnableBluetooth = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            }
            else true

            if(canEnableBluetooth && !isBluetoothEnabled){
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch (
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        sendButton1.setOnClickListener {
            try {
                bluetoothSocket?.outputStream?.write("Hell Yeah!!".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        sendButton2.setOnClickListener {
            try {
                bluetoothSocket?.outputStream?.write("Hassh! It works now".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
