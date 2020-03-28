package com.deluxerom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.grx.settings.R;

@SuppressLint("AppCompatCustomView")
public class Animation extends ImageView {

    private AnimationDrawable animationDrawable;
    private boolean mAttached;
    private static boolean v;

    public Animation(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(R.drawable.deluxe_animation );
    }

    private void updateAnim() {
        Drawable drawable = getDrawable();
        if (this.mAttached && this.animationDrawable != null) {
            this.animationDrawable.stop();
        }
        if (drawable instanceof AnimationDrawable) {
            this.animationDrawable = (AnimationDrawable) drawable;
            if (isShown()) {
                this.animationDrawable.start();
                return;
            }
            return;
        }
        this.animationDrawable = null;
    }

    private void updateAnimationState(Drawable drawable, boolean z) {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
            if (z) {
                animationDrawable.start();
            } else {
                animationDrawable.stop();
            }
        } else if (drawable instanceof Animatable){
            if (z) {
                ((Animatable) drawable).start();
            } else {
                ((Animatable) drawable).stop();
            }
        }
    }

    private void updateAnimationsState() {
        boolean z = getVisibility() == VISIBLE && hasWindowFocus();
        updateAnimationState(getDrawable(), z);
        updateAnimationState(getBackground(), z);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttached = true;
        updateAnim();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.animationDrawable != null) {
            this.animationDrawable.stop();
        }
        this.mAttached = false;
    }

    protected void onVisibilityChanged(@NonNull View view, int i) {
        super.onVisibilityChanged(view, i);
        updateAnimationsState();
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        updateAnimationsState();
    }

}
