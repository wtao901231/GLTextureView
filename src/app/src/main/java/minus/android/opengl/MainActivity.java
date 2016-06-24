package minus.android.opengl;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import minus.android.support.opengl.GLTextureView;

public class MainActivity extends Activity {

    private GLSurfaceView mGLSurfaceView;
    private GLTextureView mGLTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GLES20ContextFactory contextFactory = new GLES20ContextFactory();
        GLES20ConfigChooser chooser = new GLES20ConfigChooser(5, 6, 5, 0, 0, 0); // arg565

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextFactory(contextFactory);
        mGLSurfaceView.setEGLConfigChooser(chooser);
        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {

            private GLShape shape = new Triangle();
            private GlMVPMatrix mvpMatrix = new GlMVPMatrix();

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                shape.setupShaderProgram();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                mvpMatrix.adjustViewPort(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                shape.draw(mvpMatrix.getMVPMatrixFloatArray());
            }
        });

        mGLTextureView = (GLTextureView) findViewById(R.id.gl_texture_view);
        mGLTextureView.setEGLContextFactory(contextFactory);
        mGLTextureView.setEGLConfigChooser(chooser);
        mGLTextureView.setRenderer(new GLTextureView.Renderer() {

            private GLShape shape = new Square();
            private GlMVPMatrix mvpMatrix = new GlMVPMatrix();

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                shape.setupShaderProgram();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                mvpMatrix.adjustViewPort(width, height);
            }

            @Override
            public boolean onDrawFrame(GL10 gl) {
                shape.draw(mvpMatrix.getMVPMatrixFloatArray());
                return true;
            }

            @Override
            public void onSurfaceDestroyed() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mGLTextureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        mGLTextureView.onPause();
    }

}

class GlMVPMatrix {

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    public void adjustViewPort(int w, int h) {
        GLES20.glViewport(0, 0, w, h);
        float ratio = (float) w / h;
        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 4);
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 3,
                0f, 0, 0f,
                0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    public float[] getMVPMatrixFloatArray() {
        return mMVPMatrix.clone();
    }

}