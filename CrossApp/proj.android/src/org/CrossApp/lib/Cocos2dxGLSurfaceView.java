
package org.CrossApp.lib;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class Cocos2dxGLSurfaceView extends GLSurfaceView {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String TAG = Cocos2dxGLSurfaceView.class.getSimpleName();

	private static Cocos2dxGLSurfaceView mGLSurfaceView;

	private Cocos2dxRenderer mRenderer;
	// ===========================================================
	// Constructors
	// ===========================================================

	public Cocos2dxGLSurfaceView(final Context context)
	{
		super(context);

		this.initView();
	}

	public Cocos2dxGLSurfaceView(final Context context, final AttributeSet attrs) 
	{
		super(context, attrs);
		
		this.initView();
	}

	protected void initView() 
	{
		this.setEGLContextClientVersion(2);
		this.setFocusableInTouchMode(true);
		Cocos2dxGLSurfaceView.mGLSurfaceView = this;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================


	public static Cocos2dxGLSurfaceView getInstance()
	{
		return mGLSurfaceView;
	}

	public static void queueAccelerometer(final float x, final float y, final float z, final long timestamp) 
	{
		mGLSurfaceView.queueEvent(new Runnable()
		{
			@Override
			public void run() 
			{
				Cocos2dxAccelerometer.onSensorChanged(x, y, z, timestamp);
			}
		});
	}

	public void setCocos2dxRenderer(final Cocos2dxRenderer renderer)
	{
		this.mRenderer = renderer;
		this.setRenderer(this.mRenderer);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void onResume() 
	{
		super.onResume();
		
		this.setRenderMode(RENDERMODE_CONTINUOUSLY);
		
		this.queueEvent(new Runnable() 
		{
			@Override
			public void run()
			{
				Cocos2dxGLSurfaceView.this.mRenderer.handleOnResume();
			}
		});
	}

	@Override
	public void onPause() 
	{
		this.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				Cocos2dxGLSurfaceView.this.mRenderer.handleOnPause();
			}
		});
		
		this.setRenderMode(RENDERMODE_WHEN_DIRTY);
		
		//super.onPause();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent pMotionEvent)
	{
		// these data are used in ACTION_MOVE and ACTION_CANCEL
		final int pointerNumber = pMotionEvent.getPointerCount();
		final int[] ids = new int[pointerNumber];
		final float[] xs = new float[pointerNumber];
		final float[] ys = new float[pointerNumber];

		for (int i = 0; i < pointerNumber; i++)
		{
			ids[i] = pMotionEvent.getPointerId(i);
			xs[i] = pMotionEvent.getX(i);
			ys[i] = pMotionEvent.getY(i);
		}
		switch (pMotionEvent.getAction() & MotionEvent.ACTION_MASK) 
		{
			case MotionEvent.ACTION_POINTER_DOWN:
				final int indexPointerDown = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				final int idPointerDown = pMotionEvent.getPointerId(indexPointerDown);
				final float xPointerDown = pMotionEvent.getX(indexPointerDown);
				final float yPointerDown = pMotionEvent.getY(indexPointerDown);

				this.queueEvent(new Runnable() 
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionDown(idPointerDown, xPointerDown, yPointerDown);
						
					}
				});
				break;

			case MotionEvent.ACTION_DOWN:
				// there are only one finger on the screen
				final int idDown = pMotionEvent.getPointerId(0);
				final float xDown = xs[0];
				final float yDown = ys[0];

				this.queueEvent(new Runnable() 
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionDown(idDown, xDown, yDown);
					}
				});
				break;

			case MotionEvent.ACTION_MOVE:
				this.queueEvent(new Runnable() 
				{
					@Override
					public void run()
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionMove(ids, xs, ys);
					}
				});
				break;

			case MotionEvent.ACTION_POINTER_UP:
				final int indexPointUp = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				final int idPointerUp = pMotionEvent.getPointerId(indexPointUp);
				final float xPointerUp = pMotionEvent.getX(indexPointUp);
				final float yPointerUp = pMotionEvent.getY(indexPointUp);

				this.queueEvent(new Runnable() 
				{
					@Override
					public void run()
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionUp(idPointerUp, xPointerUp, yPointerUp);
					}
				});
				break;

			case MotionEvent.ACTION_UP:
				// there are only one finger on the screen
				final int idUp = pMotionEvent.getPointerId(0);
				final float xUp = xs[0];
				final float yUp = ys[0];

				this.queueEvent(new Runnable()
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionUp(idUp, xUp, yUp);
					}
				});
				break;

			case MotionEvent.ACTION_CANCEL:
				this.queueEvent(new Runnable()
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleActionCancel(ids, xs, ys);
					}
				});
				break;
		}

        /*
		if (BuildConfig.DEBUG) {
			Cocos2dxGLSurfaceView.dumpMotionEvent(pMotionEvent);
		}
		*/
		return true;
	}

	/*
	 * This function is called before Cocos2dxRenderer.nativeInit(), so the
	 * width and height is correct.
	 */
	@Override
	protected void onSizeChanged(final int pNewSurfaceWidth, final int pNewSurfaceHeight, final int pOldSurfaceWidth, final int pOldSurfaceHeight) 
	{
        if(!this.isInEditMode())
        {
            Log.e("SUN", "SurfaceView onSizeChanged ..."+pNewSurfaceWidth+"."+pNewSurfaceHeight+" old "+pOldSurfaceWidth+"."+pOldSurfaceHeight);
            
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = pNewSurfaceWidth;
            lp.height = pNewSurfaceHeight;
            setLayoutParams(lp);
            this.mRenderer.setScreenWidthAndHeight(pNewSurfaceWidth, pNewSurfaceHeight);
            this.mRenderer.handleOnResume();
        }
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pKeyEvent)
	{
		switch (pKeyCode) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_MENU:
				this.queueEvent(new Runnable()
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleKeyDown(pKeyCode);
					}
				});
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				this.queueEvent(new Runnable()
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleKeyDown(pKeyCode);
					}
				});
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				this.queueEvent(new Runnable()
				{
					@Override
					public void run() 
					{
						Cocos2dxGLSurfaceView.this.mRenderer.handleKeyDown(pKeyCode);
					}
				});
				return true;
			default:
				return super.onKeyDown(pKeyCode, pKeyEvent);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static void dumpMotionEvent(final MotionEvent event)
	{
		final String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		final StringBuilder sb = new StringBuilder();
		final int action = event.getAction();
		final int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
		{
			sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		
		for (int i = 0; i < event.getPointerCount(); i++) 
		{
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
			{
				sb.append(";");
			}
		}
		
		sb.append("]");
		Log.d(Cocos2dxGLSurfaceView.TAG, sb.toString());
	}
}
