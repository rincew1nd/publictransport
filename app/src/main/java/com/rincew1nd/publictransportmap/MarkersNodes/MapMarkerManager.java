package com.rincew1nd.publictransportmap.MarkersNodes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rincew1nd.publictransportmap.Utils.JsonSerializer;
import com.rincew1nd.publictransportmap.Models.Node;
import com.rincew1nd.publictransportmap.Models.PublicTransportMap;
import com.rincew1nd.publictransportmap.R;

public class MapMarkerManager {

    private Context _context;
    // TODO Make private?
    public PublicTransportMap _markers;

    public MapMarkerManager(Context context)
    {
        _context = context;
    }

    // Load markers from JSON file and generate icons
    public void LoadMarkers()
    {
        JsonSerializer reader = new JsonSerializer(_context.getResources(), R.raw.metro);
        _markers = reader.constructUsingGson(PublicTransportMap.class);
        for (Node marker: _markers.Nodes)
            marker.icon = GenerateBitmapIcon(marker);
    }

    // Generate icons
    private Bitmap GenerateBitmapIcon(Node marker)
    {
        View customMarkerView =
            ((LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.custom_marker_view, null);
        ImageView markerImage = (ImageView) customMarkerView.findViewById(R.id.marker_image);
        TextView markerText = (TextView) customMarkerView.findViewById(R.id.marker_text);

        markerImage.setBackgroundResource(R.drawable.circle);
        markerText.setText(marker.Name);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        //Drawable drawable = customMarkerView.getBackground();
        //if (drawable != null)
        //    drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public Node GetNode(int id)
    {
        for (Node node: _markers.Nodes)
            if (node.Id == id)
                return node;
        return null;
    }
}
