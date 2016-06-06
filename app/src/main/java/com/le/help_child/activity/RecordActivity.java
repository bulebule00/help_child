package com.le.help_child.activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.le.help_child.R;
import com.le.help_child.util.NetTool;
import com.le.help_child.util.UploadFileTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RecordActivity extends Fragment {
    private Button btn_up_feature;

    private EditText edt_child;
    private EditText edt_parent;
    private EditText edt_location;
    private EditText edt_address;
    private EditText edt_feature;

    private ImageView imageView;

    private String real_pic_name;
    private String str_edt_feature;
    private String new_btn_up_feature;
    private String new_path;
    private String new_pic_name;

    private String picPath = null;

    private final Map<String, String> params = new HashMap<>();
//    File tempFile = new File(Environment.getExternalStorageDirectory(),getPhotoFileName());
//
//    private String getPhotoFileName() {
//        Date date = new Date(System.currentTimeMillis());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
//        return dateFormat.format(date) + ".jpg";
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_record, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edt_child = (EditText) this.getView().findViewById(R.id.childname_et);
        edt_parent = (EditText) this.getView().findViewById(R.id.parentname_et);
        edt_location = (EditText) this.getView().findViewById(R.id.location_et);
        edt_address = (EditText) this.getView().findViewById(R.id.address_et);

        // 上传图片
        imageView = (ImageView) this.getView().findViewById(R.id.imageView);
        btn_up_feature = (Button) this.getView().findViewById(R.id.btn_feature);

        // 给照片命名，变量为real_pic_name
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String now_time = df.format(new Date());
        StringBuilder buffer1 = new StringBuilder("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuilder sb1 = new StringBuilder();
        Random r = new Random();
        int range = buffer1.length();
        for (int i = 0; i < 5; i ++) {
            sb1.append(buffer1.charAt(r.nextInt(range)));
        }
        String randStr = sb1.toString();

        real_pic_name = now_time + randStr;

        // btn_upload为上传图片按钮
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
            }
        });

        // btn_up_feature为填写特征信息按钮
        btn_up_feature.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setView(new EditText(getActivity()));
                alertDialog.show();
                Window window = alertDialog.getWindow();
                WindowManager m = window.getWindowManager();
                Display d = m.getDefaultDisplay();
                Point size = new Point();
                d.getSize(size);
                WindowManager.LayoutParams p = window.getAttributes();
                p.height = (int) (size.y * 0.49);
                p.width = (int) (size.x* 0.9);
                window.setAttributes(p);
                window.setContentView(R.layout.custom_dialog);
                edt_feature = (EditText) window.findViewById(R.id.edt_feature);
                btn_up_feature = (Button) window.findViewById(R.id.btn_feature);

                btn_up_feature.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        str_edt_feature = edt_feature.getText().toString();
                        new_btn_up_feature = str_edt_feature;
                        alertDialog.dismiss();
                    }
                });
            }
        });

        // btn_s为确定按钮
        Button btn_Ok = (Button) this.getView().findViewById(R.id.btn_s);
        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  上传图片信息
                new_path = Environment.getExternalStorageDirectory() + "/new_finger/";

                String new_picPath = new_path + new_pic_name;
                if (new_picPath.length() > 0)
                {
                    UploadFileTask uploadFileTask = new UploadFileTask(getActivity(),"http://123.57.249.60:8080/help_child_t1/FileRecord");
                    uploadFileTask.execute(new_picPath);

                    // Log.v("nihao","nihao");
                    // 删除手机中的临时照片
//                    File f = new File(new_path, new_pic_name);
//                    if (f.exists()) {
//                        f.delete();
//                    }

                }

                //  上传特征信息
