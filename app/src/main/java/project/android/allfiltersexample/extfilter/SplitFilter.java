package project.android.allfiltersexample.extfilter;

import android.opengl.GLES20;

import project.android.imageprocessing.filter.MultiInputFilter;

public class SplitFilter extends MultiInputFilter {

    private int splitPointHandle;
    private float splitPoint;

    private static final String LOOKUP_FILTER_FRAG_SHADER =
            "precision mediump float;\n"
                    + "varying highp vec2 v_TexCoord;\n"
                    + "uniform sampler2D u_Texture0;\n"
                    + "uniform sampler2D u_Texture1;\n"
                    + "uniform float splitPoint;\n"
                    + "void main() {\n"
                    + "    if (v_TexCoord.x <= 0.5) {\n"
                    + "        gl_FragColor = texture2D(u_Texture0, v_TexCoord);\n"
                    + "    } else {\n"
                    + "        gl_FragColor = texture2D(u_Texture1, v_TexCoord);\n"
                    + "    }\n"
                    + " }";


    public SplitFilter() {
        super(2);
    }

    @Override
    protected String getFragmentShader() {
        return LOOKUP_FILTER_FRAG_SHADER;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        this.splitPointHandle = GLES20.glGetUniformLocation(this.programHandle, "splitPoint");
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES20.glUniform1f(this.splitPointHandle, this.splitPoint);
    }

    public void setSplitPoint(float splitPoint) {
        synchronized(this.getLockObject()) {
            this.splitPoint = splitPoint;
        }
    }
}
