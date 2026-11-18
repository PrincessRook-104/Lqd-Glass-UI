package luma.ui;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
/** iOS-style liquid glass for the panel: a blurred copy of the game behind it, optional edge refraction with slight chromatic dispersion (Android 13+), and a moving specular rim. */
final class LiquidGlass {
  private Bitmap sharp,blurFront;private final Bitmap[] blurs=new Bitmap[2];private int blurNext;
  private Bitmap small;private Canvas smallCanvas;private int[] px,tmp;
  private final Paint down=new Paint(Paint.FILTER_BITMAP_FLAG),fill=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG),line=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Rect srcRect=new Rect(),dstRect=new Rect();private final RectF rect=new RectF();private final Path clip=new Path();
  private Bitmap backdropFront;private final Bitmap[] backdrops=new Bitmap[2];private int backdropNext;private int[] bpx,btmp;private final RectF backdropRect=new RectF();
  private GlassAgsl agsl;private boolean agslFailed;
  Bitmap sharpBitmap(){return sharp;}
  Bitmap blurBitmap(){return blurFront;}
  boolean ready(){return sharp!=null&&blurFront!=null;}
  void clear(){sharp=null;blurFront=null;}
  /** UI thread. bitmap is owned by the sampler; it is only read here and again by drawing until the sampler writes its other buffer. */
  void accept(Bitmap bitmap,float blurAmount){
    int bw=Math.max(12,bitmap.getWidth()*3/10),bh=Math.max(8,bitmap.getHeight()*3/10);
    try{
      if(small==null||small.getWidth()!=bw||small.getHeight()!=bh){small=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);smallCanvas=new Canvas(small);blurs[0]=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);blurs[1]=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);px=new int[bw*bh];tmp=new int[bw*bh];blurFront=null;}
    }catch(OutOfMemoryError e){clear();return;}
    srcRect.set(0,0,bitmap.getWidth(),bitmap.getHeight());dstRect.set(0,0,bw,bh);smallCanvas.drawBitmap(bitmap,srcRect,dstRect,down);
    small.getPixels(px,0,bw,0,0,bw,bh);GlassMath.blur(px,bw,bh,GlassMath.blurRadius(blurAmount),3,tmp);
    Bitmap target=blurs[blurNext];target.setPixels(px,0,bw,0,0,bw,bh);blurFront=target;blurNext^=1;sharp=bitmap;
  }
  void clearBackdrop(){backdropFront=null;}
  /** UI thread. Heavily blurs a tiny copy of the whole game view, drawn behind the menu while it is open. */
  void acceptBackdrop(Bitmap bitmap,float left,float top,float right,float bottom){
    int bw=bitmap.getWidth(),bh=bitmap.getHeight();
    try{if(bpx==null||bpx.length!=bw*bh){bpx=new int[bw*bh];btmp=new int[bw*bh];backdrops[0]=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);backdrops[1]=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);backdropFront=null;}}
    catch(OutOfMemoryError e){bpx=null;backdropFront=null;return;}
    bitmap.getPixels(bpx,0,bw,0,0,bw,bh);GlassMath.blur(bpx,bw,bh,5,3,btmp);
    Bitmap target=backdrops[backdropNext];target.setPixels(bpx,0,bw,0,0,bw,bh);backdropFront=target;backdropNext^=1;backdropRect.set(left,top,right,bottom);
  }
  void drawBackdrop(Canvas c,float amount,float open){
    if(backdropFront==null||amount<=.01f||open<=0f)return;float a=Math.min(1f,amount)*open;
    fill.setShader(null);fill.setStyle(Paint.Style.FILL);fill.setColorFilter(null);fill.setAlpha((int)(255*a));c.drawBitmap(backdropFront,null,backdropRect,fill);
  }
  /** Draws the glass base for the panel. Returns false when no backdrop frame exists yet, so the caller keeps the normal panel. */
  boolean draw(Canvas c,float x,float y,float w,float h,float radius,float unit,float open,int panelColor,float tint,float refraction,float blurAmount,float brightness,float lightX,float lightY,int mode){
    boolean clear=GlassMath.isClear(blurAmount);
    if(open<=0f||(!ready()&&!(clear&&mode!=GlassMath.REFRACT)))return false;
    Bitmap s=sharp,b=clear?sharp:blurFront;float tintAlpha=Math.max(0f,Math.min(1f,tint));
    boolean refracted=false;
    if(mode==GlassMath.REFRACT&&s!=null&&b!=null&&Build.VERSION.SDK_INT>=33&&!agslFailed&&c.isHardwareAccelerated()){
      try{if(agsl==null)agsl=new GlassAgsl();agsl.draw(c,s,b,x,y,w,h,radius,14*unit,Math.max(0f,Math.min(1f,refraction)),.22f*Math.min(1f,refraction*1.8f),1.25f,panelColor,tintAlpha,open,clear?0f:1f);refracted=true;}
      catch(Throwable t){agslFailed=true;agsl=null;}
    }
    rect.set(x,y,x+w,y+h);
    if(!refracted){
      c.save();clip.rewind();clip.addRoundRect(rect,radius,radius,Path.Direction.CW);c.clipPath(clip);
      fill.setShader(null);fill.setStyle(Paint.Style.FILL);fill.setColorFilter(null);fill.setAlpha((int)(255*open));if(!clear&&b!=null)c.drawBitmap(b,null,rect,fill);
      fill.setColor((panelColor&0xffffff)|((int)(255*tintAlpha*open)<<24));c.drawRect(rect,fill);c.restore();
    }
    float veil=GlassMath.veil(brightness);
    if(Math.abs(veil)>.02f){fill.setShader(null);fill.setStyle(Paint.Style.FILL);fill.setColor((((int)Math.min(255,Math.abs(veil)*(veil>0?120:150)*open))<<24)|(veil>0?0xffffff:0));c.drawRoundRect(rect,radius,radius,fill);}
    rim(c,x,y,w,h,radius,unit,open,lightX,lightY);
    return true;
  }
  private static int white(float a){return ((int)Math.max(0,Math.min(255,a))<<24)|0xffffff;}
  private void rim(Canvas c,float x,float y,float w,float h,float radius,float unit,float open,float lx,float ly){
    line.setStyle(Paint.Style.STROKE);
    line.setStrokeWidth(1.4f*unit);
    line.setShader(new LinearGradient(x,y,x+w,y+h,new int[]{white(200*open),white(26*open),white(10*open),white(120*open)},new float[]{0f,.28f,.7f,1f},Shader.TileMode.CLAMP));
    rect.set(x+.7f*unit,y+.7f*unit,x+w-.7f*unit,y+h-.7f*unit);c.drawRoundRect(rect,radius-.7f*unit,radius-.7f*unit,line);
    float cx=x+w*lx,cy=y+h*ly;
    line.setStrokeWidth(2f*unit);
    line.setShader(new RadialGradient(cx,cy,Math.max(1f,Math.max(w,h)*.55f),white(150*open),white(0),Shader.TileMode.CLAMP));
    rect.set(x+1f*unit,y+1f*unit,x+w-1f*unit,y+h-1f*unit);c.drawRoundRect(rect,radius-1f*unit,radius-1f*unit,line);
    line.setShader(null);line.setStrokeWidth(.8f*unit);line.setColor(white(22*open));
    rect.set(x+2.4f*unit,y+2.4f*unit,x+w-2.4f*unit,y+h-2.4f*unit);c.drawRoundRect(rect,radius-2.4f*unit,radius-2.4f*unit,line);
    c.save();clip.rewind();rect.set(x,y,x+w,y+h);clip.addRoundRect(rect,radius,radius,Path.Direction.CW);c.clipPath(clip);
    fill.setStyle(Paint.Style.FILL);fill.setShader(new LinearGradient(x,y,x,y+16*unit,white(38*open),white(0),Shader.TileMode.CLAMP));
    c.drawRect(x,y,x+w,y+16*unit,fill);fill.setShader(null);c.restore();
    line.setShader(null);
  }
}
