package co.techbrewery.smartratingbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlin.math.roundToInt


class SmartRatingBar : LinearLayout {

    companion object {
        private const val LOG_TAG = "LOG_SmartRatingBar"
        private const val DEFAULT_MAX_RATING = 5
        private const val DEFAULT_TINT_COLOR_RES_ID = android.R.color.black
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private var parentHeight = 0
    private var parentWidth = 0
    private var tintColor: Int? = null
    private var rating: Float = 0f
    private var maxRating = DEFAULT_MAX_RATING
    private var emptyStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black_outline)
    private var halfSelectedStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black_half)
    private var selectedStateStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black)

    @SuppressLint("ResourceType")
    private fun init(context: Context, attrs: AttributeSet?) {

        attrs?.let {
            val androidAttributesSet = intArrayOf(
                android.R.attr.layout_height,
                android.R.attr.foregroundTint
            )

            val androidAttributesTypedArray =
                context.obtainStyledAttributes(attrs, androidAttributesSet)
            parentHeight = androidAttributesTypedArray.getDimension(0, 0f).toInt()
            tintColor = androidAttributesTypedArray.getColor(
                1,
                ContextCompat.getColor(context, DEFAULT_TINT_COLOR_RES_ID)
            )
            androidAttributesTypedArray.recycle()


            val srbAttributesTypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SmartRatingBar)
            maxRating = srbAttributesTypedArray.getInt(
                R.styleable.SmartRatingBar_maxRating,
                DEFAULT_MAX_RATING
            )
            val preciseRating =
                srbAttributesTypedArray.getFloat(R.styleable.SmartRatingBar_rating, 0f)
            rating = roundToHalf(preciseRating)

            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_emptyStateDrawable)) emptyStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_emptyStateDrawable)
            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_halfSelectedStateDrawable)) halfSelectedStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_halfSelectedStateDrawable)
            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_selectedStateDrawable)) selectedStateStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_selectedStateDrawable)

            srbAttributesTypedArray.recycle()
        }

        buildView()
        setRating(rating)

        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> resizeChildren() }

        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP) {
                calculateRatingFromX(event.x)
            }
            true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        parentWidth = w
    }

    private fun resizeChildren() {
        children.forEach { child ->
            val parentHeight = layoutParams.height
            child.layoutParams.height = parentHeight
            child.layoutParams.width = parentHeight
            child.requestLayout()
        }
    }

    private fun buildView() {
        (0 until maxRating).forEach { position ->
            val imageView = createImageView()
            imageView.setImageDrawable(emptyStateDrawable)
            addView(imageView)
        }
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(context)
        tintColor?.let { imageView.setColorFilter(it) }
        imageView.layoutParams = LayoutParams(parentHeight, parentHeight)
        return imageView
    }

    private fun roundToHalf(value: Float): Float {
        return (value * 2).roundToInt() / 2f
    }

    private fun calculateRatingFromX(x: Float) {
        val percent = (x / parentWidth)
        val calculatedRating = maxRating * percent
        setRating(calculatedRating)
    }

    fun setRating(rating: Float) {
        this.rating = roundToHalf(rating)
        (0 until maxRating).forEach { position ->
            val imageView = getImageView(position)
            when {
                rating >= position + 1 -> imageView.setImageDrawable(selectedStateStateDrawable)
                rating >= position + 0.5f -> imageView.setImageDrawable(halfSelectedStateDrawable)
                else -> imageView.setImageDrawable(emptyStateDrawable)
            }
        }
    }

    fun getImageView(position: Int): ImageView = getChildAt(position) as ImageView
}