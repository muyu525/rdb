package com.expression.tool.rdb.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommonUtils {
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean startApp(Context context, String packagename) {
        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packagename);
        if (null != LaunchIntent) {
            context.startActivity(LaunchIntent);

            return true;
        } else {
            return false;
        }
    }

    public static boolean exitsAppAndActivity(Context ctt, String packagename, String activityName) {
        boolean exists = false;
        if (ctt == null || TextUtils.isEmpty(packagename) || TextUtils.isEmpty(activityName)) {
            return exists;
        }
        PackageManager packageMgr = ctt.getPackageManager();
        List<PackageInfo> list = packageMgr.getInstalledPackages(0);
        for (int i = 0; i < list.size(); i++) {
            PackageInfo info = list.get(i);
            String temp = info.packageName;
            if (temp.equals(packagename)) {
                if (info != null && info.activities != null) {
                    for (ActivityInfo ai : info.activities) {
                        Log.e("wechat", ai.name);
                        System.out.println(ai.name);
                        if (ai.name != null && ai.name.contains(activityName)) {
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return exists;
    }


    public static void openUrl(Context context, String url) {
        if (url != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//	        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            }
        }
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        if (context != null && intent != null) {
            final PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
        return false;
    }

    public static String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddr = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d",
                (ipAddr & 0xff), (ipAddr >> 8 & 0xff),
                (ipAddr >> 16 & 0xff), (ipAddr >> 24 & 0xff));
    }

    public static String getMetaOfApplication(Context ctt, String name) {
        ApplicationInfo appInfo = null;
        String msg = "";
        try {
            appInfo = ctt.getPackageManager().getApplicationInfo(ctt.getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            Object obj = appInfo.metaData.get(name);
            if (obj instanceof Long) {
                return msg = (Long) obj + "";
            }
            if (obj instanceof Integer) {
                return msg = (Integer) obj + "";
            }
            if (obj instanceof String) {
                return msg = (String) obj;
            }
        }
        return msg;
    }

    public static String getVersionName(Context ctt) {
        StringBuffer sb = new StringBuffer();
        PackageManager manager = ctt.getPackageManager();
        String pkgName = ctt.getPackageName();
        try {
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            sb.append(info.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static int getVersionCode(Context ctt) {
        int verCode = 1;
        PackageManager manager = ctt.getPackageManager();
        String pkgName = ctt.getPackageName();
        try {
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            verCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            try {
                String pkName = context.getPackageName();
                info = pm.getPackageInfo(pkName, PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return info;
    }


    public static PackageInfo getExternalPackageInfo(Context context, String path) {
        PackageInfo info = null;
        PackageManager pm = context.getPackageManager();
        try {
            info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES | PackageManager.GET_PERMISSIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static boolean hasPermission(Context context, String permission) {
        PackageInfo pkgInfo = getPackageInfo(context);
        if (pkgInfo != null) {
            for (int i = 0; i < pkgInfo.requestedPermissions.length; i++) {
                if (pkgInfo.requestedPermissions[i].equalsIgnoreCase(permission)) {
                    return true;
                }

            }
        }
        return false;
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        Point point = new Point(dm.widthPixels, dm.heightPixels);

        dm = null;

        return point;
    }

    public static String getDeviceID(Context context) {
        String devId = "1234567890";
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            devId = telephonemanage.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devId;
    }

    public static Bitmap imageScaleLimited(Bitmap bitmap, int maxW, int maxH,
                                           boolean recycle) {
        if (bitmap == null) {
            return bitmap;
        }
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) maxW) / src_w;
        float scale_h = ((float) maxH) / src_h;
        scale_h = Math.min(scale_w, scale_h);
        scale_w = scale_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        if (recycle && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return dstbmp;
    }


    public static String getSubscriberId(Context context) {
        String subId = "";
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            subId = telephonemanage.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subId;
    }

    public static String getNativePhoneNumber(Context context) {
        String NativePhoneNumber = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            NativePhoneNumber = telephonyManager.getLine1Number();
        } catch (Exception e) {

        } catch (Error e) {

        }
        if (null == NativePhoneNumber) {
            NativePhoneNumber = "";
        } else if (NativePhoneNumber.startsWith("+86")) {
            NativePhoneNumber = NativePhoneNumber.substring(3);
        }
        return NativePhoneNumber;
    }

    public static boolean installNormal(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
            return false;
        }
        try {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getStringTime(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(new Date(date));
        return str;
    }

    public static String getCurrentStringTime() {
        long date = System.currentTimeMillis();
        return getStringTime(date);
    }

}
