package com.xeasy.noticefix.utils;

import static android.graphics.Color.WHITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.xeasy.noticefix.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.Callable;


/**
 * 处理图片的工具类.
 */
public class ImageTools {

    /**
     * 图片去色,返回灰度图片
     *
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {

        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(f);
        canvas.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 获取单色位图
     */
    public static Bitmap getSinglePic(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returnBMP = Bitmap.createBitmap(inputBMP.getWidth(),
                inputBMP.getHeight(), Bitmap.Config.ARGB_8888);
        // 127;//曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
        int lightNumber;

        //计算中间值
        int allLightNumber = 0;
        int hasColor = 0;
        for (int i = 0; i < colorTemp.length; i++) {
            if (pix[i] != 0) {
                int r = Color.red(pix[i]);
                int g = Color.green(pix[i]);
                int b = Color.blue(pix[i]);
                allLightNumber += (r + g + b) / 3;
                hasColor++;
            }
        }

        // 中间值等于总的除以有的色
        if (hasColor > 0) {
            lightNumber = allLightNumber / hasColor;

            // 判断四边中点是否是白色 是白色判断为背景 执行反色
            // 上横边
            int up = pix[(int) (inputBMP.getWidth() * 1.5)];
            int down = pix[(int) (inputBMP.getWidth() * inputBMP.getHeight() - inputBMP.getWidth() * 1.5)];
            int left = pix[((inputBMP.getHeight() / 2) * inputBMP.getWidth()) - inputBMP.getWidth() + 2];
            int right = pix[((inputBMP.getHeight() / 2) * inputBMP.getWidth()) - 1];
//            XposedBridge.log("up = " + up);
//            XposedBridge.log("down = " + down);
//            XposedBridge.log("left = " + left);
//            XposedBridge.log("right = " + right);
//            XposedBridge.log("width = " + inputBMP.getWidth());
//            XposedBridge.log("height = " + inputBMP.getHeight());

            // true为有白边框
            boolean colorReversal = isWhite(up, lightNumber)
                    && isWhite(down, lightNumber)
                    && isWhite(left, lightNumber)
                    && isWhite(right, lightNumber);
            // true为有其他颜色边框
            boolean hasPadding = isBlackAndNotTransparent(up, lightNumber)
                    && isBlackAndNotTransparent(down, lightNumber)
                    && isBlackAndNotTransparent(left, lightNumber)
                    && isBlackAndNotTransparent(right, lightNumber);
            // true为透明
            boolean transparent = up == 0
                    && down == 0
                    && left == 0
                    && right == 0;

            System.out.println(colorReversal);
            Integer first = null;
            for (int i = 0; i < colorTemp.length; i++) {
                if (pix[i] != 0) {
                    int r = Color.red(pix[i]);
                    int g = Color.green(pix[i]);
                    int b = Color.blue(pix[i]);

                    //  todo ??? Common part can be extracted from 'if'
                    if (hasPadding || colorReversal) {
                        if (null == first) {
                            if (r + g + b > 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                // 小设为白
                                first = 0;
                            } else {
                                // 大设为白
                                first = 1;
                            }
                        }

                        if (first == 0) {
                            if (r + g + b <= 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                colorTemp[i] = WHITE;
                            }
                        } else {
                            if (r + g + b > 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                colorTemp[i] = WHITE;
                            }
                        }

                    } else {

                        if (null == first) {
                            // 如果第一次遇到的色是深色底 设为白
                            if (r + g + b <= 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                first = 0;
                            } else {
                                first = 1;
                            }
                        }

                        if (first == 0) {
                            if (r + g + b <= 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                colorTemp[i] = WHITE;
                            }
                        } else {
                            if (r + g + b > 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                                colorTemp[i] = WHITE;
                            }
                        }
                    }

                }
            }
            System.out.println(transparent);
        }
        returnBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returnBMP;
    }

    public static Bitmap getSinglePic4Pre(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returnBMP = Bitmap.createBitmap(inputBMP.getWidth(),
                inputBMP.getHeight(), Bitmap.Config.ARGB_8888);
        // 127;//曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
        int lightNumber;

        //计算中间值
        int allLightNumber = 0;
        int hasColor = 0;
        for (int i = 0; i < colorTemp.length; i++) {
            if (pix[i] != 0) {
                int r = Color.red(pix[i]);
                int g = Color.green(pix[i]);
                int b = Color.blue(pix[i]);
                allLightNumber += (r + g + b) / 3;
                hasColor++;
            }
        }

        // 中间值等于总的除以有的色
        if (hasColor > 0) {
            lightNumber = allLightNumber / hasColor;

            for (int i = 0; i < colorTemp.length; i++) {
                if (pix[i] != 0) {
                    int r = Color.red(pix[i]);
                    int g = Color.green(pix[i]);
                    int b = Color.blue(pix[i]);
                    if (r + g + b + 1 > 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                        // 大设为白
                        colorTemp[i] = WHITE;
                    }
                }
            }
        }
        returnBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returnBMP;
    }

