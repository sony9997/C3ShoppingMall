package com.c3.jbz.util;

import android.graphics.Bitmap;

import com.c3.jbz.BuildConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by hedong on 2017/10/10.
 */

public final class ToolsUtil {
    /**
     * 获取bitmap
     *
     * @param imageUrl
     * @return
     */
    public static void getBitmap(String imageUrl, ImageLoadingListener listener) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().loadImage(imageUrl, options, listener);
    }

    /**
     * 获取bitmap
     *
     * @param imageUrl
     * @return
     */
    public static File getBitmapFileSync(String imageUrl) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().loadImageSync(imageUrl, options);
        return ImageLoader.getInstance().getDiskCache().get(imageUrl);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 创建指定数量的随机字符串
     * @param numberFlag 是否是数字
     * @param length
     * @return
     */
    public static String createRandom(boolean numberFlag, int length){
        String retStr = "";
        String strTable = numberFlag ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
        int len = strTable.length();
        boolean bDone = true;
        do {
            retStr = "";
            int count = 0;
            for (int i = 0; i < length; i++) {
                double dblR = Math.random() * len;
                int intR = (int) Math.floor(dblR);
                char c = strTable.charAt(intR);
                if (('0' <= c) && (c <= '9')) {
                    count++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 2) {
                bDone = false;
            }
        } while (bDone);

        return retStr;
    }

    /**
     * 微信支付签名算法sign
     * @param parameters
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String createWXSign(SortedMap<Object,Object> parameters){
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if(null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + BuildConfig.wxBizSecretKey);//KEY是商户秘钥
        String characterEncoding = "UTF-8";
        String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }
}
