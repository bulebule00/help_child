package com.le.help_child.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.le.help_child.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Timer;
import java.util.TimerTask;

public class PlaceActivity extends Fragment implements View.OnClickListener,AMap.OnMarkerDragListener,
        AMap.OnMapLoadedListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter {
    private MapView mapView;
    private AMap aMap;

    private static final LatLng DONG = new LatLng(48.428702,135.106271);// 北京市经纬度
    private static final LatLng XI = new LatLng(39.178862, 73.4072);// 北京市中关村经纬度


    private double longt;//经度
    private double lant;//纬度
    private String pname;//照片名
    private String response;
    private Bundle ss;

    String url;
    private JSONArray jsonhelp = null;
    private JSONArray jsonrecord = null;
    private String hname;//help图片名
    private String rname;//record图片名
    private String htime;//help时间
    private String rcname;//record时间
    private String rpname;//record时间
    private String raddress;//record时间

    private static final int SHOW_RESPONSE = 0;

    private String str;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建默认的ImageLoader配置参数??
        ImageLoaderConfiguration configuration=ImageLoaderConfiguration.createDefault(this.getContext());
        //Initialize?ImageLoader?with?configuration.??
        ImageLoader.getInstance().init(configuration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ss = savedInstanceState;
        return inflater.inflate(R.layout.activity_place, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView = (MapView)this.getView().findViewById(R.id.map);


        mapView.onCreate(savedInstanceState);// 此方法必须重写



        //新建Handler的对象，在这里接收Message，然后更新TextView控件的内容
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SHOW_RESPONSE:
                        response = (String) msg.obj;
                        //textView_response.setText(response);
                        JSONTokener jsonParser = new JSONTokener(response);
                        JSONObject jsonObject;
                        try {
                            jsonObject = (JSONObject) jsonParser.nextValue();
                            jsonhelp = jsonObject.getJSONArray("help");
                            jsonrecord = jsonObject.getJSONArray("record");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        init();
                        break;
                    default:
                        break;
                }
            }

        };

        //方法：发送网络请求，获取百度首页的数据。在里面开启线程

        new Thread(new Runnable() {

            @Override
            public void run() {
                //用HttpClient发送请求，分为五步
                //第一步：创建HttpClient对象
                HttpClient httpCient = new DefaultHttpClient();
                //第二步：创建代表请求的对象,参数是访问的服务器地址
//                HttpGet httpGet = new HttpGet("http://www.baidu.com");
                HttpGet httpGet = new HttpGet("http://123.57.249.60:8080/help_child_t1/childservice?method=place");

                try {
                    //第三步：执行请求，获取服务器发还的相应对象
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串

                        //在子线程中将Message对象发出去
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response;
                        handler.sendMessage(message);
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();//这个start()方法不要忘记了
    }



    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            //窗口显示地图范围
            LatLng marker1 = new LatLng(34.48995,108.597783);
            LatLngBounds bounds = new LatLngBounds.Builder().include(marker1).include(DONG).include(XI).build();
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
        }
        setUpMap();
    }


    private void setUpMap() {
        aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        addMarkersToMap();// 往地图上添加marker
        addMarkersToMap();

        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        addMarkersToMap();

                        break;
                }
                super.handleMessage(msg);
            }
        };

        final Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            int i=0;
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                i++;
                if(i==5){
                    i=0;
                    timer.cancel();
                }
            }
        };

