package com.android.settings.nameless.eggs;

import android.renderscript.RSSurfaceView;
import android.renderscript.RenderScriptGL;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.MotionEvent;

public class EasterEgg1View extends RSSurfaceView {

    public EasterEgg1View(Context context) {
        super(context);
    }

    private RenderScriptGL mRS;
    private EasterEgg1RS mRender;

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        if (mRS == null) {
            RenderScriptGL.SurfaceConfig sc = new RenderScriptGL.SurfaceConfig();
            mRS = createRenderScriptGL(sc);
            mRS.setSurface(holder, w, h);

            mRender = new EasterEgg1RS();
            mRender.init(mRS, getResources(), w, h);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mRS != null) {
            mRS = null;
            destroyRenderScriptGL();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mRender.newTouchPosition(ev.getX(0), ev.getY(0), ev.getPressure(0), ev.getPointerId(0));
        return true;
    }
}
