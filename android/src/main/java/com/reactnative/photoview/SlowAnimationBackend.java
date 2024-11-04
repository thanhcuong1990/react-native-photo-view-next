package com.reactnative.photoview;

import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.backend.AnimationBackendDelegate;

public class SlowAnimationBackend extends AnimationBackendDelegate<AnimationBackend> {
    public SlowAnimationBackend(AnimationBackend animationBackend) {
        super(animationBackend);
    }

    @Override
    public int getFrameDurationMs(int frameNumber) {
        return (int) (super.getFrameDurationMs(frameNumber) / 0.2);
    }
}
