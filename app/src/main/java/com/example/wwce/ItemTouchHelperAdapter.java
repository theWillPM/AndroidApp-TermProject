package com.example.wwce;

/**
 * This was suggested by the IDE to be used on the touch events
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
