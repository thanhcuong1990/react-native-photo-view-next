package com.reactnative.photoview;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;

import android.view.View;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.fresco.ReactNetworkImageRequest;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.OnScaleChangeListener;
import me.relex.photodraweeview.OnViewTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static com.facebook.react.views.image.ReactImageView.REMOTE_IMAGE_FADE_DURATION_MS;

public class PhotoView extends PhotoDraweeView {
    private Uri mUri;
    private ReadableMap mHeaders;
    private boolean mIsDirty;
    private boolean mIsLocalImage;
    private Drawable mLoadingImageDrawable;
    private PipelineDraweeControllerBuilder mDraweeControllerBuilder;
    private int mFadeDurationMs = -1;
    private ControllerListener mControllerListener;

    public PhotoView(Context context) {
        super(context);
    }

    public void setSource(@Nullable ReadableMap source,
                          @NonNull ResourceDrawableIdHelper resourceDrawableIdHelper) {
        mUri = null;
        if (source != null) {
            String uri = source.getString("uri");
            try {
                mUri = Uri.parse(uri);
                if (mUri.getScheme() == null) {
                    mUri = null;
                }
                if (source.hasKey("headers")) {
                    mHeaders = source.getMap("headers");
                }
            } catch (Exception e) {
                // ignore malformed uri
            }
            if (mUri == null) {
                mUri = resourceDrawableIdHelper.getResourceDrawableUri(getContext(), uri);
                mIsLocalImage = true;
            } else {
                mIsLocalImage = false;
            }
        }
        mIsDirty = true;
    }

    public void setLoadingIndicatorSource(@Nullable String name,
                                          ResourceDrawableIdHelper resourceDrawableIdHelper) {
        Drawable drawable = resourceDrawableIdHelper.getResourceDrawable(getContext(), name);
        mLoadingImageDrawable = drawable != null ? new AutoRotateDrawable(drawable, 1000) : null;
        mIsDirty = true;
    }

    public void setFadeDuration(int durationMs) {
        mFadeDurationMs = durationMs;
    }

    public void setShouldNotifyLoadEvents(boolean shouldNotify) {
        if (!shouldNotify) {
            mControllerListener = null;
        } else {
            final ReactContext reactContext = (ReactContext) getContext();
            final UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
            if (uiManager == null || uiManager.getEventDispatcher() == null) {
                return;
            }
            final EventDispatcher eventDispatcher = uiManager.getEventDispatcher();

            mControllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onSubmit(String id, Object callerContext) {
                    eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_LOAD_START));
                }

                @Override
                public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                    if (imageInfo != null) {
                        eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_LOAD));
                        eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_LOAD_END));
                        update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_ERROR));
                    eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_LOAD_END));
                }
            };
        }
        mIsDirty = true;
    }

    public void maybeUpdateView(@NonNull PipelineDraweeControllerBuilder builder) {
        if (!mIsDirty) return;

        GenericDraweeHierarchy hierarchy = getHierarchy();
        if (mLoadingImageDrawable != null) {
            hierarchy.setPlaceholderImage(mLoadingImageDrawable, ScalingUtils.ScaleType.CENTER);
        }

        hierarchy.setFadeDuration(
                mFadeDurationMs >= 0
                        ? mFadeDurationMs
                        : mIsLocalImage ? 0 : REMOTE_IMAGE_FADE_DURATION_MS);

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(mUri)
                .setAutoRotateEnabled(true)
                .setResizeOptions(new ResizeOptions(getMaxTextureSize(), getMaxTextureSize()));

        ImageRequest imageRequest = ReactNetworkImageRequest
                .fromBuilderWithHeaders(imageRequestBuilder, mHeaders);

        mDraweeControllerBuilder = builder;
        mDraweeControllerBuilder.setImageRequest(imageRequest);
        mDraweeControllerBuilder.setAutoPlayAnimations(true);
        mDraweeControllerBuilder.setOldController(getController());
        mDraweeControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) return;

                update(imageInfo.getWidth(), imageInfo.getHeight());

                if (animatable instanceof AnimatedDrawable2 animatedDrawable) {
                    AnimationBackend originalBackend = animatedDrawable.getAnimationBackend();
                    SlowAnimationBackend slowBackend = new SlowAnimationBackend(originalBackend);
                    animatedDrawable.setAnimationBackend(slowBackend);
                }
            }
        });

        if (mControllerListener != null) {
            mDraweeControllerBuilder.setControllerListener(mControllerListener);
        }

        setController(mDraweeControllerBuilder.build());
        setViewCallbacks();

        mIsDirty = false;
    }

    private void setViewCallbacks() {
        final ReactContext reactContext = (ReactContext) getContext();
        final UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        if (uiManager == null || uiManager.getEventDispatcher() == null) {
            return;
        }
        final EventDispatcher eventDispatcher = uiManager.getEventDispatcher();

        setOnPhotoTapListener((view, x, y) -> {
            WritableMap scaleChange = Arguments.createMap();
            scaleChange.putDouble("x", x);
            scaleChange.putDouble("y", y);
            scaleChange.putDouble("scale", PhotoView.this.getScale());
            eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_TAP).setExtras(scaleChange));
        });

        setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
            WritableMap scaleChange = Arguments.createMap();
            scaleChange.putDouble("scale", PhotoView.this.getScale());
            scaleChange.putDouble("scaleFactor", scaleFactor);
            scaleChange.putDouble("focusX", focusX);
            scaleChange.putDouble("focusY", focusY);
            eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_SCALE).setExtras(scaleChange));
        });

        setOnViewTapListener((view, x, y) -> {
            WritableMap scaleChange = Arguments.createMap();
            scaleChange.putDouble("x", x);
            scaleChange.putDouble("y", y);
            eventDispatcher.dispatchEvent(new ImageEvent(getId(), ImageEvent.ON_VIEW_TAP).setExtras(scaleChange));
        });
    }

    private int getMaxTextureSize() {
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display, version);

        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        for (int i = 0; i < totalConfigurations[0]; i++) {
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);
            if (maximumTextureSize < textureSize[0]) {
                maximumTextureSize = textureSize[0];
            }
        }

        egl.eglTerminate(display);
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }
}
