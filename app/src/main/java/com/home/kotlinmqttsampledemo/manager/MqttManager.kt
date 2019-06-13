package com.home.kotlinmqttsampledemo.manager

import android.content.Context
import com.home.kotlinmqttsampledemo.protocols.UIUpdaterInterface
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

class MqttManager(
    private val connectionParams: MQTTConnectionParams, context: Context,
    val uiUpdater: UIUpdaterInterface?
) {

    companion object {
        private const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
    }

    private var client = MqttAndroidClient(
        context, // 建議傳getApplicationContext(), 使mqtt的生命週期與整個應用共存亡
        connectionParams.host, connectionParams.clientId + id(context)
    )
    private var uniqueID: String? = null

    /**
     * 初始化塊(initializer blocks)
     * 初始化的代碼
     */
    init {
        client.setCallback(object : MqttCallbackExtended {
            /**
             * 連接完成
             */
            override fun connectComplete(b: Boolean, s: String) {
                uiUpdater?.resetUIWithConnection(true)
            }

            /**
             * 連接中斷
             */
            override fun connectionLost(throwable: Throwable) {
                uiUpdater?.resetUIWithConnection(false)
            }

            /**
             * 消息到達
             */
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                uiUpdater?.update(mqttMessage.toString())
            }

            /**
             * 消息成功傳輸後調用
             */
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
        })
    }

    fun connect() {
        val mqttConnectOptions = MqttConnectOptions() // 配置MQTT連接
        mqttConnectOptions.isAutomaticReconnect = true // 設置自動重新連接
        mqttConnectOptions.isCleanSession = false // //設置是否清空會話, 如果設置為false 表示服務器會保留客戶端的連接記錄, 設置為true 表示每次連接到服務器都以新的身份連接
        try {
            val params = this.connectionParams
            client.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    client.setBufferOpts(disconnectedBufferOptions)
                    subscribe(params.topic)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {}
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        try {
            client.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    uiUpdater?.updateStatusViewWith("Subscribed to Topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    uiUpdater?.updateStatusViewWith("Falied to Subscribe to Topic")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exception subscribing")
            ex.printStackTrace()
        }
    }

    fun publish(message: String) {
        try {
            val msg = "Sun: $message"
            client.publish(
                this.connectionParams.topic,
                msg.toByteArray(),
                0,
                false,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        uiUpdater?.updateStatusViewWith("Published to Topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        uiUpdater?.updateStatusViewWith("Failed to Publish to Topic")
                    }
                })
        } catch (ex: MqttException) {
            System.err.println("Exception publishing")
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun id(context: Context): String {
        if (uniqueID == null) {
            val sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE
            )
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.apply()
            }
        }
        return uniqueID!!
    }
}

data class MQTTConnectionParams(
    val clientId: String,
    val host: String,
    val topic: String,
    val username: String,
    val password: String
)