//Timer timer = new Timer(true);
        timer.schedule(task,5000,5000); //延时1000ms后执行，1000ms执行一次





    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {

       /* str = response;

        String[] A =str.split("/|,");//返回字符串分隔

        for (int i = 4; i < A.length; i=i+3) {

            longt =Double.parseDouble(A[i].substring(1));
            lant =Double.parseDouble(A[i+1].substring(0,A[i+1].length()-1));
            pname = A[i-1];

            url = "http://10.9.34.126:8080/help_child_t1/help_image/"+pname+".png";
            latlng = new LatLng(lant,longt);


            View mview = getLayoutInflater(ss).inflate(R.layout.activity_marker, null);

            ImageView imageView = (ImageView) mview.findViewById(R.id.marker);

            Picasso.with(this.getActivity())
                    .load(url)
                    .error(R.drawable.error)
                    .into(imageView);

            MarkerOptions markerOptions = new MarkerOptions().position(latlng).
                    icon(BitmapDescriptorFactory.fromView(mview)).snippet("");

            // 添加到地图上
            Marker marker = aMap.addMarker(markerOptions);
            getInfoWindow(marker);
            marker.showInfoWindow();// 设置默认显示一个infowinfow

        }*/


        //help 图片显示
        LatLng latlng;
        for (int i = 0; i < jsonhelp.length(); i++) {
            JSONObject jsonhelp1;
            jsonhelp1 = ((JSONObject)jsonhelp.opt(i));

            try {
                str=jsonhelp1.getString("location");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String[] A =str.substring(1,str.length()-1).split(",");//返回字符串分隔
            double hlongt = Double.valueOf(A[0]);
            double hlant = Double.valueOf(A[1]);

            try {
                hname = jsonhelp1.getString("img");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            latlng = new LatLng(hlant, hlongt);
//            url = "http://123.57.249.60:8080/help_child_t1/help_image/"+hname+".png";



            View mview = getLayoutInflater(ss).inflate(R.layout.activity_marker, null);
//
//            ImageView imageView = (ImageView) mview.findViewById(R.id.marker);
//
//            Picasso.with(this.getActivity())
//                    .load(url)
//                    .resize(50,50)
//                    .placeholder(R.drawable.loading)
//                    .error(R.drawable.error)
//                    .tag(this.getActivity())
//                    .into(imageView);



            final ImageView mImageView = (ImageView)  mview.findViewById(R.id.marker);
            String imageUrl = "http://123.57.249.60:8080/help_child_t1/help_image/"+hname+".png";

            //显示图片的配置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading)
                    .showImageOnFail(R.drawable.error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            //ImageLoader.getInstance().displayImage(imageUrl, mImageView, options);


            ImageSize mImageSize = new ImageSize(50, 50);


            ImageLoader.getInstance().loadImage(imageUrl, mImageSize, options, new SimpleImageLoadingListener(){

                @Override
                public void onLoadingComplete(String imageUri, View view,
                                              Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    mImageView.setImageBitmap(loadedImage);
                }

            });




            try {
                htime= jsonhelp1.getString("time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MarkerOptions markerOptions = new MarkerOptions().position(latlng).
                    icon(BitmapDescriptorFactory.fromView(mview)).title("上传时间："+ htime);

            // 添加到地图上
            Marker marker = aMap.addMarker(markerOptions);
            getInfoWindow(marker);


        }

        //record 图片显示
        for (int i = 0; i < jsonrecord.length(); i++) {
            JSONObject jsonhelp1;
            jsonhelp1 = ((JSONObject)jsonrecord.opt(i));

            try {
                str=jsonhelp1.getString("location");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String[] A =str.substring(1,str.length()-1).split(",");//返回字符串分隔
            double rlongt = Double.valueOf(A[0]);
            double rlant = Double.valueOf(A[1]);

            try {
                rname = jsonhelp1.getString("img");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            latlng = new LatLng(rlant, rlongt);

//            url = "http://123.57.249.60:8080/help_child_t1/record_image/"+rname+".png";
            View mview = getLayoutInflater(ss).inflate(R.layout.activity_marker_record, null);
//
//            ImageView imageView = (ImageView) mview.findViewById(R.id.marker);
//
//            Picasso.with(this.getActivity())
//                    .load(url)
//                    .placeholder(R.drawable.loading)
//                    .error(R.drawable.error)
//                    .into(imageView);




            final ImageView mImageView = (ImageView)  mview.findViewById(R.id.marker);
            String imageUrl = "http://123.57.249.60:8080/help_child_t1/record_image/"+rname+".png";

            //显示图片的配置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading)
                    .showImageOnFail(R.drawable.error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            //ImageLoader.getInstance().displayImage(imageUrl, mImageView, options);


            ImageSize mImageSize = new ImageSize(50, 50);


            ImageLoader.getInstance().loadImage(imageUrl, mImageSize, options, new SimpleImageLoadingListener(){

                @Override
                public void onLoadingComplete(String imageUri, View view,
                                              Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    mImageView.setImageBitmap(loadedImage);
                }

            });





            try {
                rcname = jsonhelp1.getString("c_name");
                rpname = jsonhelp1.getString("p_name");
                raddress = jsonhelp1.getString("address");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MarkerOptions markerOptions = new MarkerOptions().position(latlng).
                    icon(BitmapDescriptorFactory.fromView(mview))
                    .title("失踪姓名："+rcname)
                    .snippet("联系人："+rpname+"\n联系方式："+raddress);

            // 添加到地图上
            Marker marker = aMap.addMarker(markerOptions);
            getInfoWindow(marker);


        }








    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
/*    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getLayoutInflater(ss).inflate(R.layout.custom_info_window, null);

        render(marker, infoWindow);
        return infoWindow;
    }*/

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 自定义infowinfow窗口
     */
    /*public void render(Marker marker, View view) {
       *//* ImageView imageView = (ImageView) view.findViewById(R.id.badge);
        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        //imageView.setImageResource(R.drawable.marker_foot);
        String pname1 = pname;
        Picasso.with(this.getActivity())
                .load("http://10.9.34.126:8080/help_child_t1/help_image/13.png")
                .error(R.drawable.error)
                .into(imageView);*//*

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    titleText.length(), 0);
            titleUi.setTextSize(12);
            titleUi.setText(titleText);

        } else {
            titleUi.setText("");
        }
        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    snippetText.length(), 0);
            snippetUi.setTextSize(12);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
    }*/

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.isInfoWindowShown()){
            marker.hideInfoWindow();//这个是隐藏infowindow窗口的方法

        }

    }

    @Override
    public void onMapLoaded() {
        // 设置所有maker显示在当前可视区域地图中
        /*LatLngBounds bounds = new LatLngBounds.Builder()
                .include(XI).include(DONG).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 1));*/


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
