package mobi.omegacentauri.xmisc;

import java.io.FileNotFoundException;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);

		if (prefs.getBoolean(Main.PREF_FIX_MONOPOLY, false) &&
				lpparam.packageName.startsWith("com.eamobile.monopoly_full"))
			monoPatch(lpparam);
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
