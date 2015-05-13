package mobi.omegacentauri.xmisc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorEventListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	static String MODULE_PATH;
	static final String[] replace = {
		"auction_highestBid_purple.png",
		"auctionMarker_purple.png",
		"bubble_purple.png",
		"checkbox_purple.png",
		"checkbox_purple_checked.png",
		"drag_purple.png",
		"propertymarker03.pvr",
		"tabletop_purple.png",
		"tabletop_purpleLg.png",
		"tokenhili.pvr"
	};
	static final int NOID = -1;
	static WindowManager wm = null;

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);
		MODULE_PATH = startupParam.modulePath;
		prefs.makeWorldReadable();

		XposedBridge.log("xmisc: hello");

		if (prefs.getBoolean(Main.PREF_DISABLE_HOLO, false))
			XResources.setSystemWideReplacement(
					"android", "drawable", "background_holo_dark", new XResources.DrawableLoader() {
						@Override
						public Drawable newDrawable(XResources res, int id) throws Throwable {
							return new ColorDrawable(Color.BLACK);
						}
					});

		if (prefs.getBoolean(Main.PREF_AUTO_FLIP, false))
			autoFlipGlobalPatch();

		if (prefs.getBoolean(Main.PREF_NO_WAKE, false))
			wirelessWakeupPatch();
	}
	
	private void wirelessWakeupPatch() {
		XposedBridge.log("xmisc: Wakeup patch");

		findAndHookMethod("com.android.server.power.PowerManagerService", null, 
				"shouldWakeUpWhenPluggedOrUnpluggedLocked", 
				boolean.class, int.class, boolean.class,
				new XC_MethodHook() {

			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				if (!(Boolean)param.getResult())
//					return;
//				Field f = param.thisObject.getClass().getDeclaredField("mPlugType");
//				f.setAccessible(true);
//				int plug = (Integer)f.get(param.thisObject);
				XposedBridge.log("xmisc: query "+(Boolean)param.args[0]+ " "+(Integer)param.args[1]+" "+(Boolean)param.args[2]);
				XposedBridge.log("xmisc: overrode wakeup");
				param.setResult(false);
			}

		});

//		findAndHookMethod("com.android.server.power.Notifier", null, 
//				"onWirelessChargingStarted", 
//				new XC_MethodHook() {
//
//			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				XposedBridge.log("xmisc: wirelesschargingstarted");
//				param.setResult(null);
//			}
//		});
//
//		findAndHookMethod("com.android.server.power.Notifier", null, 
//				"playWirelessChargingStartedSound", 
//				new XC_MethodHook() {
//
//			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				XposedBridge.log("xmisc: wirelesschargingsound");
//				param.setResult(null);
//			}
//		});
//
//		findAndHookMethod("com.android.server.power.PowerManagerService", null, 
//				"wakeUpNoUpdateLocked", 
//				long.class,
//				new XC_MethodHook() {
//
//			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				XposedBridge.log("xmisc: wakeUpNoUpdateLocked "+(Long)param.args[0]);
//			}
//
//		});
	
	}

	private void autoFlipGlobalPatch() {
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindow", null, "generateLayout",
				"com.android.internal.policy.impl.PhoneWindow.DecorView", new XC_MethodHook() {

			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Window window = (Window) param.thisObject;
				Context context = window.getContext();
				if (context instanceof Activity) {
					int oldOrientation = ((Activity)context).getRequestedOrientation();
					int newOrientation = autoFlipOrientation(oldOrientation);
					if (newOrientation != oldOrientation) {
						XposedBridge.log("Overriding orientation to "+newOrientation);
						((Activity)context).setRequestedOrientation(newOrientation);
					}
				}
			}

		});
	}

	private int autoFlipOrientation(int oldOrientation) {
		switch (oldOrientation) {
		case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
			if (Build.VERSION.SDK_INT >= 18)
				return ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
			else
				return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
		case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
			if (Build.VERSION.SDK_INT >= 18)
				return ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
			else
				return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
		case ActivityInfo.SCREEN_ORIENTATION_SENSOR:
		case ActivityInfo.SCREEN_ORIENTATION_USER:
			if (Build.VERSION.SDK_INT >= 18)
				return ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
			else
				return ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;						
		}
		return oldOrientation;
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);

		//		if (lpparam.packageName.endsWith(".gm"))
		//			orientationPatch(lpparam);

		if (prefs.getBoolean(Main.PREF_AUTO_FLIP, false))
			autoFlipAppPatch(lpparam);

		//if (lpparam.packageName.startsWith("com.flyersoft"))
		//			hookPaint(lpparam);

		if (!Main.PUBLIC_VERSION && 
				lpparam.packageName.startsWith("com.eamobile.monopoly_full") &&
				prefs.getBoolean(Main.PREF_FIX_MONOPOLY, false))
			monoPatch(lpparam);

		if (lpparam.packageName.equals("com.amazon.kindle")) {
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_H_MARGINS, false))
				kindleHMarginsPatch(lpparam);
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_V_MARGINS, false))
				kindleVMarginsPatch(lpparam);
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_COLORS, false))
				kindleGreenPatch(lpparam);
			if (prefs.getBoolean(Main.PREF_KINDLE_FAST_TURN, false))
				kindleFastTurn(lpparam);
			kindleFastTurn(lpparam);
		}		

