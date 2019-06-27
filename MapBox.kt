package com.pwrd.dls.marble.moudle.timemap.map.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.google.gson.JsonArray
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.gson.AutoValueGson_GeoJsonAdapterFactory
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.ImageSource
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import com.pwrd.dls.marble.R
import com.pwrd.dls.marble.common.base.BaseActivity
import com.pwrd.dls.marble.common.util.ResUtils
import com.pwrd.dls.marble.mapbox.clustering.ClusterManager
import com.pwrd.dls.marble.mapbox.clustering.Point
import com.pwrd.dls.marble.moudle.timemap.map.helper.CustomMarker
import com.pwrd.dls.marble.moudle.timemap.map.helper.bean.MapPoint
import com.pwrd.dls.marble.moudle.timemap.map.model.bean.Geometry
import com.pwrd.dls.marble.moudle.timemap.map.model.bean.mapbox.FeatureBean
import kotlinx.android.synthetic.main.test_activity.*

class MapTestActivity : BaseActivity() {
    private var csid=""
    private var presid=""
    private var mapboxMap: MapboxMap? = null
    private lateinit var testL:SymbolLayer
    private var scale=1.0f
    override fun getContentViewID(): Int = R.layout.test_activity

    override fun getTopbarID(): Int = 0

    override fun initDatas(savedInstanceState: Bundle?) {

    }

    override fun initViews(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        button.setOnClickListener {
            mapboxMap?.style?.removeSource(presid)
        }
        initMap()
    }

    private fun initMap() {
        mapView.getMapAsync {
            mapboxMap = it
            val set=it.uiSettings
            set.isCompassEnabled=false
            set.isTiltGesturesEnabled=false
            set.isLogoEnabled=false
            set.isAttributionEnabled=false
            set.isRotateGesturesEnabled=false
            mapboxMap?.addMarker(MarkerOptions()
                    .position(LatLng(30.800, 120.911))
                    .icon(IconFactory.getInstance(this).fromResource(R.drawable.icon_my_location))
                    .title("这里是华东")
                    .snippet("欢迎来到这里"))
            it.addOnScaleListener(object :MapboxMap.OnScaleListener{
                override fun onScaleBegin(detector: StandardScaleGestureDetector) {

                }

                override fun onScaleEnd(detector: StandardScaleGestureDetector) {

                }

                override fun onScale(detector: StandardScaleGestureDetector) {
                    scale*=detector.scaleFactor
                    log(it.projection.calculateZoom(1f))
                    testL.setProperties(PropertyFactory.iconSize(scale))
                }
            })
            it.addOnMapClickListener(object : MapboxMap.OnMapClickListener {
                override fun onMapClick(point: LatLng): Boolean {
                    val p=mapboxMap?.projection?.toScreenLocation(LatLng(point))
                    p?.let {
                        val list=mapboxMap?.queryRenderedFeatures(p,"lplp")
                        log(list)
                    }
                    return false
                }
            })
            val latLngQuad = LatLngQuad(
                    LatLng(46.437, -80.425),
                    LatLng(46.437, -71.516),
                    LatLng(37.936, -71.516),
                    LatLng(37.936, -80.425)
            )


            /*mapView.postDelayed({
                mapboxMap?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(90.0, 90.0)), 3000)
            }, 1000)
            mapView.postDelayed({
                mapboxMap?.easeCamera(CameraUpdateFactory.newLatLng(LatLng(0.0, 0.0)), 3000)
            }, 5000)
            val position = CameraPosition.Builder()
                    .target(LatLng(90.0, 90.0))
                    .zoom(3.0)
                    .tilt(30.0)
                    .bearing(90.0)
                    .build()
            val bounds = LatLngBounds.Builder()
                    .include(LatLng(0.0, 0.0))
                    .include(LatLng(10.0, 0.0))
                    .include(LatLng(0.0, 10.0))
                    .include(LatLng(10.0, 10.0))
                    .build()

            mapView.postDelayed({
                mapboxMap?.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
            }, 9000)
            mapView.postDelayed({
                mapboxMap?.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000)
            }, 10000)*/
            it.setStyle(Style.Builder().fromUrl("https://maptile.allhistory.com/styles/dlsgis-his-regime-AD1949/style.json?v=3"), object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    style.addImage("xxx",ResUtils.getBitmap(R.drawable.book_cover_blue))
                    style.addSource(ImageSource("f",latLngQuad,ResUtils.getBitmap(R.drawable.default_user_profile)))

                    val f1=Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(0.0,0.0))
                    val f2=Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(20.0,0.0))
                    val f3=Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(0.0,20.0))
                    val s=GeoJsonSource("source", FeatureCollection.fromFeatures(arrayOf(f1,f2,f3)))


                    val rsts=RasterSource("raster", TileSet("tileSet","https://mt3.google.cn/vt?pb=" +
                            "!1m4!1m3!1i{z}!2i{x}!3i{y}!2m3!1e4!2st!3i132!2m3!1e0!2sr!3i285205865!3m14!2szh-CN!" +
                            "3sCN!5e18!12m1!1e63!12m3!1e37!2m1!1ssmartmaps!12m4!1e26!2m2!1sstyles!2zcy50OjN8" +
                            "cC52Om9mZixzLnQ6MXxwLnY6b2ZmLHMudDoyfHAudjpvZmY!4e0?format=image/png"))



                    val geo=GeoJsonSource("geo", FeatureCollection.fromFeatures(arrayOf(makeFeature())))
                    style.addSource(rsts)
                    style.addSource(geo)

                    val circleLayer=CircleLayer("circleLayer","source")

                    circleLayer.setProperties(
                            PropertyFactory.fillColor(Color.RED),
                            PropertyFactory.circleRadius(20f),
                            PropertyFactory.visibility(Property.VISIBLE)

                    )
                    style.addSource(s)
                    style.addLayer(RasterLayer("lid","f"))

                    style.addLayer(circleLayer)
                    style.addLayer(RasterLayer("rasterLayer","raster"))

                    testL=SymbolLayer("lplp","geo")
                    testL.setProperties(
                            PropertyFactory.iconImage(Expression.get("icon")),
                            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconOffset(Expression.get("offset"))
                    )
                    style.addLayer(testL)

                    style.layers.forEach {
                        log("layer->" + it.id)
                    }





                    style.getLayer("lake")?.setProperties(
                            PropertyFactory.fillColor(Color.RED)
                    )

                }
            })
        }
    }
    private fun makeFeature():Feature{
        val f=Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(0.0,0.0))
        f.addStringProperty("icon","xxx")
        val offset=JsonArray(2)
        offset.add(10f)
        offset.add(10f)
        f.addProperty("offset",offset)
        return f
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        fun log(any: Any?) {
            Log.i("log", any?.toString())
        }

        @JvmStatic
        fun actionStart(ctx: Context) {
            ctx.startActivity(Intent(ctx, MapTestActivity::class.java))
        }
    }
}