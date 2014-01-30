package com.android.settings.nameless.eggs;

import android.content.res.Resources;
import android.renderscript.*;

import com.android.settings.R;

public class EasterEgg1RS {
    public static final int PART_COUNT = 50000; // Count of particles

    public EasterEgg1RS() {
    }

    private Resources mRes;
    private RenderScriptGL mRS;
    private ScriptC_easteregg1 mScript;

    public void init(RenderScriptGL rs, Resources res, int width, int height) {
        mRS = rs;
        mRes = res;

        ProgramFragmentFixedFunction.Builder pfb = new ProgramFragmentFixedFunction.Builder(rs);
        pfb.setVaryingColor(true);
        rs.bindProgramFragment(pfb.create());

        ScriptField_Point points = new ScriptField_Point(mRS, PART_COUNT);

        Mesh.AllocationBuilder smb = new Mesh.AllocationBuilder(mRS);
        smb.addVertexAllocation(points.getAllocation());
        smb.addIndexSetType(Mesh.Primitive.POINT);
        Mesh sm = smb.create();

        mScript = new ScriptC_easteregg1(mRS, mRes, R.raw.easteregg1);
        mScript.set_partMesh(sm);
        mScript.bind_point(points);
        mRS.bindRootScript(mScript);

        mScript.invoke_initParticles(); // Initialize Particles
    }

    public void newTouchPosition(float x, float y, float pressure, int id) {
        mScript.set_gTouchX(x);
        mScript.set_gTouchY(y);
    }
}

