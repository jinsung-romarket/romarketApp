package kr.co.romarket;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

class CycleListener implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToFoground(){
        // Moving to Foground…
        MainActivity.isBackGround = false;
        Log.d("CycleListener:onMoveToFoground", "Moving to Foground" );
        Log.d("CycleListener:onMoveToFoground", "isBackGround : " + MainActivity.isBackGround );

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // Moving to background…
        MainActivity.isBackGround = true;
        Log.d("CycleListener:onMoveToBackground", "Moving to background");
        Log.d("CycleListener:onMoveToFoground", "isBackGround : " + MainActivity.isBackGround );
    }

} // end LifecycleObserver