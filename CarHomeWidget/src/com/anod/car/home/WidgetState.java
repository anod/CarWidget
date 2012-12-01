package com.anod.car.home;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class WidgetState {
    /**
     * Lock used when maintaining queue of requested updates.
     */
    public static Object sLock = new Object();
    /**
     * Flag if there is an update thread already running. We only launch a new
     * thread if one isn't already running.
     */
    public static boolean sThreadRunning;
    
    /**
     * Internal queue of requested widget updates. You <b>must</b> access
     * through {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make
     * sure your access is correctly synchronized.
     */
    private static Queue<Integer> sAppWidgetIds = new LinkedList<Integer>();
    
    /**
     * Request updates for the given widgets. Will only queue them up, you are
     * still responsible for starting a processing thread if needed, usually by
     * starting the parent service.
     */
    public static void requestUpdate(int[] appWidgetIds) {
        synchronized (sLock) {
            for (int appWidgetId : appWidgetIds) {
                sAppWidgetIds.add(appWidgetId);
            }
        }
    }
    
    /**
     * Peek if we have more updates to perform. This method is special because
     * it assumes you're calling from the update thread, and that you will
     * terminate if no updates remain. (It atomically resets
     * {@link #sThreadRunning} when none remain to prevent race conditions.)
     */
    public static boolean hasMoreUpdates() {
        return !sAppWidgetIds.isEmpty();
    }
    
    /**
    * Collect all currently requested appWidgetId filters, returning as single
    * list. This call also clears the internal list.
    * <p>
    * Only call this while holding a {@link #sLock} lock.
    */
    public static int[] collectAppWidgetIdsLocked() {
    	final int size = sAppWidgetIds.size();
    	int[] array = new int[size];
    	Iterator<Integer> iterator = sAppWidgetIds.iterator();
    	for (int i = 0; i < size; i++) {
    		array[i] = iterator.next();
    	}
    	sAppWidgetIds.clear();
    	return array;
    }
   /**
   * Call this at any point to release any {@link WakeLock} and reset to
   * default state. Usually called before {@link Service#stopSelf()}.
   * <p>
   * Only call this while holding a {@link #sLock} lock.
   */
   public static void clearLocked() {
	   sThreadRunning = false;
	   sAppWidgetIds.clear();
   }
}
