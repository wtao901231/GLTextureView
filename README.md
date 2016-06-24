# GLTextureView

## Another Easy-way to Use OpenGL

> GLSurfaceView is just one way to incorporate OpenGL ES graphics into your application. For a full-screen or near-full screen graphics view, it is a reasonable choice. Developers who want to incorporate OpenGL ES graphics in a small portion of their layouts should take a look at TextureView. For real, do-it-yourself developers, it is also possible to build up an OpenGL ES view using SurfaceView, but this requires writing quite a bit of additional code.
See more on https://developer.android.com/training/graphics/opengl/environment.html

## GLSurfaceView v.s. GLTextureView

### GLSurfaceView
- Two window. Host window manager the view hierarchy; And other surface window attarch to the host
- 'Punches' a hole in its host window to allow its surface to be displayed, make it <b>transparent region</b>
- GLSurfaceView.updateWindow() on onPreDraw(), different UI update time

### GLTextureView
- Only one window
- Behave the same as usual view, such as alpha and animation
- Only be used in a hardware accelerated window

<b>GLTextureView can avoid drawing transparent region. In this case, user will see blocked graph not drawn by the current visible window.</b>

# Javadoc
https://wtao901231.github.io/GLTextureView/javadoc/index.html
