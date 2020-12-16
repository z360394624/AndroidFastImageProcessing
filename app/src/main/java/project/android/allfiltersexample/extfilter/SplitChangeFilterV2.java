package project.android.allfiltersexample.extfilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import project.android.imageprocessing.filter.BasicFilter;
import project.android.imageprocessing.helper.ImageHelper;

public class SplitChangeFilterV2 extends BasicFilter {

    private static final String SplitChangeFilterShader =
            "precision mediump float;\n"
            + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
            + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 1 + ";\n"
            + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 2 + ";\n"
            + "uniform float splitPoint;\n"
            + "varying vec2 " + VARYING_TEXCOORD + ";\n"
            + "void main(){\n"
            + "  vec4 texColour = texture2D(" + UNIFORM_TEXTURE0 + "," + VARYING_TEXCOORD + ");\n"
            + "  float blueColor = texColour.b * 63.0;\n"
            + "  vec2 quad1;\n"
            + "  quad1.y = floor(floor(blueColor) / 8.0);\n"
            + "  quad1.x = floor(blueColor) - (quad1.y * 8.0);\n"
            + "  vec2 quad2;\n"
            + "  quad2.y = floor(ceil(blueColor) / 8.0);\n"
            + "  quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n"
            + "  vec2 texPos1;\n"
            + "  texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.r);\n"
            + "  texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.g);\n"
            + "  vec2 texPos2;\n"
            + "  texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.r);\n"
            + "  texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * texColour.g);\n"
            + "  if (v_TexCoord.x <= splitPoint) {\n"
            + "    vec4 newColor1 = texture2D(" + UNIFORM_TEXTUREBASE + 1 + ", texPos1);\n"
            + "    vec4 newColor2 = texture2D(" + UNIFORM_TEXTUREBASE + 1 + ", texPos2);\n"
            + "    vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n"
            + "    gl_FragColor = vec4(newColor.rgb, texColour.a);\n"
            + "  } else {\n"
            + "    vec4 newColor1 = texture2D(" + UNIFORM_TEXTUREBASE + 2 + ", texPos1);\n"
            + "    vec4 newColor2 = texture2D(" + UNIFORM_TEXTUREBASE + 2 + ", texPos2);\n"
            + "    vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n"
            + "    gl_FragColor = vec4(newColor.rgb, texColour.a);\n"
            + "  }\n"
            + "}\n";

    private int lookup_textureA;
    private int lookup_textureB;
    private Bitmap lookupBitmapA;
    private Bitmap lookupBitmapB;

    private int splitPointHandle;
    private int sourceHandle;
    private int inputAHandle;
    private int inputBHandle;
    private float splitPoint;


    /**
     * Creates a MultiInputFilter with any number of initial filters or filter graphs that produce a
     * set number of textures which can be used by this filter.
     */
    public SplitChangeFilterV2() {
    }


    @Override
    protected String getFragmentShader() {
        return SplitChangeFilterShader;
//        return SplitChangeFilterShaderA;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        splitPointHandle = GLES20.glGetUniformLocation(this.programHandle, "splitPoint");
        inputAHandle = GLES20.glGetUniformLocation(this.programHandle, "u_Texture1");
        inputBHandle = GLES20.glGetUniformLocation(this.programHandle, "u_Texture2");

        if (lookup_textureA == 0) {
            lookup_textureA = ImageHelper.bitmapToTexture(lookupBitmapA);
        }

        if (lookup_textureB == 0) {
            lookup_textureB = ImageHelper.bitmapToTexture(lookupBitmapB);
        }
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
        GLES20.glUniform1f(this.splitPointHandle, this.splitPoint);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lookup_textureA);
        GLES20.glUniform1i(inputAHandle, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lookup_textureB);
        GLES20.glUniform1i(inputBHandle, 2);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (lookup_textureA != 0) {
            int[] tex = new int[1];
            tex[0] = lookup_textureA;
            GLES20.glDeleteTextures(1, tex, 0);
            lookup_textureA = 0;
        }
        if (lookupBitmapA != null) {
            lookupBitmapA = null;
        }
        if (lookup_textureB != 0) {
            int[] tex = new int[1];
            tex[0] = lookup_textureB;
            GLES20.glDeleteTextures(1, tex, 0);
            lookup_textureB = 0;
        }
        if (lookupBitmapB != null) {
            lookupBitmapB = null;
        }
    }

    public void changeFilter(String lookupPathA, String lookupPathB) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        lookupBitmapA = BitmapFactory.decodeFile(lookupPathA, options);
        lookupBitmapB = BitmapFactory.decodeFile(lookupPathB, options);
    }

    public void changeFilter(Bitmap lookupPathA, Bitmap lookupPathB) {
        lookupBitmapA = lookupPathA;
        lookupBitmapB = lookupPathB;
    }

    public void setSplitPoint(float splitPoint) {
        Log.e(TAG, "switchFilter: " + splitPoint);
        synchronized (this.getLockObject()) {
            this.splitPoint = splitPoint;
        }
    }
}
