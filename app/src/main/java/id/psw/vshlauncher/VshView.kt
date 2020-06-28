package id.psw.vshlauncher

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.lang.Exception
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.math.roundToInt


/**
 * TODO: document your custom view class.
 */
class VshView : View {

    companion object{
        const val Deg2Rad = PI / 180f
        const val Rad2Deg = 180f / PI
        var padding = RectF(0f,0f,0f,0f)
        private var padOffset = RectF(0f,0f,0f,0f)
    }

    /// region Variable
    private var TAG = "VshView.cpp"
    private var frame = 0f
    private var frameStart = 0L
    private var paintIconSelected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintMenuTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintSubtextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var paintIconUnselected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintTextUnselected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintSubtextUnselected = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var paintStatusBoxOutline = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintMisc = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintStatusBoxFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintStatusText = TextPaint(Paint.ANTI_ALIAS_FLAG)

    var selectedXf = 0f
    var selectedYf = 0f
    var selectedX = 0
    var selectedY = 0
    var menuIndex = 0

    var category : ArrayList<VshX> = arrayListOf()
    var hideMenu = false
    var backgroundAlpha = 1f

    var density = 1f
    var scaledDensity = 1f

    /// endregion Variable

    fun d(i:Float):Float{return i * density}
    fun d(i:Int):Int{return (i * density).toInt()}
    fun sd(i:Float):Float{return i * density}
    fun sd(i:Int):Int{return (i * scaledDensity).toInt()}

    private fun generatePaint(){
        paintTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.WHITE
            textSize = sd(18f)
            textAlign = Paint.Align.LEFT
            setShadowLayer(sd(5f), 0f,0f, Color.argb(255,255,255,255))
        }
        paintTextUnselected = TextPaint(paintTextSelected).apply {
            alpha = 128
            textSize = sd(15f)
            setShadowLayer(sd(2f), 0f,0f, Color.argb(128,0,0,0))
        }

        paintSubtextSelected = TextPaint(paintTextSelected).apply{
            textSize = sd(12f)
        }

        paintSubtextUnselected = TextPaint(paintTextSelected).apply{
            alpha = 128
            textSize = sd(10f)
            setShadowLayer(sd(2f), 0f,0f, Color.argb(128,0,0,0))
        }

        paintMenuTextSelected = TextPaint(paintTextSelected).apply {
            textSize = sd(15f)
            textAlign = Paint.Align.CENTER
            setShadowLayer(0f,0f,0f,Color.BLACK)
        }

