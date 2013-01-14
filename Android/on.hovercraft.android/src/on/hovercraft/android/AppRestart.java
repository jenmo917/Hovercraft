package on.hovercraft.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
* \brief This activity shows nothing; instead, it restarts the android process.
*
* \author Jens Moser
*/
public class AppRestart extends Activity 
{
	/**
	* \brief onCreate
	*
	* App is destroyed
	*
	* \author Jens Moser
	*/
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        System.exit(0);
    }
	/**
	* \brief doRestart
	*
	* Call this function and the app is restarted.
	*
	* \author Jens Moser
	*/	
    public static void doRestart(Activity anyActivity) 
    {
        anyActivity.startActivity(new Intent(anyActivity.getApplicationContext(), AppRestart.class));
    }
}