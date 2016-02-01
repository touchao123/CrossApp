
package org.CrossApp.lib;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint("UseSparseArrays") public class CrossAppTextView
{
	private EditText textView = null; 
	private static FrameLayout layout = null;
	private static Cocos2dxActivity context = null;
	private static Handler handler = null;
	private static HashMap<Integer, CrossAppTextView> dict = null;
	private int mykey = -1;
	private ByteBuffer imageData = null;
	private Bitmap bmp = null;
	private int keyboardheight = 0;
	private int keyboardheightTemp = 0;
	private int leftMargin = 5;
	private int rightMargin = 5;
	private int inputType = (InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
	private int fontSize = 20;
	private String placeHolder = "";
	private int placeHolderColor = Color.GRAY;
	private String textViewText = "";
	private int textViewTextColor = Color.BLACK;
	private int contentSizeW = 800;
	private int contentSizeH = 400;
	private boolean secureTextEntry = false;
	private boolean showClearButton = false;
	private int keyBoardReturnType = EditorInfo.IME_ACTION_NONE;
	private int gravity = (Gravity.LEFT | Gravity.TOP);
	private TextWatcher textWatcher = null;
	private OnEditorActionListener onEditorActionListener = null;
	private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = null;
	
	//浠ｇ�����璋����瑕�
	private boolean isSetText = false;
	private String  beforeTextString = "";
	private int selection = 0;
	
	//������寮瑰�洪�����
	private boolean isShowKey = false;
	private boolean isKeyAction = false;
	
 	protected void finalize()
    {
		layout.removeView(textView);
     }
	
	public static void initWithHandler()
	{
		if (handler == null)
    	{
    		handler = new Handler(Looper.myLooper());
    	}
		
		if (dict == null)
    	{
    		dict = new HashMap<Integer, CrossAppTextView>();
    	}
		
		if (context == null)
    	{
    		context =  (Cocos2dxActivity)Cocos2dxActivity.getContext();
    	}
		
		if (layout == null)
    	{
    		layout = Cocos2dxActivity.getFrameLayout();
    	}
	}
	
	public static void reload()
	{
		handler = new Handler(Looper.myLooper());
		context =  (Cocos2dxActivity)Cocos2dxActivity.getContext();
		layout = Cocos2dxActivity.getFrameLayout();
		
		Set<Integer> keys = (Set<Integer>) dict.keySet() ; 
		Iterator<Integer> iterator = keys.iterator() ; 
		while (iterator.hasNext())
		{
			Integer key = iterator.next();
			CrossAppTextView textField = dict.get(key);
			textField.initWithTextView(key);
		}
	}
	
	//keyBoard return call back
	private static native void keyBoardReturnCallBack(int key);
	private static native boolean textChange(int key,String before,String change,int arg0,int arg1);
	private static native void text(int key, byte[] text, int lenght);
    public void init(int key)
    {
    	
    	
    	mykey = key;
    	dict.put(key, this);

    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {
            	initWithTextView(mykey);
            }
        });
    }
    
    //keyboard height
    

    private static native void keyBoardHeightReturn(int key,int height);
    private static native void resignFirstResponder(int key);
    public int getKeyBoardHeight()
    {
    	onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() 
    	{

            @Override
            public void onGlobalLayout() 
            {
            	
                // TODO Auto-generated method stub
                Rect r = new Rect();
                layout.getWindowVisibleDisplayFrame(r);

                int screenHeight = layout.getRootView().getHeight();
                
                keyboardheightTemp = screenHeight- r.bottom;
                if (keyboardheightTemp!=keyboardheight) {
                	context.runOnUiThread(new Runnable() 
                	{
            			
            			@Override
            			public void run() 
            			{
            				// TODO Auto-generated method stub
            				if (keyboardheightTemp < 1 && isShowKey == true)
            				{
    							//hide
            					isShowKey = false;
            					context.runOnGLThread(new Runnable() 
                            	{
                                    @Override
                                    public void run()
                                    {
                                    	resignFirstResponder(mykey);
                                    }
                                });
            					
    						}
//            				if (keyboardheight<1) {
//    							//show
//            					Log.d("android", "show board");
//    						}
//            				Log.d("android", "call c++");
            				
            				//keyBoardReturn
            				if (isKeyAction)
            				{
            					context.runOnGLThread(new Runnable() 
                            	{
                                    @Override
                                    public void run()
                                    {
                                    	keyBoardHeightReturn(mykey, keyboardheightTemp);
                                    }
                                });
            					isKeyAction = false;
            				}
            			}
            		});
    			}
                keyboardheight = keyboardheightTemp;
            }
        };
    	layout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		return keyboardheight;

    }

    public void setFontSize(final int size) 
    {
		context.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				fontSize = size;
				textView.setTextSize(size);
			}
		});
	}

    public void setTextViewText(final String text) 
    {
    		context.runOnUiThread(new Runnable()
    		{
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				isSetText = true;
				textViewText = text;
				textView.setText(text);
				isSetText = false;
			}
		});
	}
    
    //textView color 
    public void setTextViewTextColor(final int color)
    {
		context.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				textViewTextColor = color;
				textView.setTextColor(color);
			}
		});
	}
    

    //textView Algin
    public void setTextViewAlgin(final int var)
    {
    	context.runOnUiThread(new Runnable() 
    	{
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				switch (var)
				{
				case 0:
					//center
					gravity = (Gravity.LEFT | Gravity.TOP);
					break;
				case 1:
					//left
					gravity = (Gravity.CENTER | Gravity.TOP);
					break;
				case 2:
					//right
					gravity = (Gravity.RIGHT | Gravity.TOP);
					break;
				default:
					break;
				}
				textView.setGravity(gravity);
			}
		});
	}
    
    public void setKeyBoardReturnType(final int type)
    {
		context.runOnUiThread(new Runnable() 
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String string = type + "";
				switch (type) {
				case 0:
					inputType = (InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
					keyBoardReturnType = EditorInfo.IME_ACTION_NONE;
					break;
				case 1:
					inputType = (InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
					keyBoardReturnType = EditorInfo.IME_ACTION_DONE;
					break;
				case 2:
					inputType = (InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
					keyBoardReturnType = EditorInfo.IME_ACTION_SEND;
					break;
				case 3:
					inputType = (InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
					keyBoardReturnType = EditorInfo.IME_ACTION_NEXT;
					break;
				default:
					break;
				}
				textView.setInputType(inputType);
				textView.setImeOptions(keyBoardReturnType);
			}
		});
    }
    
	public void setTextViewPoint(final int x, final int y)
    {
    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {
            	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)textView.getLayoutParams(); 
            	params.leftMargin = x; 
            	params.topMargin = y;
            	textView.setLayoutParams(params);
            }
        });
    }
    
    public void setTextViewSize(final int width, final int height)
    {
    	contentSizeW = width;
    	contentSizeH = height;
    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {
            	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)textView.getLayoutParams(); 
            	params.width = width;
            	params.height = height;
            	textView.setLayoutParams(params);
            }
        });
    }
    
    
    private static native void onByte(int key, byte[] buf, int wdith, int height);
    
    public void getImage()
    {
    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {
            	bmp = textView.getDrawingCache();
            	if (bmp != null)
            	{
            		imageData = ByteBuffer.allocate(bmp.getRowBytes() * bmp.getHeight());
            		bmp.copyPixelsToBuffer(imageData);
            		
            		context.runOnGLThread(new Runnable() 
                	{
                        @Override
                        public void run()
                        {
                        	onByte(mykey, imageData.array(), bmp.getWidth(), bmp.getHeight());
                        }
                    });
            	}
            }
        });
    }
    
    private static native void hideImageView(int key);
    
    public void becomeFirstResponder()
    {
    	Cocos2dxActivity.setSingleTextView(this);
    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {
            	isShowKey = true;
            	isKeyAction = true;
            	
            	//show
              	InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE); 
        		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        		textView.requestFocus();
        		Editable etext = textView.getText();
            	textView.setSelection(etext.length());
        		
        		context.runOnGLThread(new Runnable() 
            	{
                    @Override
                    public void run()
                    {
                    	hideImageView(mykey);
                    }
                });
            }
        });
    }
    
    public void resignFirstResponder()
    {
    	Cocos2dxActivity.setSingleTextView(null);
    	context.runOnUiThread(new Runnable() 
    	{
            @Override
            public void run()
            {        
            	isShowKey = false;
            	isKeyAction = true;
            	
            	InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);  
            	imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        		textView.clearFocus();
        		
        		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)textView.getLayoutParams(); 
            	params.leftMargin = -5000; 
            	params.topMargin = -5000;
            	textView.setLayoutParams(params);
        		
            	TimerTask task = new TimerTask()
            	{    
            		public void run()
            		{    
            			bmp = textView.getDrawingCache();
                    	if (bmp != null)
                    	{
                    		imageData = ByteBuffer.allocate(bmp.getRowBytes() * bmp.getHeight());
                    		bmp.copyPixelsToBuffer(imageData);
                    		
                    		context.runOnGLThread(new Runnable() 
                        	{
                                @Override
                                public void run()
                                {
                                	onByte(mykey, imageData.array(), bmp.getWidth(), bmp.getHeight());
                                }
                            });
                    	}
            		}    
            	};  
            		
            	Timer timer = new Timer();  
            	timer.schedule(task, (long) 50);
				
            }
        });
    }

    public void removeThis()
    {
    	textView.removeTextChangedListener(textWatcher);
    	layout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    	layout.removeView(textView);
    }

	static public CrossAppTextView getTextView(final int key)
	{
		CrossAppTextView var = dict.get(key);
		if (var != null) return var;
		return null;
		
	}
	
	static public void createTextView(final int key)
	{
		CrossAppTextView text = new CrossAppTextView();
		text.init(key);
	}
	
	static public void removeTextView(final int key) 
	{
		final CrossAppTextView var = dict.get(key);
		if (var != null)
		{
			context.runOnUiThread(new Runnable() 
	    	{
	            @Override
	            public void run()
	            {
	            	var.removeThis();
	            	dict.remove(key);
	            }
	        });
		}
	}

	public void initWithTextView(int key)
	{
		if (textView != null)
		{
			layout.removeView(textView);
		}
		
		textView = new EditText(context) ; 
		textView.setSingleLine(false);  
		textView.setGravity(gravity);
		textView.setBackgroundColor(0);
    	textView.setFocusable(true);
    	textView.setDrawingCacheEnabled(true);
    	textView.setTextSize(fontSize);
		textView.setInputType(inputType);
//		textView.setHint(placeHolder);
//		textView.setHintTextColor(placeHolderColor);
		textView.setText(textViewText);
		textView.setTextColor(textViewTextColor);
		textView.setImeOptions(keyBoardReturnType);
		
    	FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) ; 
    	params.leftMargin = -5000; 
    	params.topMargin = -5000;
    	params.width = contentSizeW;
    	params.height = contentSizeH;
    	layout.addView(textView, params) ;


    	textWatcher = new TextWatcher()
    	{
    		@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
				//起始位置， 删除长度，增加长度
				// TODO Auto-generated method stub
				if (isSetText)
				{
					return;
				}

				String string = arg0.toString();
				
				String  changedText = "";
				if (arg3 > 0) 
				{
					//只是添加
					changedText = string.substring(arg1, arg1 + arg3);
				}
				else 
				{
					//只是删除
					changedText = "";
				}

				if (!textChange(mykey, beforeTextString, changedText, arg1, arg2))
				{
					isSetText = true;
					textView.setText(beforeTextString);
					textView.setSelection(selection);
					isSetText = false;
				}
				else
				{
					isSetText = true;
					textView.setText(string);
					textView.setSelection(selection - arg2 + arg3);
//					context.runOnGLThread(new Runnable() 
//	            	{
//	                    @Override
//	                    public void run()
//	                    {
//	                    	ByteBuffer textBuffer = ByteBuffer.wrap(textView.getText().toString().getBytes());
//	    					text(mykey, textBuffer.array(), textBuffer.array().length);
//	                    }
//	                });
					ByteBuffer textBuffer = ByteBuffer.wrap(textView.getText().toString().getBytes());
					text(mykey, textBuffer.array(), textBuffer.array().length);
					isSetText = false;
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3)
			{
				if (isSetText)
				{
					return;
				}
				// TODO Auto-generated method stub
				beforeTextString = arg0.toString();
				selection = textView.getSelectionStart();
			}
			
			@Override
			public void afterTextChanged(Editable arg0)
			{
				// TODO Auto-generated method stub
			}
		};
		textView.addTextChangedListener(textWatcher);

		
		onEditorActionListener = new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (keyBoardReturnType != EditorInfo.IME_ACTION_NONE)
				{
					context.runOnGLThread(new Runnable() 
	            	{
	                    @Override
	                    public void run()
	                    {
	                    	keyBoardReturnCallBack(mykey);
	                    }
	                });
					
					return true;
				}
        		return false;
			}
		};    	
    	textView.setOnEditorActionListener(onEditorActionListener);
    	
		getKeyBoardHeight();
	}
	
	public void resume() 
	{
		TimerTask task = new TimerTask()
		{    
			public void run()
			{    
				resignFirstResponder(mykey);
			}    
		};  
		
		Timer timer = new Timer();  
		timer.schedule(task, (long) 100);
	}
}
