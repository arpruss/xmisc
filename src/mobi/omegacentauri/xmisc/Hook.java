package mobi.omegacentauri.xmisc;

import java.io.FileNotFoundException;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookZygoteInit, IXposedHookLoadPackage /*, IXposedHookInitPackageResources */ {
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
	
	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);
		MODULE_PATH = startupParam.modulePath;
		prefs.makeWorldReadable();
		if (prefs.getBoolean(Main.PREF_DISABLE_HOLO, false))
			XResources.setSystemWideReplacement(
					"android", "drawable", "background_holo_dark", new XResources.DrawableLoader() {
						@Override
						public Drawable newDrawable(XResources res, int id) throws Throwable {
							return new ColorDrawable(Color.BLACK);
						}
					});
		
	}

//	@Override
//    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
//        if (!resparam.packageName.equals("com.amazon.kindle"))
//            return;
//
////        XposedBridge.log("kindle subst");
////        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
////        resparam.res.setReplacement("com.amazon.kindle", "array", "page_margins_user_settings", 
////        		modRes.fwd(R.array.page_margins_user_settings));
////        resparam.res.setReplacement("com.amazon.kindle", "array", "vertical_page_margins_user_settings", 
////        		modRes.fwd(R.array.vertical_page_margins_user_settings));
//    }
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);

		if (!Main.PUBLIC_VERSION && 
				lpparam.packageName.startsWith("com.eamobile.monopoly_full") &&
				prefs.getBoolean(Main.PREF_FIX_MONOPOLY, false))
			monoPatch(lpparam);
		
		if (lpparam.packageName.equals("com.amazon.kindle")) {
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_H_MARGINS, false))
				kindleHMarginsPatch(lpparam);
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_V_MARGINS, false))
				kindleVMarginsPatch(lpparam);
			if (prefs.getBoolean(Main.PREF_FIX_KINDLE_GREEN, false))
				kindleGreenPatch(lpparam);
		}
	}
	
	public void kindleGreenPatch(LoadPackageParam lpparam) {
		findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
				"getColor", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int r = (Integer)param.getResult();
				if (r == 0xffc5e7ce) {
					// green text
					param.setResult(0xff000000);
				}
				else if (r == 0xff3a4b43) {
					// green background
					param.setResult(0xff00ff00);
				}
				else if ((Integer)param.args[0] == 0x7f0a00c3) {
					// secondary green text
					param.setResult(0xff00aa00);
				}
			}
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
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
					int r = (Integer)param.getResult();
					XposedBridge.log("Correcting margin "+r+" to "+(r/8));
					param.setResult(r/8);
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
