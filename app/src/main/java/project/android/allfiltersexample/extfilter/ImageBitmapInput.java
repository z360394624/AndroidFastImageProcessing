package project.android.allfiltersexample.extfilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import project.android.imageprocessing.helper.ImageHelper;
import project.android.imageprocessing.input.GLTextureOutputRenderer;

public class ImageBitmapInput extends GLTextureOutputRenderer {

    private Bitmap bitmap;
    private int imageWidth;
    private int imageHeight;
    private boolean newBitmap;

    public ImageBitmapInput(Bitmap bitmap) {
        this.setImage(bitmap);
    }

    public ImageBitmapInput() {
    }

    public ImageBitmapInput(String pathName) {
        this.setImage(pathName);
    }

    protected void drawFrame() {
        if (this.newBitmap) {
            this.loadTexture();
        }

        super.drawFrame();
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    private void loadImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
        setRenderSize(imageWidth, imageHeight);
        newBitmap = true;
        textureVertices = new FloatBuffer[4];

        float[] texData0 = new float[] {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
        textureVertices[0].put(texData0).position(0);

        float[] texData1 = new float[] {
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
        };
        textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
        textureVertices[1].put(texData1).position(0);

        float[] texData2 = new float[] {
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
        };
        textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
        textureVertices[2].put(texData2).position(0);

        float[] texData3 = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
        textureVertices[3].put(texData3).position(0);
    }

    public void destroy() {
        super.destroy();
        if (this.texture_in != 0) {
            int[] var1 = new int[]{this.texture_in};
            GLES20.glDeleteTextures(1, var1, 0);
        }

        if (this.bitmap != null && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
            this.bitmap = null;
        }

        this.newBitmap = true;
    }

    private void loadTexture() {
        if (!this.bitmap.isRecycled()) {
            if (this.texture_in != 0) {
                int[] var1 = new int[]{this.texture_in};
                GLES20.glDeleteTextures(1, var1, 0);
            }

            this.texture_in = ImageHelper.bitmapToTexture(this.bitmap);
        }

        this.newBitmap = false;
        this.markAsDirty();
    }

    public void releaseFrameBuffer() {
//        super.releaseFrameBuffer();
        super.destroy();
        if (this.texture_in != 0) {
            int[] var1 = new int[]{this.texture_in};
            GLES20.glDeleteTextures(1, var1, 0);
        }

    }

    public int texture_in() {
        return texture_in;
    }

    public void setImage(Bitmap bitmap) {
        this.loadImage(bitmap);
    }

    public void setImage(String filePath) {
        BitmapFactory.Options var2 = new BitmapFactory.Options();
        var2.inScaled = false;
        this.loadImage(BitmapFactory.decodeFile(filePath, var2));
    }
}
