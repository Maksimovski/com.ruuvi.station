package com.ruuvi.station.feature.addtag

import android.app.AlertDialog
import android.content.Context
import android.support.annotation.StringRes
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.ruuvi.station.R
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.mqtt.MqttManager
import io.reactivex.disposables.Disposable

object AddTagDialog {

    private var disposable: Disposable? = null

    fun create(
            context: Context,
            mqttManager: MqttManager,
            @StringRes title: Int,
            onSubscribeSuccess: (dialog: AlertDialog, tag: RuuviTag) -> Unit,
            onSubscribeFailure: () -> Unit
    ): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_tag, null)
        val alertDialog = AlertDialog.Builder(context)
                .setTitle(title)
                //.setMessage(message)
                .setNegativeButton(R.string.add_tag_dialog_action_cancel) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(R.string.add_tag_dialog_action_subscribe) { _, _ -> }

        view.findViewById<EditText>(R.id.et_tag_address)?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                view.findViewById<TextView>(R.id.tv_message)?.text = ""
            }
        })

        alertDialog.setView(view)
        val dialog = alertDialog.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val tagAddress = view.findViewById<EditText>(R.id.et_tag_address)?.text.toString()
                if (isTagAddressValid(tagAddress)) {
                    view.findViewById<LinearLayout>(R.id.ll_input_address_holder)?.visibility = View.GONE
                    view.findViewById<LinearLayout>(R.id.ll_progress_bar_holder)?.visibility = View.VISIBLE
                    disposable = mqttManager.registerRemoteTag(tagAddress).subscribe({ tag ->
                        onSubscribeSuccess(dialog, tag)
                    }, {
                        view.findViewById<LinearLayout>(R.id.ll_input_address_holder)?.visibility = View.VISIBLE
                        view.findViewById<LinearLayout>(R.id.ll_progress_bar_holder)?.visibility = View.GONE
                        view.findViewById<TextView>(R.id.tv_message)?.text = context.getString(R.string.error_message_subscribe_tag)
                        onSubscribeFailure()
                    })
                } else {
                    view.findViewById<TextView>(R.id.tv_message)?.text = context.getString(R.string.error_message_invalid_tag_address)
                }
            }
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnDismissListener {
            disposable?.dispose()
        }
        return dialog
    }

    private fun isTagAddressValid(tagAddress: String) = tagAddress.isNotEmpty()
}