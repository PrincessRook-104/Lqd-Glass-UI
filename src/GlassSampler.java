package luma.ui;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
/** Copies game SurfaceView regions at reduced size for the liquid glass panel and the menu backdrop blur. Never captures the Luma overlay and never saves or sends frames. */
final class GlassSampler {
  interface Result {void frame(Bitmap bitmap);void backdrop(Bitmap bitmap,float left,float top,float right,float bottom);void unavailable();}
  private static final float BACKDROP_LONG_SIDE=160f;private volatile long interval=91;
  private final Activity activity;private final View anchor;private final Result callback;private final Handler handler=new Handler(Looper.getMainLooper());
  private final Bitmap[] buffers=new Bitmap[2];private int back;private Bitmap backdropBuffer;
  private boolean running,pending,wantPanel=true,wantBackdrop;private int generation;private float x,y,w,h;
  GlassSampler(Activity a,View anchor,Result r){activity=a;this.anchor=anchor;callback=r;}
  void start(){if(running)return;running=true;generation++;handler.post(tick);}
  void stop(){running=false;generation++;pending=false;handler.removeCallbacks(tick);}
  /** Milliseconds between backdrop captures (1000 = 1 fps ... 8 = 120 fps). A new capture never starts before the previous one finished. */
  void interval(long ms){interval=Math.max(1L,ms);}
  void configure(boolean panel,boolean backdrop){wantPanel=panel;wantBackdrop=backdrop;}
  void bounds(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;}
  private final Runnable tick=new Runnable(){public void run(){sample();}};
  private void later(){if(running){handler.removeCallbacks(tick);handler.postDelayed(tick,interval);}}
  private static SurfaceView largest(View v,SurfaceView best){
    if(v instanceof SurfaceView&&v.isShown()&&v.getWidth()>20&&v.getHeight()>20){SurfaceView s=(SurfaceView)v;if(best==null||(long)s.getWidth()*s.getHeight()>(long)best.getWidth()*best.getHeight())best=s;}
    if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)best=largest(g.getChildAt(i),best);}return best;
  }
  private void fail(){callback.unavailable();later();}
  private boolean usable(SurfaceView s){return s!=null&&s.getHolder().getSurface().isValid();}
  private void sample(){if(!running||pending)return;SurfaceView surface=largest(activity.getWindow().getDecorView(),null);
    if(!usable(surface)){fail();return;}
    if(wantPanel)panel(surface);else if(wantBackdrop)backdrop(surface);else later();
  }
  private void panel(final SurfaceView surface){
    if(w<=0||h<=0){fail();return;}
    int[] src=new int[2],pos=new int[2];surface.getLocationOnScreen(src);anchor.getLocationOnScreen(pos);
    int left=Math.round(pos[0]+x-src[0]),top=Math.round(pos[1]+y-src[1]);
    int right=Math.round(pos[0]+x+w-src[0]),bottom=Math.round(pos[1]+y+h-src[1]);
    if(left<0||top<0||right>surface.getWidth()||bottom>surface.getHeight()||right<=left||bottom<=top){if(wantBackdrop){callback.unavailable();backdrop(surface);}else fail();return;}
    float scale=GlassMath.captureScale(right-left,bottom-top);int tw=Math.max(16,Math.round((right-left)*scale)),th=Math.max(16,Math.round((bottom-top)*scale));
    Bitmap target=buffers[back];
    if(target==null||target.getWidth()!=tw||target.getHeight()!=th){try{target=Bitmap.createBitmap(tw,th,Bitmap.Config.ARGB_8888);}catch(OutOfMemoryError e){fail();return;}buffers[back]=target;}
    final Bitmap bmp=target;final int request=generation;pending=true;
    try{PixelCopy.request(surface,new Rect(left,top,right,bottom),bmp,result->{
      pending=false;
      if(!running||request!=generation)return;
      if(result==PixelCopy.SUCCESS){back^=1;callback.frame(bmp);}else callback.unavailable();
      if(wantBackdrop&&usable(surface))backdrop(surface);else later();
    },handler);}catch(RuntimeException e){pending=false;if(running&&request==generation)callback.unavailable();later();}
  }
  private void backdrop(final SurfaceView surface){
    int sw=surface.getWidth(),sh=surface.getHeight();if(sw<=0||sh<=0){later();return;}
    float scale=Math.min(1f,BACKDROP_LONG_SIDE/Math.max(sw,sh));int tw=Math.max(16,Math.round(sw*scale)),th=Math.max(16,Math.round(sh*scale));
    if(backdropBuffer==null||backdropBuffer.getWidth()!=tw||backdropBuffer.getHeight()!=th){try{backdropBuffer=Bitmap.createBitmap(tw,th,Bitmap.Config.ARGB_8888);}catch(OutOfMemoryError e){later();return;}}
    int[] src=new int[2],pos=new int[2];surface.getLocationOnScreen(src);anchor.getLocationOnScreen(pos);
    final float l=src[0]-pos[0],t=src[1]-pos[1],r=l+sw,b=t+sh;final Bitmap bmp=backdropBuffer;final int request=generation;pending=true;
    try{PixelCopy.request(surface,new Rect(0,0,sw,sh),bmp,result->{
      pending=false;
      if(running&&request==generation&&result==PixelCopy.SUCCESS)callback.backdrop(bmp,l,t,r,b);
      later();
    },handler);}catch(RuntimeException e){pending=false;later();}
  }
}
