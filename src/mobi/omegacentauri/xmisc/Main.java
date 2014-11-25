package mobi.omegacentauri.xmisc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Main extends Activity {
	Resources res;
	SharedPreferences prefs;
	final static String PREF_DISABLE_HOLO = "disableHolo";
	final static String PREF_FIX_MONOPOLY = "fixMonopoly";
	static final String PREFS = "preferences";
	
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

        CheckBox mono = (CheckBox)findViewById(R.id.fix_monopoly);
        
        mono.setChecked(prefs.getBoolean(PREF_FIX_MONOPOLY, false));
        mono.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_FIX_MONOPOLY, isChecked).commit();
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