//		if (lpparam.packageName.startsWith("com.nuance.swype") &&
//				prefs.getBoolean(Main.PREF_NO_SWYPE_EMOJI, false))
//			swypeNoEmoji(lpparam);

	}

	private void swypeNoEmoji(LoadPackageParam lpparam) {
		findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
				"getBoolean", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if ((Boolean)param.getResult() && 
						((Resources)param.thisObject).getResourceEntryName(
								(Integer)param.args[0]).equals("enable_emoji_in_english_ldb")) 
					param.setResult(false);
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
	}

	private void autoFlipAppPatch(LoadPackageParam lpparam) {
		findAndHookMethod(Activity.class, "setRequestedOrientation", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				int oldOrientation = (Integer)param.args[0];
				int newOrientation = autoFlipOrientation(oldOrientation);
				if (oldOrientation != newOrientation) {
					XposedBridge.log("Overriding orientation to "+newOrientation);
					param.args[0] = (Integer)newOrientation;
				}
			}

		});
	}

	//	public void hookPaint(LoadPackageParam lpparam) {
	//		XposedBridge.log("hookPaint()");
	//		try {
	//			XposedBridge.hookAllConstructors(
	//					Class.forName("android.graphics.Paint", false, lpparam.classLoader), 
	//					new XC_MethodHook() {
	//						@Override
	//						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	//					        ((Paint)param.thisObject).setColorFilter(colorFilters[COLOR_MODE_RED]);
	//						}
	//					}
	//					);
	//		} catch (ClassNotFoundException e) {
	//			XposedBridge.log("Paint constructor hook "+e);
	//		}
	//
	//		/*try {
	//			findAndHookMethod(
	//					Class.forName("android.graphics.Paint", false, lpparam.classLoader), 
	//					"setColorFilter",
	//					ColorFilter.class,
	//					new XC_MethodHook() {
	//						@Override
	//						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	//							param.args[0] = colorFilters[COLOR_MODE_RED];
	//						}
	//					}
	//					);
	//		} catch (ClassNotFoundException e) {
	//			XposedBridge.log("Paint filter hook "+e);
	//		}*/
	//	
	//		/* try {
	//			findAndHookMethod(
	//					Class.forName("android.graphics.Paint", false, lpparam.classLoader), 
	//					"getColorFilter",
	//					new XC_MethodHook() {
	//						@Override
	//						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	//							param.setResult(new ColorMatrixColorFilter(new ColorMatrix(
	//			                        colorMatrices[COLOR_MODE_RED_ON_BLACK])));
	//						}
	//					}
	//					);
	//		} catch (ClassNotFoundException e) {
	//			XposedBridge.log("Paint filter hook "+e);
	//		} */
	//	
	//	}

	public void kindleGreenPatch(LoadPackageParam lpparam) {
		findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
				"getColor", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int r = (Integer)param.getResult();
				//				XposedBridge.log("getcolor: "+String.format("%08x %08x", (Integer)param.args[0], 
				//						r));
				if (r == 0xffc5e7ce) {
					// green background
					param.setResult(0xff000000);
				}
				else if (r == 0xff3a4b43) {
					// green text
					param.setResult(0xff00ff00);
				}
				else if ((Integer)param.args[0] == 0x7f0a00c3) {
					// secondary green text
					param.setResult(0xff00aa00);
				}
				else if (r == 0xff5a4129) {
					// sepia text
					param.setResult(0xFF000000);
				}
				else if (r == 0xff937d63) {
					// sepia secondary
					param.setResult(0xff8e8e8e);
				}
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
	}

	public void kindleFastTurn(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.amazon.kcp.reader.ReaderNavigator", 
					lpparam.classLoader,
					"turnPage", 
					Class.forName("com.amazon.android.docviewer.KindleDocView$PagingDirection", false, lpparam.classLoader), 
					Class.forName("com.amazon.android.docviewer.KindleDocView$AnimationDirection", false, lpparam.classLoader), 
					float.class,
					new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				}

				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log("Requested velocity "+(Float)param.args[2]);
					if (Math.abs((Float)param.args[2]) < 1e-6f) {
						param.args[2] = (Float)(-1e20f);
						XposedBridge.log("xRequested velocity "+(Float)param.args[2]);
					}
				}
			});
		} catch (ClassNotFoundException e) {
			XposedBridge.log("Error "+e);
		}
	}

	public void kindleHMarginsPatch(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.amazon.android.docviewer.KindleDocLineSettings", 
					lpparam.classLoader,
					"getCalculatedHorizontalMargins", 
					Class.forName("com.amazon.android.docviewer.KindleDocLineSettings$Margin", false, lpparam.classLoader), 
					int.class,
					new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					//					int r = (Integer)param.getResult();
					//					XposedBridge.log("Correcting margin "+r+" to "+(r/8));
					param.setResult(0);
				}
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				}
			});
		} catch (ClassNotFoundException e) {
			XposedBridge.log("Error "+e);
		}
	}

	public void kindleVMarginsPatch(LoadPackageParam lpparam) {
		findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
				"getDimensionPixelSize", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (0x7f08012c == (Integer)param.args[0]) {
					//					XposedBridge.log("top_margin ps "+(Integer)param.getResult());
					param.setResult((Integer)4);
				}
				else if (0x7f08012d == (Integer)param.args[0]) {
					//					XposedBridge.log("bottom_margin ps "+(Integer)param.getResult());
					param.setResult((Integer)20);
				}
				else if (0x7f08012a == (Integer)param.args[0]) {
					// 			        XposedBridge.log("footer pos");
					param.setResult((Integer)8);
				}
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
	}

	public void monoPatch(LoadPackageParam lpparam) {
		findAndHookMethod("android.content.res.AssetManager", lpparam.classLoader,
				"open", String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				for (String s : replace) {
					if (((String)param.args[0]).endsWith("/"+s)) {
						String a = new String(s);
						if (a.endsWith(".pvr"))
							a += ".mp3";
						XposedBridge.log("loading "+a);
						param.setResult(XModuleResources.createInstance(MODULE_PATH, null).getAssets().open(a));
						return;
					}
				}
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				//				param.args[0] = monoAssetFix((String)param.args[0]);
			}
		});
		findAndHookMethod("android.content.res.AssetManager", lpparam.classLoader,
				"openFd", String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				for (String s : replace) {
					if (((String)param.args[0]).endsWith("/"+s)) {
						String a = new String(s);
						if (a.endsWith(".pvr"))
							a += ".mp3";
						XposedBridge.log("loading Fd "+a);
						param.setResult(XModuleResources.createInstance(MODULE_PATH, null).getAssets().openFd(a));
						return;
					}
				}
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				//				param.args[0] = monoAssetFix((String)param.args[0]);
			}
		});
	}

	//	static String monoAssetFix(String s) {
	//		return swap(swap(s, "_purple", "_red"),"propertymarker04","propertymarker03");
	//	}
	//	
	//	static String swap(String s, String a, String b) {
	//		if (s.contains(a)) {
	//			return s.replace(a,b);
	//		}
	//		else if (s.contains(b)) {
	//			return s.replace(b, a);
	//		}
	//		else
	//			return s;
	//	}
}
