package com.hypersocket.client.gui.jfx;
import javafx.scene.input.MouseEvent;


/**
 * Helper for tracking the velocity of touch events, for implementing
 * flinging and other such gestures.  Use {@link #obtain} to retrieve a
 * new instance of the class when you are going to begin tracking, put
 * the motion events you receive into it with {@link #addMovement(MotionEvent)},
 * and when you want to determine the velocity call
 * {@link #computeCurrentVelocity(int)} and then {@link #getXVelocity()}
 * and {@link #getXVelocity()}.
 */
public final class VelocityTracker {
    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG;

    private static final int NUM_PAST = 10;
    private static final int MAX_AGE_MILLISECONDS = 200;
    
    private static final int POINTER_POOL_CAPACITY = 20;

    private static Pointer sRecycledPointerListHead;
    private static int sRecycledPointerCount;
    
    private static final class Pointer {
        public Pointer next;
        
        public int id;
        public float xVelocity;
        public float yVelocity;
        
        public final float[] pastX = new float[NUM_PAST];
        public final float[] pastY = new float[NUM_PAST];
        public final long[] pastTime = new long[NUM_PAST]; // uses Long.MIN_VALUE as a sentinel
        
    }
    
    private Pointer mPointerListHead; // sorted by id in increasing order
    private int mLastTouchIndex;

    public VelocityTracker() {
        clear();
    }
    
    /**
     * Reset the velocity tracker back to its initial state.
     */
    public void clear() {
        releasePointerList(mPointerListHead);
        
        mPointerListHead = null;
        mLastTouchIndex = 0;
    }
    
    /**
     * Add a user's movement to the tracker.  You should call this for the
     * initial {@link MotionEvent#ACTION_DOWN}, the following
     * {@link MotionEvent#ACTION_MOVE} events that you receive, and the
     * final {@link MotionEvent#ACTION_UP}.  You can, however, call this
     * for whichever events you desire.
     * 
     * @param ev The MotionEvent you received and would like to track.
     */
    public void addMovement(MouseEvent ev) {
        final int historySize = 0;
        final int lastTouchIndex = mLastTouchIndex;
        final int nextTouchIndex = (lastTouchIndex + 1) % NUM_PAST;
        final int finalTouchIndex = (nextTouchIndex + historySize) % NUM_PAST;
        
        mLastTouchIndex = finalTouchIndex;

        if (mPointerListHead == null) {
        	mPointerListHead = obtainPointer();
        }
        
         
        final float[] pastX = mPointerListHead.pastX;
        final float[] pastY = mPointerListHead.pastY;
        final long[] pastTime = mPointerListHead.pastTime;
        
        
        pastX[finalTouchIndex] = (float)ev.getX();
        pastY[finalTouchIndex] = (float)ev.getY();
        pastTime[finalTouchIndex] = System.currentTimeMillis();
        
    }

    /**
     * Equivalent to invoking {@link #computeCurrentVelocity(int, float)} with a maximum
     * velocity of Float.MAX_VALUE.
     * 
     * @see #computeCurrentVelocity(int, float) 
     */
    public void computeCurrentVelocity(int units) {
        computeCurrentVelocity(units, Float.MAX_VALUE);
    }

