package com.ruuvi.station.feature.addtag

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.ruuvi.station.R
import com.ruuvi.station.databinding.ActivityAddTagBinding
import com.ruuvi.station.di.Injectable
import com.ruuvi.station.feature.TagSettings
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.model.TagSensorReading
import com.ruuvi.station.mqtt.MqttManager
import com.ruuvi.station.service.ScannerService
import com.ruuvi.station.util.Starter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddTagActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener, Injectable {
    lateinit var starter: Starter

    private lateinit var binder: ActivityAddTagBinding
    private val disposable = CompositeDisposable()

    @Inject
    lateinit var mqttManager: MqttManager

    companion object {
        const val TAG = "AddTagActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DataBindingUtil.setContentView(this, R.layout.activity_add_tag)

        setSupportActionBar(binder.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setActionBarOptionMenu()

        setTagListAdapter()

        disposable.add(discoverTagObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observeDatabase())
        )
        starter = Starter(this)
        starter.getThingsStarted()

        requestIgnoringBatteryOptimizations()
    }

    override fun onResume() {
        super.onResume()
        checkBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_tag, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.add_tag -> {
                showAddTagDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return onOptionsItemSelected(item)
    }

    private fun checkBluetooth(): Boolean {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            return true
        }
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivity(enableBtIntent)
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // party
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        starter.requestPermissions()
                    } else {
                        showPermissionSnackbar(this)
                    }
                    Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPermissionSnackbar(activity: Activity) {
        Snackbar.make(binder.root, getString(R.string.location_permission_needed), Snackbar.LENGTH_LONG).also {
            it.setAction(getString(R.string.settings)) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finish()
    }

    private fun setTagListAdapter() {
        binder.rvTagList.layoutManager = LinearLayoutManager(this).also { LinearLayoutManager.VERTICAL }
        binder.rvTagList.adapter = AddTagAdapter(this, onItemClickListener = { tag ->
            ScannerService.logTag(tag, this, true)
            Intent(this, TagSettings::class.java).apply {
                putExtra(TagSettings.TAG_ID, tag.id)
            }.let {
                startActivityForResult(it, 1)
            }
        })
    }

    private fun discoverTagObservable() = Observable.interval(0, 2, TimeUnit.SECONDS)

    private fun observeDatabase(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {

            }

            override fun onNext(t: Long) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, -5)
                Log.i(TAG, "Observable interval $t")
                (binder.rvTagList.adapter as AddTagAdapter).addTagList(
                        RuuviTag.getAll(true).filter {
                            it.isRemoteTag()
                        }.plus(RuuviTag.getAll(false)
                                .filter {
                                    it.updateAt.time > calendar.time.time
                                })
                                .sortedWith(kotlin.Comparator { o1, o2 -> o2.rssi - o1.rssi })
                                .also {
                                    if (it.isEmpty()) {
                                        binder.tvNoTagsMessage.visibility = View.VISIBLE
                                    } else {
                                        binder.tvNoTagsMessage.visibility = View.GONE
                                    }
                                })
            }

            override fun onError(e: Throwable) {

            }
        }
    }

    private fun setActionBarOptionMenu() {
        binder.toolbar.inflateMenu(R.menu.add_tag)
        binder.toolbar.setOnMenuItemClickListener(this)
    }

    private fun showAddTagDialog() {
        if (mqttManager.isConnected()) {
            AddTagDialog.create(this, mqttManager, R.string.title_add_tag,
                    onSubscribeSuccess = { dialog, remoteTag ->
                        remoteTag.isRemoteTag = 1
                        remoteTag.favorite = true
                        remoteTag.save()
                        val remoteTagSensorReading = TagSensorReading(remoteTag)
                        remoteTagSensorReading.save()
                        dialog.dismiss()
                    }, onSubscribeFailure = {

            }).show()
        }
    }

    private fun requestIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                if (!this.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent()
                    if (!this.isIgnoringBatteryOptimizations(packageName)) {
                        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                }
            }

        }
    }
}
