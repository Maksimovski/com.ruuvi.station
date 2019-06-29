package com.ruuvi.station.feature

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ruuvi.station.R
import com.ruuvi.station.util.RuuviPreferences
import kotlinx.android.synthetic.main.fragment_mqtt_settings.*

class MqttSettingsFragment : Fragment() {

    private var ruuviPreferences: RuuviPreferences? = null
    private lateinit var progressDialog: ProgressDialog
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context)
        context?.let {
            ruuviPreferences = RuuviPreferences(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_mqtt_settings, container, false)
        setUIListeners()
        ruuviPreferences?.let {
            rootView?.findViewById<EditText>(R.id.et_mqtt_broker_url)?.setText(it.mqttBrokerUrl, TextView.BufferType.EDITABLE)
        }
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }

    private fun setUIListeners() {
        rootView?.findViewById<EditText>(R.id.et_mqtt_broker_url)?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.toString()?.let {
                    rootView?.findViewById<Button>(R.id.btn_gateway_test)?.isEnabled = it.isNotEmpty()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //NOP
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //NOP
            }
        })

        rootView?.findViewById<Button>(R.id.btn_gateway_test)?.setOnClickListener {
            verifyMqttBroker()
        }
    }

    private fun isMqttBrokerUrlValid(url: String) = url.isNotEmpty()

    private fun verifyMqttBroker() {
        btn_gateway_test.isEnabled = false

        val url = rootView?.findViewById<EditText>(R.id.et_mqtt_broker_url)?.text.toString()
        val username = rootView?.findViewById<EditText>(R.id.et_mqtt_broker_username)?.text.toString()
        val password = rootView?.findViewById<EditText>(R.id.et_mqtt_broker_url)?.text.toString()

        if (isMqttBrokerUrlValid(url)) {
            showProgressDialog(true, getString(R.string.message_verify_broker_progress))
            // TODO When MQTT clint will be implemented, on click on verify button there should
            // MQTT client-broker connection be established in order to verify that the credentials
            // are valid before storing them in preferences.
        }
    }

    private fun showProgressDialog(show: Boolean, message: String) {
        progressDialog.let {
            if (show) {
                enableProgressMode(root_view as ViewGroup)
                if (it.isShowing) {
                    it.dismiss()
                }
                it.setMessage(message)
                it.show()
            } else {
                it.setMessage("")
                it.dismiss()
                disableProgressMode(root_view as ViewGroup)
            }
        }
    }

    private fun setEnabledRecursive(enabled: Boolean, viewGroup: ViewGroup?) {
        if (viewGroup == null) {
            return
        }
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.isEnabled = enabled
            if (child is ViewGroup) {
                setEnabledRecursive(enabled, child)
            }
        }
    }

    private fun disableProgressMode(viewGroup: ViewGroup) {
        setEnabledRecursive(true, viewGroup)
    }

    private fun enableProgressMode(viewGroup: ViewGroup) {
        setEnabledRecursive(false, viewGroup)
    }

    companion object {
        const val TAG = "MqttSettingsFragment"
        @JvmStatic
        fun newInstance() = MqttSettingsFragment()

    }
}
