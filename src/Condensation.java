package luma.ui;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import java.nio.ByteBuffer;
/** Draws the condensation on the glass: a frosted fog layer with wiped clear areas, and droplets that act as tiny upside-down lenses over the game. */
final class Condensation {
  final CondensationModel model=new CondensationModel(System.nanoTime());
  private Bitmap mask;private byte[] bytes;private ByteBuffer buffer;
  private final Paint layerPaint=new Paint(Paint.FILTER_BITMAP_FLAG),maskPaint=new Paint(Paint.FILTER_BITMAP_FLAG),veilPaint=new Paint(),dropPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG),ring=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Matrix lens=new Matrix();private BitmapShader lensShader;private Bitmap lensBitmap;
  private final Path clip=new Path();private final RectF rect=new RectF(),oval=new RectF();
  Condensation(){maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));veilPaint.setColor(0xffd5deea);veilPaint.setAlpha(52);ring.setStyle(Paint.Style.STROKE);}
  void step(float dt,float w,float h,float unit,float fogAmount,float droplets,float drip,float regrow){
    if(unit<=0f)return;
    model.configure(fogAmount,droplets,drip,regrow);model.ensure(w/unit,h/unit);model.step(dt);
  }
  void wipe(float ux,float uy,float radiusU){model.wipeStroke(ux,uy,radiusU);}
  void endWipe(){model.endWipe();}
  void refog(){model.refog();}
  private void updateMask(){
    int gw=model.gw,gh=model.gh;if(gw==0)return;
    if(mask==null||mask.getWidth()!=gw||mask.getHeight()!=gh){mask=Bitmap.createBitmap(gw,gh,Bitmap.Config.ALPHA_8);bytes=new byte[gw*gh];buffer=ByteBuffer.wrap(bytes);}
    float[] fog=model.fog;for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Math.round(255f*Math.min(1f,fog[i]*1.1f));
    buffer.rewind();mask.copyPixelsFromBuffer(buffer);
  }
  /** blur and sharp are the glass backdrop bitmaps (either may be null before the first frame arrives). */
  void draw(Canvas c,float x,float y,float w,float h,float radius,float unit,float open,Bitmap sharp,Bitmap blur){
    if(open<=.01f||model.gw==0)return;
    updateMask();rect.set(x,y,x+w,y+h);
    c.save();clip.rewind();clip.addRoundRect(rect,radius,radius,Path.Direction.CW);c.clipPath(clip);
    int layer=c.saveLayerAlpha(rect,(int)(255*Math.min(1f,open)));
    if(blur!=null){layerPaint.setAlpha(255);c.drawBitmap(blur,null,rect,layerPaint);}
    c.drawRect(rect,veilPaint);
    c.drawBitmap(mask,null,rect,maskPaint);
    c.restoreToCount(layer);
    drawDrops(c,x,y,w,h,unit,open,sharp);
    c.restore();
  }
  private void drawDrops(Canvas c,float x,float y,float w,float h,float unit,float open,Bitmap sharp){
    if(sharp!=null&&sharp!=lensBitmap){lensBitmap=sharp;lensShader=new BitmapShader(sharp,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);if(Build.VERSION.SDK_INT>=33)lensShader.setFilterMode(BitmapShader.FILTER_MODE_LINEAR);}
    float s=.8f,sx=sharp==null?1f:sharp.getWidth()/w,sy=sharp==null?1f:sharp.getHeight()/h;
    for(int i=0;i<model.drops.size();i++){
      CondensationModel.Drop d=model.drops.get(i);float cx=x+d.x*unit,cy=y+d.y*unit,r=d.r*unit;if(r<1.2f)continue;
      float ry=d.sliding?r*1.2f:r,rx=d.sliding?r*.9f:r;oval.set(cx-rx,cy-ry,cx+rx,cy+ry);
      if(lensShader!=null){
        // each droplet shows the game upside down and slightly minified, like water on real glass
        lens.setScale(-1f/(sx*s),-1f/(sy*s));lens.postTranslate(cx+(cx-x)/s,cy+(cy-y)/s);lensShader.setLocalMatrix(lens);
        dropPaint.setShader(lensShader);dropPaint.setAlpha((int)(200*open));
      }else{dropPaint.setShader(null);dropPaint.setColor(0xffffffff);dropPaint.setAlpha((int)(46*open));}
      c.drawOval(oval,dropPaint);dropPaint.setShader(null);
      ring.setShader(null);ring.setStrokeWidth(Math.max(1f,r*.16f));ring.setColor(0xff000000);ring.setAlpha((int)(72*open));c.drawOval(oval,ring);
      ring.setStrokeWidth(Math.max(1f,r*.12f));ring.setColor(0xffffffff);ring.setAlpha((int)(110*open));
      oval.inset(r*.18f,r*.18f);c.drawArc(oval,20f,70f,false,ring);
      dropPaint.setColor(0xffffffff);dropPaint.setAlpha((int)(215*open));c.drawCircle(cx-r*.35f,cy-r*.4f,Math.max(1f,r*.2f),dropPaint);
    }
  }
}
