package co.techbrewery.smartratingbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnTouchListener
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

    var onRatingChanged: ((Float) -> Unit)? = null

    var rating: Float = 0f
        set(value) {
            field = roundRating(value)
            (0 until maxRating).forEach { position ->
                val imageView = getImageView(position)
                updateDrawable(imageView, position)
            }
        }
    var allowHalf = true

    var interactionEnabled = true
        set(value) {
            field = value
            if (interactionEnabled) setOnTouchListener(touchListener)
            else setOnTouchListener(null)
        }

    var maxRating = DEFAULT_MAX_RATING
        set(value) {
            field = value
            buildView()
        }

    var emptyStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black_outline)
    var halfSelectedStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black_half)
    var selectedStateStateDrawable =
        ContextCompat.getDrawable(context, R.drawable.ic_star_black)

    private val touchListener = OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_MOVE) {
            setRatingOnMovement(calculateRatingFromX(event.x))
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            setRatingOnTap(calculateRatingFromX(event.x))
        } else if (event.action == MotionEvent.ACTION_UP) {
            onRatingChanged?.invoke(rating)
        }
        true
    }

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

            allowHalf =
                srbAttributesTypedArray.getBoolean(R.styleable.SmartRatingBar_allowHalf, true)
            interactionEnabled =
                srbAttributesTypedArray.getBoolean(
                    R.styleable.SmartRatingBar_interactionEnabled,
                    true
                )

            val preciseRating =
                srbAttributesTypedArray.getFloat(R.styleable.SmartRatingBar_rating, 0f)
            rating = roundRating(preciseRating)

            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_emptyStateDrawable)) emptyStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_emptyStateDrawable)
            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_halfSelectedStateDrawable)) halfSelectedStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_halfSelectedStateDrawable)
            if (srbAttributesTypedArray.hasValue(R.styleable.SmartRatingBar_selectedStateDrawable)) selectedStateStateDrawable =
                srbAttributesTypedArray.getDrawable(R.styleable.SmartRatingBar_selectedStateDrawable)

            srbAttributesTypedArray.recycle()
        }

        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> resizeChildren() }

        if (interactionEnabled) setOnTouchListener(touchListener)
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
        removeAllViews()
        (0 until maxRating).forEach { position ->
            val imageView = createImageView()
            updateDrawable(imageView, position)
            addView(imageView)
        }
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(context)
        tintColor?.let { imageView.setColorFilter(it) }
        imageView.layoutParams = LayoutParams(parentHeight, parentHeight)
        return imageView
    }

    private fun roundRating(preciseRating: Float): Float {
        return if (allowHalf) roundToHalf(preciseRating)
        else preciseRating.roundToInt().toFloat()
    }

    private fun roundToHalf(value: Float): Float {
        return (value * 2).roundToInt() / 2f
    }

    private fun calculateRatingFromX(x: Float): Float {
        val percent = (x / parentWidth)
        return maxRating * percent
    }

    private fun updateDrawable(imageView: ImageView, position: Int) {
        if (allowHalf) {
            when {
                rating >= position + 1 -> imageView.setImageDrawable(selectedStateStateDrawable)
                rating >= position + 0.5f -> imageView.setImageDrawable(
                    halfSelectedStateDrawable
                )
                else -> imageView.setImageDrawable(emptyStateDrawable)
            }
        } else {
            when {
                rating >= position + 1 -> imageView.setImageDrawable(selectedStateStateDrawable)
                else -> imageView.setImageDrawable(emptyStateDrawable)
            }
        }
    }

    private fun setRatingOnMovement(rating: Float) {
        this.rating = roundRating(rating)
        (0 until maxRating).forEach { position ->
            val imageView = getImageView(position)
            if (allowHalf) {
                when {
                    rating >= position + 1 -> imageView.setImageDrawable(selectedStateStateDrawable)
                    rating >= position + 0.5f -> imageView.setImageDrawable(
                        halfSelectedStateDrawable
                    )
                    else -> imageView.setImageDrawable(emptyStateDrawable)
                }
            } else {
                when {
                    rating >= position -> imageView.setImageDrawable(selectedStateStateDrawable)
                    else -> imageView.setImageDrawable(emptyStateDrawable)
                }
            }
        }
    }

    private fun setRatingOnTap(rating: Float) {
        this.rating = roundRating(rating)
        (0 until maxRating).forEach { position ->
            val imageView = getImageView(position)
            when {
                rating >= position -> imageView.setImageDrawable(selectedStateStateDrawable)
                else -> imageView.setImageDrawable(emptyStateDrawable)
            }
        }
    }

    private fun getImageView(position: Int): ImageView = getChildAt(position) as ImageView

}