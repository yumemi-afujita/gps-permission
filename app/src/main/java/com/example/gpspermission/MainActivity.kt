package com.example.gpspermission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var onUpdateLocation: OnUpdateLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btMapSearch = findViewById<Button>(R.id.btMapSearch)
        val btMapShowCurrent = findViewById<Button>(R.id.btMapShowCurrent)

        // Button 地図検索
        btMapSearch.setOnClickListener {
            // 入力欄に入力されたキーワード文字列を取得
            val etSearchWord = findViewById<EditText>(R.id.etSearchWord)
            var searchWord = etSearchWord.text.toString()

            // 入力されたキーワードをURLエンコード
            searchWord = URLEncoder.encode(searchWord, "UTF-8")
            // MAPアプリと連携するURI文字列を生成
            val uriStr = "geo:0,0?q=${searchWord}"
            // URI文字列からURIオブジェクトを生成
            val uri = Uri.parse(uriStr)
            // Intentオブジェクト作成
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Button　地図表示
        btMapShowCurrent.setOnClickListener {
            // マップアプリと連携するURI文字列生成
            val uriStr = "geo:${latitude},${longitude}"
            // URIオブジェクト生成
            val uri = Uri.parse(uriStr)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // FusedLocationProviderClientオブジェクトを生成
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        // locationRequestオブジェクトを生成
        locationRequest = LocationRequest.create()
        locationRequest.let {
            // 位置情報の最新更新間隔設定
            it.interval = 5000
            // 位置情報の最短更新間隔設定
            it.fastestInterval = 1000
            // 位置情報取得精度を設定
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // 位置情報が変更された時のコールバックプジェクトを生成
        onUpdateLocation = OnUpdateLocation()
    }

    // 位置情報が変更された時の処理を行うコールバック
    private inner class OnUpdateLocation : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.let {
                // 直近の位置情報取得
                val location = it.lastLocation?.let { current ->
                    // 緯度取得
                    latitude = current.latitude
                    // 経度取得
                    longitude = current.longitude
                    // 画面表示
                    val tvLatitude = findViewById<TextView>(R.id.tvLatitude)
                    tvLatitude.text = latitude.toString()
                    val tvLongitude = findViewById<TextView>(R.id.tvLongitude)
                    tvLongitude.text = longitude.toString()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // ACCESS_FINE_LOCATIONに対するパーミッションダイアログでかつ許可を選択
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 再度ACCESS_FINE_LOCATIONに対する許可状態を確認　許可されていなければ処理を中止する
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                return
        }
        // 位置情報追跡を開始
        fusedLocationClient.requestLocationUpdates(locationRequest, onUpdateLocation, mainLooper)
    }

    override fun onResume() {
        super.onResume()
        // 位置情報の追跡を開始
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // ACCESS_FINE_LOCATIONの許可を求めるダイアログを表示する
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d("permission", "$permissions")
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1000)
            // onResumeメソッド終了
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, onUpdateLocation, mainLooper)
    }

    override fun onPause() {
        super.onPause()

        // 位置情報の追跡を停止
        fusedLocationClient.removeLocationUpdates(onUpdateLocation)
    }
}