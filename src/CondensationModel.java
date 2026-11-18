package luma.ui;
import java.util.ArrayList;
import java.util.Random;
/** Pure-Java condensation simulation: a fog density grid plus water droplets that grow, merge, can be wiped away and run down the glass. All positions are in panel units (u), origin at the panel's top-left. */
final class CondensationModel {
  static final float CELL=4f;
  static final class Drop{float x,y,r,vy,critical;boolean sliding;int id;}
  final ArrayList<Drop> drops=new ArrayList<Drop>();
  int gw,gh;float widthU,heightU;float[] fog=new float[0];
  float fogTarget=.7f,dropRate=.5f,drip=.5f,regrow=.3f;
  private final Random random;private float spawnCarry,time,lastX,lastY;private boolean wiping;private int nextId;
  CondensationModel(long seed){random=new Random(seed);}
  static float c01(float v){return v<0f?0f:v>1f?1f:v;}
  static int dropCap(float dropRate){return (int)(50+c01(dropRate)*100);}
  void configure(float fogTarget,float dropRate,float drip,float regrow){this.fogTarget=c01(fogTarget);this.dropRate=c01(dropRate);this.drip=c01(drip);this.regrow=c01(regrow);}
  /** Sizes the grid for the panel. Returns true when the simulation was (re)started. */
  boolean ensure(float wu,float hu){
    if(wu<8f||hu<8f)return false;
    int nw=Math.max(8,(int)Math.ceil(wu/CELL)),nh=Math.max(8,(int)Math.ceil(hu/CELL));
    widthU=wu;heightU=hu;
    if(gw>0&&Math.abs(nw-gw)<2&&Math.abs(nh-gh)<2)return false;
    gw=nw;gh=nh;fog=new float[gw*gh];refog();
    return true;
  }
  /** Fogs the whole glass again and seeds a few starting droplets. */
  void refog(){
    if(gw==0)return;
    java.util.Arrays.fill(fog,fogTarget*.85f);drops.clear();spawnCarry=0f;
    int seeds=(int)(8+dropRate*30);for(int i=0;i<seeds;i++)trySpawn();
  }
  float fogAt(float x,float y){
    if(gw==0)return 0f;
    int ix=Math.max(0,Math.min(gw-1,(int)(x/CELL))),iy=Math.max(0,Math.min(gh-1,(int)(y/CELL)));return fog[iy*gw+ix];
  }
  private void trySpawn(){
    for(int attempt=0;attempt<6;attempt++){
      float x=3f+random.nextFloat()*Math.max(1f,widthU-6f),y=3f+random.nextFloat()*Math.max(1f,heightU-6f);
      if(fogAt(x,y)<.3f)continue;
      Drop d=new Drop();d.id=nextId++;d.x=x;d.y=y;float q=random.nextFloat();d.r=.9f+q*q*1.8f;d.critical=4.2f+random.nextFloat()*2.4f;drops.add(d);return;
    }
  }
  /** Scales fog down inside a soft disc. core is the fraction of the radius that is cleared completely. */
  void carve(float cx,float cy,float r,float core){
    if(gw==0||r<=0f)return;
    int x0=Math.max(0,(int)((cx-r)/CELL)),x1=Math.min(gw-1,(int)((cx+r)/CELL)),y0=Math.max(0,(int)((cy-r)/CELL)),y1=Math.min(gh-1,(int)((cy+r)/CELL));
    for(int iy=y0;iy<=y1;iy++)for(int ix=x0;ix<=x1;ix++){
      float px=(ix+.5f)*CELL-cx,py=(iy+.5f)*CELL-cy;float t=(float)Math.sqrt(px*px+py*py)/r;if(t>=1f)continue;
      float keep=t<=core?0f:(t-core)/(1f-core);keep=keep*keep*(3f-2f*keep);fog[iy*gw+ix]*=keep;
    }
  }
  private void wipeAt(float x,float y,float r){
    carve(x,y,r,.45f);
    for(int i=drops.size()-1;i>=0;i--){Drop d=drops.get(i);float dx=d.x-x,dy=d.y-y,lim=r*.9f+d.r*.5f;if(dx*dx+dy*dy<lim*lim)drops.remove(i);}
  }
  /** Finger wipe: call on touch down and move; consecutive calls are joined into one continuous stroke. */
  void wipeStroke(float x,float y,float r){
    if(gw==0)return;
    if(!wiping){wipeAt(x,y,r);wiping=true;}
    else{float dx=x-lastX,dy=y-lastY;float dist=(float)Math.sqrt(dx*dx+dy*dy);int n=Math.max(1,(int)Math.ceil(dist/(r*.35f)));
      for(int k=1;k<=n;k++)wipeAt(lastX+dx*k/n,lastY+dy*k/n,r);}
    lastX=x;lastY=y;
  }
  void endWipe(){wiping=false;}
  void step(float dt){
    if(gw==0)return;
    dt=Math.min(dt,.05f);if(dt<=0f)return;time+=dt;
    float up=(.01f+.12f*regrow)*dt,down=.12f*dt;
    for(int i=0;i<fog.length;i++){float f=fog[i];if(f<fogTarget){f+=up;if(f>fogTarget)f=fogTarget;}else if(f>fogTarget){f-=down;if(f<fogTarget)f=fogTarget;}fog[i]=f;}
    spawnCarry+=dropRate*10f*Math.max(.15f,fogTarget)*dt;
    int cap=dropCap(dropRate);
    while(spawnCarry>=1f){spawnCarry-=1f;if(drops.size()<cap)trySpawn();}
    for(int i=drops.size()-1;i>=0;i--){
      Drop d=drops.get(i);
      if(d.sliding){slide(d,dt);if(d.y-d.r>heightU+2f||d.r<.9f)drops.remove(i);continue;}
      float here=fogAt(d.x,d.y);
      d.r+=here>.25f?(.10f+.22f*dropRate)*dt*(.5f+here):.02f*dt;
      if(d.r>d.critical){d.sliding=true;d.vy=3f;}
    }
    mergeAndAbsorb();
  }
  private void slide(Drop d,float dt){
    float g=14f+60f*drip,vmax=(18f+90f*drip)*(.6f+d.r/8f);
    d.vy=Math.min(vmax,d.vy+g*dt);
    d.x+=(float)Math.sin(time*6f+d.id)*.9f*dt;
    float move=d.vy*dt;int n=Math.max(1,(int)Math.ceil(move/Math.max(1f,d.r*.6f)));
    for(int k=0;k<n;k++){d.y+=move/n;carve(d.x,d.y,Math.max(2f,d.r),.5f);}
    d.r-=(.45f+.3f*drip)*dt;
    if(d.r<2.8f&&random.nextFloat()<.35f*dt){d.sliding=false;d.vy=0f;d.critical=d.r+3.5f+random.nextFloat()*2f;}
  }
  private void merge(Drop a,Drop b){
    float a2=a.r*a.r,b2=b.r*b.r,sum=a2+b2;
    a.x=(a.x*a2+b.x*b2)/sum;a.y=(a.y*a2+b.y*b2)/sum;a.r=(float)Math.sqrt(sum);b.r=0f;
  }
  private void mergeAndAbsorb(){
    for(int i=0;i<drops.size();i++){
      Drop a=drops.get(i);if(a.r<=0f||a.sliding)continue;
      for(int j=i+1;j<drops.size();j++){Drop b=drops.get(j);if(b.r<=0f||b.sliding)continue;
        float dx=a.x-b.x,dy=a.y-b.y,lim=(a.r+b.r)*.8f;if(dx*dx+dy*dy<lim*lim)merge(a,b);}
    }
    for(int i=0;i<drops.size();i++){
      Drop s=drops.get(i);if(s.r<=0f||!s.sliding)continue;
      for(int j=0;j<drops.size();j++){Drop e=drops.get(j);if(e.r<=0f||e.sliding)continue;
        float dx=s.x-e.x,dy=s.y-e.y,lim=(s.r+e.r)*.9f;
        if(dx*dx+dy*dy<lim*lim){s.r=Math.min(8.5f,(float)Math.sqrt(s.r*s.r+e.r*e.r));e.r=0f;}}
    }
    for(int i=drops.size()-1;i>=0;i--)if(drops.get(i).r<=0f)drops.remove(i);
  }
}