    /**
     * Compute the current velocity based on the points that have been
     * collected.  Only call this when you actually want to retrieve velocity
     * information, as it is relatively expensive.  You can then retrieve
     * the velocity with {@link #getXVelocity()} and
     * {@link #getYVelocity()}.
     * 
     * @param units The units you would like the velocity in.  A value of 1
     * provides pixels per millisecond, 1000 provides pixels per second, etc.
     * @param maxVelocity The maximum velocity that can be computed by this method.
     * This value must be declared in the same unit as the units parameter. This value
     * must be positive.
     */
    public void computeCurrentVelocity(int units, float maxVelocity) {
        final int lastTouchIndex = mLastTouchIndex;
        
        for (Pointer pointer = mPointerListHead; pointer != null; pointer = pointer.next) {
            final long[] pastTime = pointer.pastTime;
            
            // Search backwards in time for oldest acceptable time.
            // Stop at the beginning of the trace as indicated by the sentinel time Long.MIN_VALUE.
            int oldestTouchIndex = lastTouchIndex;
            int numTouches = 1;
            final long minTime = pastTime[lastTouchIndex] - MAX_AGE_MILLISECONDS;
            while (numTouches < NUM_PAST) {
                final int nextOldestTouchIndex = (oldestTouchIndex + NUM_PAST - 1) % NUM_PAST;
                final long nextOldestTime = pastTime[nextOldestTouchIndex];
                if (nextOldestTime < minTime) { // also handles end of trace sentinel
                    break;
                }
                oldestTouchIndex = nextOldestTouchIndex;
                numTouches += 1;
            }
            
            // If we have a lot of samples, skip the last received sample since it is
            // probably pretty noisy compared to the sum of all of the traces already acquired.
            if (numTouches > 3) {
                numTouches -= 1;
            }
            
            // Kind-of stupid.
            final float[] pastX = pointer.pastX;
            final float[] pastY = pointer.pastY;
            
            final float oldestX = pastX[oldestTouchIndex];
            final float oldestY = pastY[oldestTouchIndex];
            final long oldestTime = pastTime[oldestTouchIndex];
            
            float accumX = 0;
            float accumY = 0;
            
            for (int i = 1; i < numTouches; i++) {
                final int touchIndex = (oldestTouchIndex + i) % NUM_PAST;
                final int duration = (int)(pastTime[touchIndex] - oldestTime);
                
                if (duration == 0) continue;
                
                float delta = pastX[touchIndex] - oldestX;
                float velocity = (delta / duration) * units; // pixels/frame.
                accumX = (accumX == 0) ? velocity : (accumX + velocity) * .5f;
            
                delta = pastY[touchIndex] - oldestY;
                velocity = (delta / duration) * units; // pixels/frame.
                accumY = (accumY == 0) ? velocity : (accumY + velocity) * .5f;
            }
            
            if (accumX < -maxVelocity) {
                accumX = - maxVelocity;
            } else if (accumX > maxVelocity) {
                accumX = maxVelocity;
            }
            
            if (accumY < -maxVelocity) {
                accumY = - maxVelocity;
            } else if (accumY > maxVelocity) {
                accumY = maxVelocity;
            }
            
            pointer.xVelocity = accumX;
            pointer.yVelocity = accumY;
            
            if (localLOGV) {
                System.out.println("Pointer " + pointer.id + ": Y velocity=" + accumX +" X velocity=" + accumY + " N=" + numTouches);
            }
        }
    }
    
    /**
     * Retrieve the last computed X velocity.  You must first call
     * {@link #computeCurrentVelocity(int)} before calling this function.
     * 
     * @return The previously computed X velocity.
     */
    public float getXVelocity() {
        Pointer pointer = getPointer(0);
        return pointer != null ? pointer.xVelocity : 0;
    }
    
    /**
     * Retrieve the last computed Y velocity.  You must first call
     * {@link #computeCurrentVelocity(int)} before calling this function.
     * 
     * @return The previously computed Y velocity.
     */
    public float getYVelocity() {
        Pointer pointer = getPointer(0);
        return pointer != null ? pointer.yVelocity : 0;
    }
    
    /**
     * Retrieve the last computed X velocity.  You must first call
     * {@link #computeCurrentVelocity(int)} before calling this function.
     * 
     * @param id Which pointer's velocity to return.
     * @return The previously computed X velocity.
     */
    public float getXVelocity(int id) {
        Pointer pointer = getPointer(id);
        return pointer != null ? pointer.xVelocity : 0;
    }
    
    /**
     * Retrieve the last computed Y velocity.  You must first call
     * {@link #computeCurrentVelocity(int)} before calling this function.
     * 
     * @param id Which pointer's velocity to return.
     * @return The previously computed Y velocity.
     */
    public float getYVelocity(int id) {
        Pointer pointer = getPointer(id);
        return pointer != null ? pointer.yVelocity : 0;
    }
    
    private final Pointer getPointer(int id) {
        for (Pointer pointer = mPointerListHead; pointer != null; pointer = pointer.next) {
            if (pointer.id == id) {
                return pointer;
            }
        }
        return null;
    }
    
    private static final Pointer obtainPointer() {
        if (sRecycledPointerCount != 0) {
            Pointer element = sRecycledPointerListHead;
            sRecycledPointerCount -= 1;
            sRecycledPointerListHead = element.next;
            element.next = null;
            return element;
        }
        return new Pointer();
    }
    
    private static final void releasePointer(Pointer pointer) {
        if (sRecycledPointerCount < POINTER_POOL_CAPACITY) {
            pointer.next = sRecycledPointerListHead;
            sRecycledPointerCount += 1;
            sRecycledPointerListHead = pointer;
        }
    }
    
    private static final void releasePointerList(Pointer pointer) {
        if (pointer != null) {
                int count = sRecycledPointerCount;
                if (count >= POINTER_POOL_CAPACITY) {
                    return;
                }
                
                Pointer tail = pointer;
                for (;;) {
                    count += 1;
                    if (count >= POINTER_POOL_CAPACITY) {
                        break;
                    }
                    
                    Pointer next = tail.next;
                    if (next == null) {
                        break;
                    }
                    tail = next;
                }

                tail.next = sRecycledPointerListHead;
                sRecycledPointerCount = count;
                sRecycledPointerListHead = pointer;
        }
    }
}