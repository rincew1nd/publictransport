package com.rincew1nd.publictransportmap;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.rincew1nd.publictransportmap.GraphManager.GraphManager;
import com.rincew1nd.publictransportmap.Models.Settings;
import com.rincew1nd.publictransportmap.ShortPath.ShortPathManager;
import com.rincew1nd.publictransportmap.ShortPath.ShortestPathObj;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.rincew1nd.publictransportmap", appContext.getPackageName());
    }

    @Test
    public void mapLoadTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GraphManager gm = GraphManager.GetInstance();
        gm.SetContext(appContext);
        gm.LoadGraph();
        Settings.SearchDepth = 2;
        Settings.ToStationId = 10;
        Settings.FromStationId = 1;
        ShortPathManager sp = ShortPathManager.GetInstance();
        ArrayList<ShortestPathObj> spr = sp.FindShortestPaths();
        spr.get(1);
    }
}
