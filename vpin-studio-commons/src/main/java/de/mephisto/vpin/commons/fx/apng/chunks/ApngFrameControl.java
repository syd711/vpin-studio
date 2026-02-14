package de.mephisto.vpin.commons.fx.apng.chunks;

/**
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG_Specification</a>
 */
public class ApngFrameControl
{
  /** No disposal is done on the frame before rendering the next; the contents of the output buffer are left as is. */
  public static final int DISPOSE_OP_NONE = 0;
  /** The frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame. */
  public static final int DISPOSE_OP_BACKGROUND = 1;
  /** The frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame. */
  public static final int DISPOSE_OP_PREVIOUS = 2;

  /** All color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region. */
  public static final int BLEND_OP_SOURCE = 0;
  /** The frame should be composited onto the output buffer based on its alpha, using a simple OVER operation */
  public static final int BLEND_OP_OVER = 1;

  int width;
  int height;
  int xOffset;
  int yOffset;
  int delayNum;
  int delayDen;
  int disposeOp;
  int blendOp;

  public ApngFrameControl(int width, int height, int xOffset, int yOffset,
            int delayNum, int delayDen, int disposeOp, int blendOp)
  {
    this.width = width;
    this.height = height;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.delayNum = delayNum;
    this.delayDen = delayDen;
    this.disposeOp = disposeOp;
    this.blendOp = blendOp;
  }

  /**
   * delay_num and delay_den define the numerator and denominator of the delay fraction; 
   * indicating the time to display the current frame, in seconds. If the denominator is 0, it is to be treated 
   * as if it were 100 (that is, delay_num then specifies 1/100ths of a second). If the the value of the numerator 
   * is 0 the decoder should render the next frame as quickly as possible, though viewers may impose a reasonable 
   * lower bound. They are encoded as two-byte unsigned integers.
   * @return
   */
  public int getDelayMillis() {
    int num = Math.max(0, Math.min(65535, delayNum));
    int den = delayDen == 0 ? 100 : Math.max(1, Math.min(65535, delayDen));
    int time = (int) (1000.0 * num / den);
    return num == 0? (den == 0 ? 0 : 1) : time;
  }

  //--------------------------------------

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getXOffset() {
    return xOffset;
  }

  public int getYOffset() {
    return yOffset;
  }

  public int getDelayNum() {
    return delayNum;
  }

  public int getDelayDen() {
    return delayDen;
  }

  public int getDisposeOp() {
    return disposeOp;
  }

  public int getBlendOp() {
    return blendOp;
  }
}
