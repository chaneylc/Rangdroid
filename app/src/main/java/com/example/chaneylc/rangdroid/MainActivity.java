package com.example.chaneylc.rangdroid;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private int mHeight;
    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Choose file to import."), 100);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();

        display.getSize(size);

        mWidth = size.x;
        mHeight = size.y;
    }

    @Override
    final protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {

            if (intent != null) {
                switch (requestCode) {
                    case 100:
                        //ImageView iv = (ImageView) findViewById(R.id.imageView);
                        //iv.setImageBitmap(BitmapFactory.decodeFile(getPath(intent.getData())));
                        //Canvas c = new Canvas(BitmapFactory.decodeFile(getPath(intent.getData())));
                        CanvasView cv = new CanvasView(this, getPath(intent.getData()), mWidth, mHeight);

                        setContentView(cv);
                        break;
                }
            }
        }
    }

    //based on https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
    public String getPath(Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (DocumentsContract.isDocumentUri(this, uri)) {

                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String[] doc =  DocumentsContract.getDocumentId(uri).split(":");
                    final String documentType = doc[0];

                    if ("primary".equalsIgnoreCase(documentType)) {
                        return Environment.getExternalStorageDirectory() + "/" + doc[1];
                    }
                }
                else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    if (!id.isEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                    }
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(this, contentUri, null, null);
                }
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            } else if ("com.estrongs.files".equals(uri.getAuthority())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static class CanvasView extends View {

        RectF rect;

        Paint mPaint;

        String bitmapPath;

        Canvas mCanvas;

        Bitmap mBitmap;

        ArrayList<PointF> mPoints;

        public CanvasView(Context context, String path, int width, int height) {

            super(context);

            bitmapPath = path;

            rect = new RectF(20, 20, 100,100);

            Bitmap b = BitmapFactory.decodeFile(bitmapPath);

            mBitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            mBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, false);

            mPoints = new ArrayList<>();

            mPaint = new Paint();

            mPaint.setTextSize(64f);
            //mCanvas = new Canvas(mutable);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(mCanvas);


            mPaint.setFilterBitmap(true);

            canvas.drawBitmap(mBitmap, null, new Rect(0, 0,
                    mBitmap.getWidth(), mBitmap.getHeight()), mPaint);

            canvas.drawText("1. Draw center point", 100, 100, mPaint);
            canvas.drawText("2. Draw left-most point", 100, 200, mPaint);
            canvas.drawText("3. Draw right-most point", 100, 300, mPaint);

            if (mPoints.size() > 0) {
                for (PointF p : mPoints) {
                    canvas.drawCircle(p.x, p.y, 5, mPaint);
                }
            }

            if (mPoints.size() == 3) {
                PointF first = mPoints.get(0);
                PointF second = mPoints.get(1);
                PointF third = mPoints.get(2);
                canvas.drawLine(first.x, first.y, second.x, second.y, mPaint);
                canvas.drawLine(first.x, first.y, third.x, third.y, mPaint);

                double angle = Math.toDegrees(Math.atan2(third.x - first.x,third.y - first.y)-
                        Math.atan2(second.x- first.x,second.y- first.y));
                canvas.drawText(String.valueOf(angle), 100, 400, mPaint);
            }
            //canvas.drawOval(rect, mPaint);

        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mPoints.size() < 3) mPoints.add(new PointF(x, y));
                    else {
                        mPoints.clear();
                        mPoints.add(new PointF(x,y));
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //point.set(x, y);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //point = null;
                    //invalidate();
                    break;
            }
            return true;
        }
    }
}
