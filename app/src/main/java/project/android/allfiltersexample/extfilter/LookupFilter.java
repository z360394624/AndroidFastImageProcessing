package project.android.allfiltersexample.extfilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import project.android.imageprocessing.filter.MultiInputFilter;
import project.android.imageprocessing.helper.ImageHelper;
import project.android.imageprocessing.input.GLTextureOutputRenderer;

public class LookupFilter extends MultiInputFilter {

    private int lookup_texture;
    private Bitmap lookupBitmap;
    private String id;
    private String name;


    public LookupFilter(String path) {
        super(2);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        lookupBitmap = BitmapFactory.decodeFile(path, options);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (lookup_texture != 0) {
            int[] tex = new int[1];
            tex[0] = lookup_texture;
            GLES20.glDeleteTextures(1, tex, 0);
            lookup_texture = 0;
        }
    }

    @Override
    protected String getFragmentShader() {
        return
                "precision mediump float;\n"
                        + "uniform sampler2D " + UNIFORM_TEXTURE0 + ";\n"
                        + "uniform sampler2D " + UNIFORM_TEXTUREBASE + 1 + ";\n"
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
                        + "  vec4 newColor1 = texture2D(" + UNIFORM_TEXTUREBASE + 1 + ", texPos1);\n"
                        + "  vec4 newColor2 = texture2D(" + UNIFORM_TEXTUREBASE + 1 + ", texPos2);\n"
                        + "  vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n"
                        + "  gl_FragColor = vec4(newColor.rgb, texColour.a);\n"
                        + "}\n";
    }

    @Override
    public void newTextureReady(int texture, GLTextureOutputRenderer source, boolean newData) {
        if(filterLocations.size() < 2 || !source.equals(filterLocations.get(0))) {
            clearRegisteredFilterLocations();
            registerFilterLocation(source, 0);
            registerFilterLocation(this, 1);
        }
        if(lookup_texture == 0) {
            lookup_texture = ImageHelper.bitmapToTexture(lookupBitmap);
        }
        super.newTextureReady(lookup_texture, this, newData);
        super.newTextureReady(texture, source, newData);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
