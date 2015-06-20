package mobi.omegacentauri.xmisc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Main extends Activity {
	public static final boolean PUBLIC_VERSION = false;
	Resources res;
	SharedPreferences prefs;
	final static String PREF_DISABLE_HOLO = "disableHolo";
	final static String PREF_FIX_MONOPOLY = "fixMonopoly";
	static final String PREFS = "preferences";
	public static final String PREF_FIX_KINDLE_COLORS = "fixKindleGreen";
	public static final String PREF_FIX_KINDLE_H_MARGINS = "fixKindleHMargins";
	public static final String PREF_FIX_KINDLE_V_MARGINS = "fixKindleVMargins";
	public static final String PREF_KINDLE_FAST_TURN = "kindleFastTurn";
	public static final String PREF_LOG_BT = "logBT";
	public static final String PREF_AUTO_FLIP = "autoFlip";
	public static final String PREF_NO_SWYPE_EMOJI = "noSwypeEmoji";
	public static final String PREF_NO_WAKE = "noWake";
	public static final String PREF_FORCE_IMMERSIVE = "forceImmersive";
	public static final String PREF_RESTRICT_MARKETS = "noMarket";
	
	@SuppressLint("WorldReadableFiles")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(Main.PREFS, Context.MODE_WORLD_READABLE);
		
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        res = getResources();        

        CheckBox holo = (CheckBox)findViewById(R.id.holo_disable);
        
        holo.setChecked(prefs.getBoolean(PREF_DISABLE_HOLO, false));
        holo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_DISABLE_HOLO, isChecked).commit();
			}
		});

        if (PUBLIC_VERSION)
        	findViewById(R.id.fix_monopoly).setVisibility(View.GONE);
        else
        	setter(R.id.fix_monopoly, PREF_FIX_MONOPOLY, false);
        setter(R.id.fix_kindle_h_margins, PREF_FIX_KINDLE_H_MARGINS, false);
        setter(R.id.fix_kindle_v_margins, PREF_FIX_KINDLE_V_MARGINS, false);
        setter(R.id.fix_kindle_green, PREF_FIX_KINDLE_COLORS, false);
        setter(R.id.kindle_fast_turn, PREF_KINDLE_FAST_TURN, false);
        setter(R.id.auto_flip, PREF_AUTO_FLIP, false);
        setter(R.id.no_wake, PREF_NO_WAKE, false);
        setter(R.id.force_immersive, PREF_FORCE_IMMERSIVE, false);
        setter(R.id.restrict_markets, PREF_RESTRICT_MARKETS, false);
	}

	private void setter(int id, final String prefString, boolean defaultState) {
        CheckBox mono = (CheckBox)findViewById(id);
        
        mono.setChecked(prefs.getBoolean(prefString, defaultState));
        mono.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(prefString, isChecked).commit();
			}
		});
	}
	
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch(item.getItemId()) {
//    	case R.id.clear:
//    		clear();
//    		return true;
//    	case R.id.options:
//    		startActivity(new Intent(this, Options.class));
//    		return true;
//    	default:
//    		return false;
//    	}
//    }
//
//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//	    return true;
//	}

}

