package minus.android.opengl;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class Circle extends GLShape {

	final float centerX = -0.5f;
	final float centerY = -0.5f;
	final float r = 0.5f;
	final int sectionCount = 360;

    public float[] getCounterClockWiseVertex() {
    	float[] v = new float[3 * (sectionCount + 1)];
    	v[0] = centerX;
    	v[1] = centerY;
    	v[2] = 0;
		double incr = 2 * Math.PI / sectionCount;
		for (int i = 1; i <= sectionCount; ++i) {
			v[3 * i] = (float) (centerX + r * Math.sin(i * incr));
			v[3 * i + 1] = (float) (centerY + r * Math.cos(i * incr));
			v[3 * i + 2] = 0;
		}
		return v;
	}

    @Override
    protected int getDrawMode() {
    	return GLES20.GL_TRIANGLES;
    }

    @Override
	protected short[] getDrawOrder() {
		short[] order = new short[3 * sectionCount];
		order[0] = 0;
		order[1] = sectionCount;
		order[2] = 1;
		for(int i = 1; i < sectionCount; ++i) {
			order[3 * i] = 0;
			order[3 * i + 1] = (short) i;
			order[3 * i + 2] = (short) (i + 1);
		}
		return order;
	}

	@Override
	protected int getFillColorRGBA() {
		return Color.parseColor("#0000ff00");
	}

}

class Square extends GLShape {

		static float squareCoords[] = {
	            -0.5f,  0.5f, 0f,   // top left
	            -0.5f, -0.5f, 0.0f,   // bottom left
	            0.5f, -0.5f, 0.0f,   // bottom right
	            0.5f,  0.5f, 0.0f }; // top right

	    public float[] getCounterClockWiseVertex() {
			return squareCoords;
		}

	    @Override
		protected short[] getDrawOrder() {
			return new short[] { 0, 1, 2, 0, 2, 3 };
		}

		@Override
		protected int getFillColorRGBA() {
			return Color.parseColor("#ffffffff");
		}

		@Override
		protected int getDrawMode() {
			return GLES20.GL_TRIANGLES;
		}

	}

	class Triangle extends GLShape {

		static float triangleCoords[] = {   // in counterclockwise order:
	             0.0f,  0.622008459f, 0.0f, // top
	            -0.5f, -0.311004243f, 0.0f, // bottom left
	             0.5f, -0.311004243f, 0.0f  // bottom right
	    };

		public float[] getCounterClockWiseVertex() {
			return triangleCoords;
		}

		@Override
		protected int getFillColorRGBA() {
			return Color.parseColor("#00ff0000");
		}

		@Override
		protected short[] getDrawOrder() {
			return new short[] {0, 1, 2};
		}

		@Override
		protected int getDrawMode() {
			return GLES20.GL_LINE_LOOP;
		}

	}

	public abstract class GLShape {

        public enum Rotate {

            NONE(0), FLIP_DEGREE_90(90), FLIP_DEGREE_180(180), FLIP_DEGREE_270(270);

            private final int degree;

            Rotate(int degree) {
                this.degree = degree;
            }

            public int value() {
                return degree;
            }

        }

        public static int loadShader(int type, String shaderCode){

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES20.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }

        public static int setupShaderProgram(String vs, String fs) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vs);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fs);


            // create empty OpenGL ES Program
            int mProgram = GLES20.glCreateProgram();

            // add the vertex shader to program
            GLES20.glAttachShader(mProgram, vertexShader);

            // add the fragment shader to program
            GLES20.glAttachShader(mProgram, fragmentShader);

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(mProgram);

            return mProgram;
        }

        static float[] toColorFloatArray(int colorInt) {
            float r =  (0xff & (colorInt >> 24)) / (float)0xff;
            float g =  (0xff & (colorInt >> 16)) / (float)0xff;
            float b =  (0xff & (colorInt >> 8)) / (float)0xff;
            float a =  (0xff & colorInt) / (float)0xff;
            return new float[] { r, g, b, a };
        }

        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

		private final String vertexShaderCode =
		        // This matrix member variable provides a hook to manipulate
		        // the coordinates of the objects that use this vertex shader
		        "uniform mat4 uMVPMatrix;" +
		        "attribute vec4 vPosition;" +
		        "void main() {" +
		        // the matrix must be included as a modifier of gl_Position
		        // Note that the uMVPMatrix factor *must be first* in order
		        // for the matrix multiplication product to be correct.
		        "  gl_Position = uMVPMatrix * vPosition;" +
		        "}";

	    private final String fragmentShaderCode =
	        "precision mediump float;" +
	        "uniform vec4 vColor;" +
	        "void main() {" +
	        "  gl_FragColor = vColor;" +
	        "}";

        private int mProgram;

        private int mPositionHandle;
        private int mColorHandle;
        private int mMVPMatrixHandle;

        private FloatBuffer vertexBuffer;

        final int drawCount;
        final ShortBuffer drawBuffer;

        final float vertexCoords[];
        final int vertexCount;

        // Set color with red, green, blue and alpha (opacity) values
        final float color[];

        public GLShape() {
            short[] drawOrderArray = getDrawOrder();
            vertexCoords = getCounterClockWiseVertex();
            vertexCount = vertexCoords.length / COORDS_PER_VERTEX;

            color = toColorFloatArray(getFillColorRGBA());

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer fbb = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    vertexCoords.length * Float.SIZE);
            // use the device hardware's native byte order
            fbb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = fbb.asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(vertexCoords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);

            drawCount = drawOrderArray.length;
            ByteBuffer sbb = ByteBuffer.allocateDirect(drawOrderArray.length * Short.SIZE);
            sbb.order(ByteOrder.nativeOrder());
            drawBuffer = sbb.asShortBuffer();
            drawBuffer.put(drawOrderArray);
            drawBuffer.position(0);
        }

        public GLShape draw(float[] mvpMatrix) {
            return draw(mvpMatrix, color, getDrawMode());
        }

        public GLShape draw(float[] m, float scaleX, float scaleY, Rotate flip, boolean mirror) {
            Matrix.scaleM(m, 0, scaleX, scaleY, 1);
            if(Rotate.NONE != flip) {
                Matrix.rotateM(m, 0, flip.value(), 0, 0, 1);
            }
            if(mirror) {
                Matrix.rotateM(m, 0, 180, 0, 1, 0); // TODO:
            }
            return draw(m, color, getDrawMode());
        }

        public GLShape draw(float[] mvpMatrix, float[] color, int drawMode) {
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            // Set color for drawing the triangle
            GLES20.glUniform4fv(mColorHandle, 1, color, 0);

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

            // Draw the triangle
            GLES20.glDrawElements(drawMode, drawCount, GLES20.GL_UNSIGNED_SHORT, drawBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);

            return this;
        }

        public GLShape setupShaderProgram() {
            mProgram = setupShaderProgram(getVertexShaderCode(), getFragmentShaderCode());
            return this;
        }

		protected String getVertexShaderCode() {
			return vertexShaderCode;
		}

		protected String getFragmentShaderCode() {
			return fragmentShaderCode;
		}

		protected int getDrawMode() {
			return GLES20.GL_LINE_LOOP;
		}

	    protected abstract short[] getDrawOrder();
	    protected abstract float[] getCounterClockWiseVertex();
	    protected abstract int getFillColorRGBA();

	}
