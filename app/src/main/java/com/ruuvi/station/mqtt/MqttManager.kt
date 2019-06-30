package com.ruuvi.station.mqtt

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.model.TagSensorReading
import com.ruuvi.station.util.AlarmChecker
import com.ruuvi.station.util.RuuviPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit

class MqttManager(private var context: Context, private var ruuviPreferences: RuuviPreferences) : LifecycleObserver {
    companion object {
        const val TAG = "MQTTManager"
        const val QOS = 1
    }

    private val gson = Gson()
    private var mqttClient: MqttAndroidClient?
    private var disposable: Disposable? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val clientId = MqttClient.generateClientId()
        Log.i(TAG, "MQTT client id: $clientId")
        mqttClient = MqttAndroidClient(context, ruuviPreferences.mqttBrokerUrl, clientId)

        connectMqttBroker(object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i(TAG, "Connection success")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i(TAG, "Connection failure: $exception")
            }
        })

        subscribeToRemoteTags()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun connect() {
        if (!isConnected()) {
            connectMqttBroker(object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.i(TAG, "Connection failure: $exception")
                }
            })

            subscribeToRemoteTags()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun disconnect() {
        disconnectMqttBroker(object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i(TAG, "Disconnection success")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i(TAG, "Disconnection failure: $exception")
            }
        })
        disposable?.dispose()
    }

    fun connectMqttBroker(actionCallback: IMqttActionListener) {
        try {
            if (mqttClient == null) {
                val clientId = MqttClient.generateClientId()
                Log.i(TAG, "MQTT client id: $clientId")
                mqttClient = MqttAndroidClient(context, ruuviPreferences.mqttBrokerUrl, clientId)
                connectMqttBroker(actionCallback)
            } else {
                mqttClient?.let {
                    val options = MqttConnectOptions()
                    options.keepAliveInterval = 100
                    options.isAutomaticReconnect = true
                    if (ruuviPreferences.mqttBrokerUsername.isNotEmpty()) {
                        options.userName = ruuviPreferences.mqttBrokerUsername
                        options.password = ruuviPreferences.mqttBrokerPassword
                    }

                    it.connect(options, actionCallback)
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnectMqttBroker(actionCallback: IMqttActionListener) {
        try {
            mqttClient?.disconnect(100, actionCallback)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribeToTopic(topic: String, qos: Int, iMqttMessageListener: IMqttMessageListener) {
        try {
            mqttClient?.subscribe(topic, qos, iMqttMessageListener)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unSubscribeFromTopic(topic: String, actionCallback: IMqttActionListener) {
        mqttClient?.unsubscribe(topic, context, actionCallback)
    }

    fun publish(tag: RuuviTag) {
        try {
            val tagJson = gson.toJson(tag)
            Log.i(TAG, "Publishing tags: $tagJson \n")
            Toast.makeText(ruuviPreferences.context, "Publishing tag: ${tag.id}", Toast.LENGTH_SHORT).show()
            mqttClient?.publish(tag.id, tagJson.toByteArray(), QOS, true)
            //mqttClient?.publish(tag.id, MqttMessage(tagJson.toByteArray()))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribeToTopic(tagAddress: String): Observable<RuuviTag> {
        return Observable.create { emitter ->
            try {
                mqttClient?.subscribe(tagAddress, QOS) { topic, message ->
                    if (tagAddress == topic) {
                        message?.let {
                            val ruuviTag = gson.fromJson(String(message.payload), RuuviTag::class.java)
                            if (ruuviTag.id == null) {
                                emitter.onError(IllegalStateException("Invalid tag format: ${String(message.payload)}"))
                            } else {
                                Log.i(TAG, "Remote tag discovered: ${String(message.payload)}")
                                emitter.onNext(ruuviTag)
                            }
                        }
                        emitter.onComplete()
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
                Log.i(TAG, "Subscription to $tagAddress failed: $e")
                emitter.onError(e)
                emitter.onComplete()
            }
        }
    }

    fun registerRemoteTag(tagAddress: String): Observable<RuuviTag> {
        return Observable.create { emitter ->
            try {
                mqttClient?.subscribe(tagAddress, QOS) { topic, message ->
                    if (tagAddress == topic) {
                        message?.let {
                            val ruuviTag = gson.fromJson(String(message.payload), RuuviTag::class.java)
                            if (ruuviTag.id == null) {
                                emitter.onError(IllegalStateException("Invalid tag format: ${String(message.payload)}"))
                            } else {
                                Log.i(TAG, "Remote tag discovered: ${String(message.payload)}")
                                emitter.onNext(ruuviTag)
                                unSubscribeFromTopic(tagAddress, object : IMqttActionListener {
                                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                                        emitter.onComplete()
                                    }

                                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                        emitter.onComplete()
                                    }
                                })
                            }
                        }
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
                Log.i(TAG, "Subscription to $tagAddress failed: $e")
                emitter.onError(e)
                emitter.onComplete()
            }
        }
    }

    private fun subscribeToRemoteTags() {
        disposable = Observable.interval(0, 5, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(
                        object : DisposableObserver<Long>() {
                            override fun onComplete() {

                            }

                            override fun onNext(t: Long) {
                                RuuviTag.getAll(true)
                                        .filter { it.isRemoteTag() }
                                        .forEach {
                                            if (isConnected()) {
                                                subscribeToTopic(it.id).subscribe({ remoteTag ->
                                                    remoteTag.isRemoteTag = 1
                                                    remoteTag.favorite = true
                                                    remoteTag.save()
                                                    val remoteTagSensorReading = TagSensorReading(remoteTag)
                                                    remoteTagSensorReading.save()
                                                    AlarmChecker.check(remoteTag, context)
                                                }, {})
                                            }
                                        }
                            }

                            override fun onError(e: Throwable) {

                            }
                        }
                )

    }

    fun isConnected() = mqttClient?.isConnected ?: false
}