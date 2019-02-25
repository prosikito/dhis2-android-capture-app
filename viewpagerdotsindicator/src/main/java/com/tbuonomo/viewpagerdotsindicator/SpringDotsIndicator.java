package com.tbuonomo.viewpagerdotsindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.viewpager.widget.ViewPager;

import static android.widget.LinearLayout.HORIZONTAL;
import static com.tbuonomo.viewpagerdotsindicator.UiUtils.getThemePrimaryColor;

public class SpringDotsIndicator extends FrameLayoutIndicator {
    public static final float DEFAULT_DAMPING_RATIO = 0.5f;
    public static final int DEFAULT_STIFFNESS = 300;

    // Attributes
    private int dotsStrokeSize;
    private int dotsSpacing;
    private float stiffness;
    private float dampingRatio;
    private int dotIndicatorSize;
    private int dotIndicatorAdditionalSize;
    private int horizontalMargin;
    private SpringAnimation dotIndicatorSpring;

    public SpringDotsIndicator(Context context) {
        this(context, null);
    }

    public SpringDotsIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        strokeDots = new ArrayList<>();
        strokeDotsLinearLayout = new LinearLayout(context);
        LayoutParams linearParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalMargin = dpToPx(24);
        linearParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        strokeDotsLinearLayout.setLayoutParams(linearParams);
        strokeDotsLinearLayout.setOrientation(HORIZONTAL);
        addView(strokeDotsLinearLayout);

        dotsStrokeSize = dpToPx(16); // 16dp
        dotsSpacing = dpToPx(4); // 4dp
        dotsStrokeWidth = dpToPx(2); // 2dp
        dotIndicatorAdditionalSize = dpToPx(1); // 1dp additional to fill the stroke dots
        dotsCornerRadius = dotsStrokeSize / 2; // 1dp additional to fill the stroke dots
        dotIndicatorColor = getThemePrimaryColor(context);
        dotsStrokeColor = dotIndicatorColor;
        stiffness = DEFAULT_STIFFNESS;
        dampingRatio = DEFAULT_DAMPING_RATIO;
        dotsClickable = true;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpringDotsIndicator);

            // Dots attributes
            dotIndicatorColor = a.getColor(R.styleable.SpringDotsIndicator_dotsColor, dotIndicatorColor);
            dotsStrokeColor = a.getColor(R.styleable.SpringDotsIndicator_dotsStrokeColor, dotIndicatorColor);
            dotsStrokeSize = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsSize, dotsStrokeSize);
            dotsSpacing = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsSpacing, dotsSpacing);
            dotsCornerRadius = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsCornerRadius, dotsStrokeSize / 2f);
            stiffness = a.getFloat(R.styleable.SpringDotsIndicator_stiffness, stiffness);
            dampingRatio = a.getFloat(R.styleable.SpringDotsIndicator_dampingRatio, dampingRatio);

            // Spring dots attributes
            dotsStrokeWidth = (int) a.getDimension(R.styleable.SpringDotsIndicator_dotsStrokeWidth, dotsStrokeWidth);

            a.recycle();
        }

        dotIndicatorSize = dotsStrokeSize - dotsStrokeWidth * 2 + dotIndicatorAdditionalSize;

        if (isInEditMode()) {
            addStrokeDots(5);
            addView(buildDot(false));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshDots();
    }

    public void refreshDots() {
        if (dotIndicatorView == null) {
            setUpDotIndicator();
        }

        if (viewPager != null && viewPager.getAdapter() != null) {
            // Check if we need to refresh the strokeDots count
            if (strokeDots.size() < viewPager.getAdapter().getCount()) {
                addStrokeDots(viewPager.getAdapter().getCount() - strokeDots.size());
            } else if (strokeDots.size() > viewPager.getAdapter().getCount()) {
                removeDots(strokeDots.size() - viewPager.getAdapter().getCount());
            }
            setUpDotsAnimators();
        } else {
            Log.e(SpringDotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    private void setUpDotIndicator() {
        dotIndicatorView = buildDot(false);
        addView(dotIndicatorView);
        dotIndicatorSpring = new SpringAnimation(dotIndicatorView, SpringAnimation.TRANSLATION_X);
        SpringForce springForce = new SpringForce(0);
        springForce.setDampingRatio(dampingRatio);
        springForce.setStiffness(stiffness);
        dotIndicatorSpring.setSpring(springForce);
    }

    public ViewGroup buildDot(boolean stroke) {
        ViewGroup dot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.spring_dot_layout, this, false);
        ImageView dotView = dot.findViewById(R.id.spring_dot);
        dotView.setBackground(
                ContextCompat.getDrawable(getContext(), stroke ? R.drawable.spring_dot_stroke_background : R.drawable.spring_dot_background));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dotView.getLayoutParams();
        params.width = params.height = stroke ? dotsStrokeSize : dotIndicatorSize;
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        params.setMargins(dotsSpacing, 0, dotsSpacing, 0);

        setUpDotBackground(stroke, dotView);
        return dot;
    }

    public void setUpOnPageChangedListener() {
        pageChangedListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float globalPositionOffsetPixels = position * (dotsStrokeSize + dotsSpacing * 2) + (dotsStrokeSize + dotsSpacing * 2) * positionOffset;
                float indicatorTranslationX = globalPositionOffsetPixels + horizontalMargin + dotsStrokeWidth - dotIndicatorAdditionalSize / 2f;
                dotIndicatorSpring.getSpring().setFinalPosition(indicatorTranslationX);

                if (!dotIndicatorSpring.isRunning()) {
                    dotIndicatorSpring.start();
                }
            }

            @Override
            public void onPageSelected(int position) {
                // do nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        };
    }
}