//                if(new_btn_up_feature != null && new_btn_up_feature.length() > 0)
//                {
                if(new_btn_up_feature == null||new_btn_up_feature.length() == 0)
                {
                    new_btn_up_feature = "";
                }
                    params.put("de",new_btn_up_feature);
                    params.put("cn",edt_child.getText().toString());
                    params.put("pn",edt_parent.getText().toString());
                    params.put("rl",edt_location.getText().toString());
                    params.put("ra",edt_address.getText().toString());
                    params.put("ri",real_pic_name);
                    params.put("method","record");
                    new Thread(){
                        @Override
                        public void run() {
                            InputStream is = null;
                            String url1 = "http://123.57.249.60:8080/help_child_t1/childservice";
                            try {
                                is = NetTool.getInputStreamByPost(url1,params);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            byte[] data = new byte[0];
                            try {
                                data = NetTool.readStream(is);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.i("打印信息", new String(data));
                        }
                    }.start();
              //  }
            }
        });
    }

    // 重写onActivityResult方法，用来接收回传的数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
           switch (requestCode) {
               case 1:
                   if (data != null)
                   startPhotoZoom(data.getData());
//            String path;
//            Uri uri = data.getData();
//            byte[] pic_data = null;
//            try {
//                String[] pojo = {MediaStore.Images.Media.DATA};
//                Cursor cursor = this.getActivity().getContentResolver().query(uri, pojo, null, null, null);
//                if (cursor != null) {
//                    ContentResolver cr = this.getActivity().getContentResolver();
//                    int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    cursor.moveToFirst();
//                    path = cursor.getString(colunm_index);
//                    if (path.endsWith("jpg") || path.endsWith("png")) {
//                        picPath = path;
//                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//                        imageView = (ImageView) this.getView().findViewById(R.id.imageView);
//                        //imageView.setImageBitmap(bitmap);
//                        pic_data = getBitmapByte(bitmap);
//                    } else {
//                        alert();
//                    }
//                } else {
//                    alert();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            try {
//                new_pic_name = real_pic_name + ".png";
//                SaveToSDCard(pic_data, new_pic_name);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
                   break;
               case 2:
                   if (data != null)
                       setPicToView(data);
                   break;
        }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            byte[] pic_data;
            Bitmap photo = bundle.getParcelable("data");
            pic_data = getBitmapByte(photo);
            //Drawable drawable = new BitmapDrawable(photo);
            imageView.setImageBitmap(photo);
            new_pic_name = real_pic_name + ".png";
            try {
                SaveToSDCard(pic_data, new_pic_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, 2);
    }

    private static void SaveToSDCard(byte[] data, String file) throws IOException
    {
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + "/new_finger/");

        // 如果目录不存在，则创建一个名为"finger"的目录
        if (!fileFolder.exists()) {
            fileFolder.mkdir();
        }
        final File jpgFile = new File(fileFolder, file);
        // 文件输出流
        FileOutputStream outputStream = new FileOutputStream(jpgFile);
        // 写入sd卡中
        outputStream.write(data);
        // 关闭输出流
        outputStream.close();
    }

    // 该方法用于将Bitmap格式转成二进制流
    private byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void alert() {
        Dialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle("提示")
                .setMessage("您选择的不是有效的图片")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                picPath = null;
                            }
                        }).create();
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + "/new_finger/");

        // 如果目录不存在，则创建一个名为"finger"的目录
        if (fileFolder.exists()) {
            deleteFile(fileFolder);
        }

        super.onDestroyView();
    }

    private static void  deleteFile(File file)
    {
        if((Environment.getExternalStorageState()).equals(Environment.MEDIA_MOUNTED))
        {
            if (file.exists())
            {
                if (file.isFile())
                {
                    file.delete();
                }
                // 如果它是一个目录
                else if (file.isDirectory())
                {
                    // 声明目录下所有的文件 files[];
                    File files[] = file.listFiles();
                    for (File file1 : files) { // 遍历目录下所有的文件
                        deleteFile(file1); // 把每个文件 用这个方法进行迭代
                    }
                }
                file.delete();
            }}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