    /**
     * 反色
     *
     * @param inputBMP in
     * @return r
     */
    public static Bitmap reverseColor(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returBMP = Bitmap.createBitmap(inputBMP.getWidth(),
                inputBMP.getHeight(), Bitmap.Config.ARGB_8888);
        int lightNumber;//曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
        //计算中间值
        int allLightNumber = 0;
        int hasColor = 0;
        for (int i = 0; i < colorTemp.length; i++) {
            if (pix[i] != 0) {
                int r = Color.red(pix[i]);
                int g = Color.green(pix[i]);
                int b = Color.blue(pix[i]);
                allLightNumber += (r + g + b) / 3;
                hasColor++;
            }
        }

        // 中间值等于总的除以有的色
        if (hasColor > 0) {
            lightNumber = allLightNumber / hasColor;

            for (int i = 0; i < colorTemp.length; i++) {
                int r = Color.red(pix[i]);
                int g = Color.green(pix[i]);
                int b = Color.blue(pix[i]);
                if (r + g + b + 1 <= 3 * lightNumber) {//三种颜色相加大于3倍的曝光值，才是黑色，否则白色
                    // 大设为白
                    colorTemp[i] = WHITE;
                }
            }
        }
        returBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returBMP;
    }

    private static boolean isWhite(int color, int lightNumber) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return color != 0 && r + g + b > 3 * lightNumber;//三种颜色相加大于3倍的曝光值，才是白色，否则黑色
    }

    private static boolean isBlackAndNotTransparent(int color, int lightNumber) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return color != 0 && r + g + b <= 3 * lightNumber;//三种颜色相加大于3倍的曝光值，才是白色，否则黑色
    }


    /**
     * 自动去除单色位图的透明边框
     */
    public static Bitmap removeBorder(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        // 要去除的内边距
        //曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高

        int width = inputBMP.getWidth();
//        int height = inputBMP.getHeight();

        //计算边距
        // 四边 四个初始值
        int a, b, c, d;
        //  a从 0, 0 出发向右移动 画横线 每轮结束向下移动一行 x + 1
        a = 1;
        //  b从 width , 0 出发 向下移动 画竖线 每轮结束向左移动一行 y - 1
        b = inputBMP.getWidth();
        //  c从 width , height 出发 向左移动 画横线 每轮结束向下移动一行 x - 1
        c = inputBMP.getHeight();
        //  d从 0 , height 出发 向上移动 画竖线 每轮结束向左移动一行 y + 1
        d = 1;
        loop:
        try {
            while (a < c) {
                // a 是上边框 整条线起点是d 终点是b 判断整个x轴是否全是透明
                for (int i = d; i < b; i++) {
                    // 计算坐标 a是y轴,i为x 向右画横线
                    if (pix[(a - 1) * width + i - 1] != 0) {
                        break loop;
                    }
                }
                // b 是右边框 划线起点是a 终点是c 判断整个y轴是否全是透明
                for (int i = a; i < c; i++) {
                    // 计算坐标 b是x轴内循环不变,i为y 向下画竖线
                    if (pix[(i - 1) * width + b - 1] != 0) {
                        break loop;
                    }
                }
                // c 是下边框 线的起点是b 终点是d 判断整个x轴是否全是透明
                for (int i = b; i > d; i--) {
                    // 计算坐标 c是y轴内循环不变,i为x 向左画横线
                    if (pix[(c - 1) * width + i - 1] != 0) {
                        break loop;
                    }
                }
                // d 是左 线的起点是c 终点是a 判断整个y轴是否全是透明
                for (int i = c; i > a; i--) {
                    // 计算坐标 c是y轴内循环不变,i为x 向上画竖线
                    if (pix[(i - 1) * width + d - 1] != 0) {
                        break loop;
                    }
                }
                a++;
                b--;
                c--;
                d++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(--a);

        // 根据a的值 重新裁剪图标
        Bitmap bitmap = Bitmap.createBitmap(inputBMP, d, a, b - d, c - a);
        System.out.println(bitmap);
        return bitmap;
    }


    /**
     * 自动去除单色位图的透明边框 忽略长宽比
     */
    public static Bitmap removeBorderIgnorance(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());

        int width = inputBMP.getWidth();

        //计算边距
        // 四边 四个初始值
        int a, b, c, d;
        //  a从 0, 0 出发向右移动 画横线 每轮结束向下移动一行 x + 1
        a = 1;
        //  b从 width , 0 出发 向下移动 画竖线 每轮结束向左移动一行 y - 1
        b = inputBMP.getWidth();
        //  c从 width , height 出发 向左移动 画横线 每轮结束向下移动一行 x - 1
        c = inputBMP.getHeight();
        //  d从 0 , height 出发 向上移动 画竖线 每轮结束向左移动一行 y + 1
        d = 1;
        try {

            int up = 0, down = 0, left = 0, right = 0;

            while (up + down + left + right != -4) {
                // a 是上边框 整条线起点是d 终点是b 判断整个x轴是否全是透明
                if (up != -1) {
                    for (int i = d; i < b; i++) {
                        // 计算坐标 a是y轴,i为x 向右画横线
                        if (pix[(a - 1) * width + i - 1] != 0) {
                            up = -1;
                            break;
                        }
                    }
                    a++;
                }
                // b 是右边框 划线起点是a 终点是c 判断整个y轴是否全是透明
                if (right != -1) {
                    for (int i = a; i < c; i++) {
                        // 计算坐标 b是x轴内循环不变,i为y 向下画竖线
                        if (pix[(i - 1) * width + b - 1] != 0) {
                            right = -1;
                            break;
                        }
                    }
                    b--;
                }

                // c 是下边框 线的起点是b 终点是d 判断整个x轴是否全是透明
                if (down != -1) {
                    for (int i = b; i > d; i--) {
                        // 计算坐标 c是y轴内循环不变,i为x 向左画横线
                        if (pix[(c - 1) * width + i - 1] != 0) {
                            down = -1;
                            break;
                        }
                    }
                    c--;
                }
                // d 是左 线的起点是c 终点是a 判断整个y轴是否全是透明
                if (left != -1) {
                    for (int i = c; i > a; i--) {
                        // 计算坐标 c是y轴内循环不变,i为x 向上画竖线
                        if (pix[(i - 1) * width + d - 1] != 0) {
                            left = -1;
                            break;
                        }
                    }
                    d++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(--a);
        System.out.println(++b);
        System.out.println(++c);
        System.out.println(--d);

        // 根据a的值 重新裁剪图标
        Bitmap bitmap = Bitmap.createBitmap(inputBMP, --d, --a, b - d, c - a);
        System.out.println(bitmap);
        return bitmap;
    }

    public static Bitmap toBitmap(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }


    public static boolean isGrayscaleIcon(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        for (int i = 0; i < colorTemp.length; i++) {
            if (pix[i] != 0) {
                int r = Color.red(pix[i]);
                int g = Color.green(pix[i]);
                int b = Color.blue(pix[i]);
                if (r + g + b != 255 * 3) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            Drawable vectorDrawable = AppCompatResources.getDrawable(context, vectorDrawableId);
            assert vectorDrawable != null;
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    public static Bitmap createBitmapByView(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);
        v.draw(c);
        return bmp;
    }

    @SuppressWarnings("unused")
    public static Bitmap createBitmap3(View v, int width, int height) {
        //测量使得view指定大小
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        //调用layout方法布局后，可以得到view的尺寸大小
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }


    @SuppressWarnings("unused")
    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fos;
        try {
            File root = Environment.getExternalStorageDirectory();
            File file = new File(root, "test.png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setSmallIcon(Icon icon, Notification notification) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess") @SuppressLint("DiscouragedPrivateApi")
            Field mSmallIcon = Notification.class.getDeclaredField("mSmallIcon");
            mSmallIcon.setAccessible(true);
            mSmallIcon.set(notification, icon);
        } catch (Exception e) {
            System.out.println("反射赋值失败");
            e.printStackTrace();
        }
    }

    /**
     * 去色同时加圆角
     *
     * @param bmpOriginal 原图
     * @param pixels      圆角弧度
     * @return 修改后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal, int pixels) {
        return toRoundCorner(getSinglePic(bmpOriginal), pixels);
    }

    /**
     * 把图片变成圆角
     *
     * @param bitmap 需要修改的图片
     * @param pixels 圆角的弧度
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float) pixels, (float) pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    /**
     * 使圆角功能支持BitampDrawable
     *
     * @param bitmapDrawable 1
     * @param pixels         1
     * @return 1
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable, int pixels) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmapDrawable = new BitmapDrawable(toRoundCorner(bitmap, pixels));
        return bitmapDrawable;
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64) {
        byte[] decode = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    public static Bitmap base64ToBitmapSplit(String base64) {
        byte[] decode = Base64.decode(base64.split(",")[1], Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }


    //保存图片到相册 主动设置uri
    public static void saveImage(Context context, Bitmap bitmap, String name) {

        Callable<Objects> callable = () -> {
            try {
                Uri saveUri = createImagePathUri(context, name);
                OutputStream outputStream = context.getContentResolver().openOutputStream(saveUri);

                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {//设置压缩比
                    //保存成功
                    Intent intent = new Intent();
                    intent.setData(saveUri);
                    context.sendBroadcast(intent);
                    // 最后通知图库更新
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, saveUri));
                    MediaScannerConnection.scanFile(context,
                            new String[]{"/storage/emulated/0/"}, null,
                            (path, uri) -> Toast.makeText(context, context.getString(R.string.save_success), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.save_fail), Toast.LENGTH_SHORT).show();

            }
            return null;
        };
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            PermissionsUtil.reqPermission((Activity) context, Manifest.permission.READ_MEDIA_IMAGES, callable);
        } else {
            PermissionsUtil.reqPermission((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE, callable);
        }
    }

    //设置保存文件的文件名等属性
    public static Uri createImagePathUri(final Context context, String imageName) {
        final Uri[] imageFilePath = {null};

        String status = Environment.getExternalStorageState();
//            SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
//            String imageName = timeFormatter.format(new Date(time));
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            imageFilePath[0] = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            imageFilePath[0] = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }

        return imageFilePath[0];
    }
}