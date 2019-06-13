package com.home.kotlinmqttsampledemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.home.kotlinmqttsampledemo.manager.MQTTConnectionParams
import com.home.kotlinmqttsampledemo.manager.MqttManager
import com.home.kotlinmqttsampledemo.protocols.UIUpdaterInterface
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UIUpdaterInterface {

    private var mqttManager: MqttManager? = null

    override fun resetUIWithConnection(status: Boolean) {
        ipAddressFieldEditText.isEnabled = !status
        topicFieldEditText.isEnabled = !status
        messageFieldEditText.isEnabled = status
        connectBtn.isEnabled = !status
        sendBtn.isEnabled = status
        if (status) {
            updateStatusViewWith("Connected")
        } else {
            updateStatusViewWith("Disconnected")
        }
    }

    override fun updateStatusViewWith(status: String) {
        statusLabelTextView.text = status
    }

    override fun update(message: String) {
        val text = messageHistoryView.text.toString()
        val newText: String
        newText = if (text == "") {
            message
        } else {
            text + "\n" + message
        }
        messageHistoryView.text = newText
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetUIWithConnection(false) // 預設是沒有連接
    }

    fun connect(view: View) {
        if (!ipAddressFieldEditText.text.isNullOrEmpty() && !topicFieldEditText.text.isNullOrEmpty()) {
            val host = "tcp://" + ipAddressFieldEditText.text.toString() + ":1883"
            val topic = topicFieldEditText.text.toString()
            val connectionParams = MQTTConnectionParams("MQTTSample", host, topic, "", "")
            mqttManager = MqttManager(connectionParams, applicationContext, this)
            mqttManager?.connect()
        } else {
            updateStatusViewWith("Please enter all valid fields")
        }
    }

    fun sendMessage(view: View) {
        mqttManager?.publish(messageFieldEditText.text.toString())
        messageFieldEditText.setText("")
    }
}