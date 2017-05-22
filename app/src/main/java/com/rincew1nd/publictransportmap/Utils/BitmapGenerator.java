package com.rincew1nd.publictransportmap.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rincew1nd.publictransportmap.R;

public class BitmapGenerator {

    public static Bitmap GenerateBitmapIcon(Context context, String text, int color) {
        View customMarkerView =
                ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker_view, null);
        ImageView markerImage = (ImageView) customMarkerView.findViewById(R.id.marker_image);
        TextView markerText = (TextView) customMarkerView.findViewById(R.id.marker_text);

        ((GradientDrawable)markerImage.getBackground()).setColor(color);
        markerText.setText(text);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}