        paintStatusText = TextPaint(paintMenuTextSelected).apply {
            textAlign = Paint.Align.RIGHT
            textSize = sd(15f)
        }
        paintStatusBoxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(64,255,255,255)
            style = Paint.Style.FILL
        }

        paintStatusBoxOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(128,255,255,255)
            style = Paint.Style.STROKE
            strokeWidth = d(1.5f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        paintIconUnselected = TextPaint(paintIconSelected).apply { alpha = 128 }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VshView, defStyle, 0
        )
        a.recycle()

        density = resources.displayMetrics.density
        scaledDensity = resources.displayMetrics.scaledDensity
        generatePaint()
        fillCategory()
    }


    @Suppress("DEPRECATION")
    fun fillCategory(){
        val home = VshX("HOME", "Halaman Depan", resources.getDrawable(R.drawable.category_home), density)
        category.add(home)
        category.add(VshX("APPS", "Apl", resources.getDrawable(R.drawable.category_apps), density))
        category.add(VshX("GAME", "Permainan", resources.getDrawable(R.drawable.category_games), density))
        category.add(VshX("FILM", "Video", resources.getDrawable(R.drawable.category_video), density))
        category.add(VshX("SONG", "Musik", resources.getDrawable(R.drawable.category_music), density))
        category.add(VshX("SETT", "Pengaturan", resources.getDrawable(R.drawable.category_setting), density))
        val debug = VshX("DBUG", "Debug", resources.getDrawable(R.drawable.category_debug), density)
        category.add(debug)
        // Debug items
        debug.items.add(VshY(0xd1, "Show Debug Info", showDebugInfo.toString(), onClick = Runnable{
            showDebugInfo = !showDebugInfo
            debug.getItemBy(0xd1)?.subtext = showDebugInfo.toString()
        },icon = resources.getDrawable(R.drawable.category_debug), density = density
            ))
        debug.items.add(VshY(0xd2, "Set Clock Loading Mode", clockAsLoadingIndicator.toString(), onClick = Runnable{
            clockAsLoadingIndicator = !clockAsLoadingIndicator
            debug.getItemBy(0xd2)?.subtext = clockAsLoadingIndicator.toString()
        },icon = resources.getDrawable(R.drawable.category_debug), density = density
        ))

        home.items.add(VshY(0x00, "Mulai", "Tap di ikon untuk menjalankan aplikasi.",
            icon = resources.getDrawable(R.drawable.icon_start),
            density = density,
            onClick = Runnable {
                setSelectionAbs(1,0)
            }
        ))
        home.items.add(VshY(0x01, "Tutup", "Atau tap ikon ini untuk menyembunikan menu.",
            icon = resources.getDrawable(R.drawable.icon_start),
            density = density,
            onClick = Runnable {
                hideMenu = !hideMenu
            }
        ))
    }

    // Draw text from it's top instead of baseline like JS does :P
    private fun Canvas.drawTextU(text:String, x:Float, y:Float, paint:TextPaint){
        text.split("\n").forEachIndexed { index, s ->
            val ym = y + paint.textSize + (paint.textSize * index)
            drawText(s, x, ym, paint)
        }
    }

    private fun Canvas.drawTextC(text:String, x:Float, y:Float, paint:TextPaint){
        text.split("\n").forEachIndexed { index, s ->
            val ym = y + (paint.textSize/2f) + (paint.textSize * index)
            drawText(s, x, ym, paint)
        }
    }

    /// region Size Change

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if(isInEditMode){
            // Just call super and return...
            super.onSizeChanged(w, h, oldw, oldh)
            return
        }
        padOffset.top = padding.top
        padOffset.left = padding.left
        padOffset.right = w - padding.right
        padOffset.bottom = h - padding.bottom
        super.onSizeChanged(w, h, oldw, oldh)
    }
    /// endregion

    /// region Status Bar Rendering
    private var upperClockBg = RectF(0f,0f,0f,0f)
    var clockExpandInfo = ""
    var operatorName = ""
    var use24Format = false
    private var lastClockExpandInfo = "-"
    private var clockPath = Path()
    private var clockHand = PointF(0f,0f)
    var clockAsLoadingIndicator = false
    private var clockStr = "10:43"
    val cal = Calendar.getInstance()

    private fun calculateHandRotation(t : Float, r : Float){
        // Do rotating the clock by frame instead of clock
        if(clockAsLoadingIndicator){
            clockHand.x = cos( ( ((frame % 60 / 60f) * 360) - 90 ) * Deg2Rad).toFloat() * r
            clockHand.y = sin( ( ((frame % 60 / 60f) * 360) - 90 ) * Deg2Rad ).toFloat() * r
        }else{
            clockHand.x = cos( ( (t * 360) - 90 ) * Deg2Rad).toFloat() * r
            clockHand.y = sin( ( (t * 360) - 90 ) * Deg2Rad ).toFloat() * r
        }
    }

    private fun updateClock(x:Float, y:Float){
        cal.timeInMillis = System.currentTimeMillis()
        clockStr = if(use24Format){
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(cal.time)
        }else{
            SimpleDateFormat("dd/MM hh:mm aa", Locale.getDefault()).format(cal.time)
        }

        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)

        clockPath.reset()
        clockPath.addCircle(x,y, sd(10f), Path.Direction.CW)

        // Animated Clock
        if(clockAsLoadingIndicator){
            val tt = frame % 60f / 60f
            clockPath.addCircle(x,y,sd(tt * 12f), Path.Direction.CW)
        }

        clockPath.moveTo(x,y)

        // Draw hour hand
        calculateHandRotation((hour % 12 ) / 12f, 5f)
        clockPath.lineTo(x + d(clockHand.x), y + d(clockHand.y))
        clockPath.lineTo(x,y)

        // Draw minute hand
        calculateHandRotation((minute % 60)/60f, 7f)
        clockPath.lineTo(x + d(clockHand.x ), y + d(clockHand.y))
        clockPath.lineTo(x,y)
        clockPath.close()
    }

    fun recalculateClockRect(){
        val yPivot = height * 0.1f
        val l = width - (d(300f) + (width * 0.1f))
        val u = yPivot - sd(15f)
        val r = width + sd(100f)
        val height = if(clockExpandInfo.isEmpty()) 10f else 25f
        val d = yPivot + sd(height)
        upperClockBg = RectF(l,u,r,d)
        val infoStatusX = width- (d(35f) + (width * 0.1f))
        expandInfoClipRect = RectF(l,u,infoStatusX,d)
        fullCanvasRect = RectF(0f,0f,width.toFloat(), this.height.toFloat())
    }

    private var lastWidth = 0
    private var lastHeight = 0
    private var expandInfoClipRect = RectF(0f,0f,0f,0f)
    private var fullCanvasRect = RectF(0f,0f,0f,0f)
    private var expandInfoTextRect = Rect(0,0,0,0)

    private fun lClock(canvas: Canvas){
        if(clockExpandInfo != lastClockExpandInfo || lastWidth != width || lastHeight != height || expandInfoClipRect.isEmpty){
            recalculateClockRect()
        }

        canvas.drawRoundRect(upperClockBg, 5f * density, 5f * density, paintStatusBoxFill)
        canvas.drawRoundRect(upperClockBg, 5f * density, 5f * density, paintStatusBoxOutline)

        val statusText = "$operatorName     $clockStr"
        var statusX = width - (sd(35f)+ (width * 0.1f))

        updateClock(width - (d(15f) + (width * 0.1f)), upperClockBg.centerY())


        paintStatusBoxFill.color = Color.argb(64,0,0,0)
        paintStatusBoxOutline.color = Color.WHITE

        canvas.drawPath(clockPath, paintStatusBoxFill)
        canvas.drawPath(clockPath, paintStatusBoxOutline)

        paintStatusBoxFill.color = Color.argb(64,255,255,255)
        paintStatusBoxOutline.color = Color.argb(128,255,255,255)

        lastClockExpandInfo = clockExpandInfo

        if(clockExpandInfo.isEmpty()){
            canvas.drawTextC(statusText , statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
        }else{
            canvas.drawText(statusText, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)

            paintStatusText.getTextBounds(clockExpandInfo, 0, clockExpandInfo.length, expandInfoTextRect)

            if(expandInfoClipRect.width() < expandInfoTextRect.width()){
                canvas.clipRect(expandInfoClipRect)

                statusX -= (frame * 2) % (expandInfoTextRect.width()*2f)

                paintStatusText.textAlign = Paint.Align.LEFT
                canvas.drawTextU(clockExpandInfo, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
                paintStatusText.textAlign = Paint.Align.RIGHT

                // Reset Canvas Rect
                canvas.clipRect(fullCanvasRect)
            }else{

                // Just render it as-is
                canvas.drawTextU(clockExpandInfo, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
            }
        }
    }
    /// endregion

    private fun lHorizontalMenu(canvas:Canvas){
        val pivotX = width * 0.3f
        val pivotY = height * 0.3f
        category.forEachIndexed{ index, data ->

            val iconPaint = if(index == selectedX){paintIconSelected}else{paintIconUnselected}
            val icon = if(index == selectedX){data.selectedIcon}else{data.unselectedIcon}

            val screenX = (pivotX + ((index - selectedXf) * d(100))) - (icon.width / 2f)
            // Don't render item outside outside
            if(screenX > -icon.width && screenX < width+icon.width){
                canvas.drawBitmap( icon ,screenX, pivotY - (icon.height/2f), iconPaint)
                if(index == selectedX){
                    canvas.drawTextC(data.name, screenX + (icon.width / 2f), pivotY + (icon.height / 2f), paintMenuTextSelected )
                }
            }
        }
    }

    private fun lVerticalItems(canvas:Canvas){
        val pivotX = (width * 0.3f) - ((selectedX - selectedXf) * d(-100))
        val pivotY = height * 0.3f

        // Don't do any rendering if empty
        if(category[selectedX].items.isEmpty()) return

        try{
            category[selectedX].items.forEachIndexed { index, data ->
                val isSelected = index == selectedY
                val iconPaint = if (isSelected) {
                    paintIconSelected
                } else {
                    paintIconUnselected
                }
                val textPaint = if (isSelected) {
                    paintTextSelected
                } else {
                    paintTextUnselected
                }
                val subtextPaint = if (isSelected) {
                    paintSubtextSelected
                } else {
                    paintSubtextUnselected
                }

                val icon = if (isSelected) {
                    data.selectedIcon
                } else {
                    data.unselectedIcon
                }
                var centerY = pivotY + ((index - selectedYf) * d(60f)) + d(100f)
                var screenY =
                    pivotY + ((index - selectedYf) * d(60f)) - (icon.height / 2f) + d(100f)
                if (index - selectedY < 0) {
                    screenY -= d(110f)
                    centerY -= d(110f)
                }
                if (index - selectedY > 0) {
                    screenY += d(30f)
                    centerY += d(30f)
                }

                // don't render offscreen
                if (centerY > -icon.height && centerY < height + icon.height) {
                    canvas.drawBitmap(icon, pivotX - (icon.width / 2f), screenY, iconPaint)
                    canvas.drawText(
                        data.text,
                        pivotX + (d(50f)),
                        screenY + (icon.height / 2f),
                        textPaint
                    )
                    canvas.drawTextU(
                        data.subtext,
                        pivotX + (d(50f)),
                        screenY + (icon.height / 2f),
                        subtextPaint
                    )
                }
            }
        }catch(cmfce:ConcurrentModificationException){
            cmfce.printStackTrace()
        }
    }

    /// region Options Popup
    var isOnOptions = false
    var optionsRect = RectF(0f,0f,0f,0f)
    private fun lOptions(canvas:Canvas){
        if(optionsRect.isEmpty){
            optionsRect.left = width - (d(200f) + padOffset.right)
            optionsRect.top = -d(20f)
            optionsRect.bottom = height + d(30f)
            optionsRect.right = width + d(10f)
        }
        canvas.drawRect(optionsRect, paintStatusBoxFill)
        canvas.drawRect(optionsRect, paintStatusBoxOutline)
    }

    /// endregion

    private fun mUpdate(){
        selectedYf = 0.75f.toLerp(selectedY.toFloat(), selectedYf)
        selectedXf = 0.75f.toLerp(selectedX.toFloat(), selectedXf)
        backgroundAlpha = 0.1f.toLerp(backgroundAlpha, hideMenu.choose(0f, 0.5f))
        frame++
    }

    ///region FPS Meter
    private var fpsRect = RectF(0f,0f,0f,0f)
    private fun lFPSMeter(canvas:Canvas){
        val ms = System.currentTimeMillis() - frameStart
        if(fpsRect.isEmpty || lastWidth != width || lastHeight != height){
            fpsRect.left = sd(20f)
            fpsRect.top = (height * 0.9f) - (sd(12f))
            fpsRect.right = sd(200f)
            fpsRect.bottom = fpsRect.top + sd(25f)
        }

        val fps = (1000f / ms).roundToInt()
        canvas.drawRoundRect(fpsRect, 10f,10f,paintStatusBoxFill)
        canvas.drawRoundRect(fpsRect, 10f,10f,paintStatusBoxOutline)
        canvas.drawTextC("$fps FPS / $ms ms", fpsRect.centerX(), fpsRect.centerY(), paintMenuTextSelected)
    }
    /// endregion
    private fun mLateUpdate(){

    }

    /// region Debug Info
    var showDebugInfo = false
    private fun lDebugInfo(canvas:Canvas){
        arrayListOf(
            frame,
            frameStart,
            selectedX,
            selectedXf,
            paintStatusBoxOutline).forEachIndexed{ index, any ->
            canvas.drawText(any.toString(), d(10f), d(75f) + (d(15f)* index), paintTextSelected)
        }
    }
    /// endregion Debug Info

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mUpdate()
        canvas.drawColor(Color.argb((255 * backgroundAlpha).floorToInt(),0,0,0))
        if(!hideMenu){
            lVerticalItems(canvas)
            lHorizontalMenu(canvas)
        }
        if(isOnOptions) lOptions(canvas)
        lClock(canvas)
        mLateUpdate()
        lFPSMeter(canvas)
        if(showDebugInfo) lDebugInfo(canvas)
        frameStart = System.currentTimeMillis()
        postInvalidate()

        lastWidth = width
        lastHeight = height
    }

    /// region Input
    fun setSelection(x:Int, y:Int){
        if(hideMenu) return
        if(x != 0){
            category[selectedX].itemY = selectedY
        }

        selectedX = (selectedX + x).coerceIn(0, category.size - 1)
        if(category[selectedX].items.isNotEmpty()){
            selectedY = (selectedY + y).coerceIn(0, category[selectedX].items.size - 1)
        }

        if(x != 0){
            selectedY = category[selectedX].itemY
            // Skip sliding animation when changing between categories
            selectedYf = selectedY.toFloat()
        }
    }

    fun setSelectionAbs(x:Int, y:Int){
        if(hideMenu) return
        if(x != selectedX){
            category[selectedX].itemY = selectedY
        }

        selectedX = x.coerceIn(0, category.size - 1)
        if(category[selectedX].items.isNotEmpty()){
            selectedY = y.coerceIn(0, category[selectedX].items.size - 1)
        }

        // Skip vertical sliding animation
        selectedYf = selectedY.toFloat()
    }

    fun executeCurrentItem(){
        if(hideMenu) {
            // Press Menu to Unhide
            hideMenu = false
        }else{
            try{
                category[selectedX].items[selectedY].onClick.run()
            }catch (ex:Exception){ ex.printStackTrace() }
        }
    }

    fun findById(id:String) :VshX? = category.find { it.id == id }
    /// endregion
}