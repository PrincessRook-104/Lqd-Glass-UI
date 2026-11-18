package luma.ui;
public final class CondensationModelTest {
  static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);}
  static CondensationModel fresh(float rate,float regrow){CondensationModel m=new CondensationModel(7L);m.configure(.7f,rate,.5f,regrow);m.ensure(600f,360f);return m;}
  public static void main(String[] a){
    CondensationModel m=fresh(.5f,.3f);
    check(m.gw==150&&m.gh==90,"grid size");
    check(Math.abs(m.fogAt(300f,180f)-.7f*.85f)<1e-4f,"initial fog");
    check(!m.drops.isEmpty(),"seed drops");
    // wiping clears fog and removes drops under the finger, but leaves the far side alone
    m.drops.clear();CondensationModel.Drop d=new CondensationModel.Drop();d.x=300f;d.y=180f;d.r=3f;d.critical=6f;m.drops.add(d);
    CondensationModel.Drop far=new CondensationModel.Drop();far.x=500f;far.y=300f;far.r=3f;far.critical=6f;m.drops.add(far);
    m.wipeStroke(300f,180f,24f);m.wipeStroke(360f,180f,24f);m.endWipe();
    check(m.fogAt(300f,180f)<.01f&&m.fogAt(330f,180f)<.01f&&m.fogAt(360f,180f)<.01f,"stroke clears fog along its path");
    check(m.fogAt(500f,300f)>.55f,"fog far away is untouched");
    check(m.drops.size()==1&&m.drops.get(0)==far,"drops under the stroke are wiped away");
    // fog comes back over time
    CondensationModel r=fresh(0f,1f);r.wipeStroke(300f,180f,24f);r.endWipe();
    for(int i=0;i<60*30;i++)r.step(1f/60f);
    check(r.fogAt(300f,180f)>.65f,"fog regrows after being wiped");
    // a drop past its critical size runs down, carving a clear trail
    CondensationModel s=fresh(0f,0f);s.drops.clear();s.configure(.7f,0f,.5f,0f);
    java.util.Arrays.fill(s.fog,.7f);
    CondensationModel.Drop run=new CondensationModel.Drop();run.x=200f;run.y=20f;run.r=5.5f;run.critical=5f;s.drops.add(run);
    for(int i=0;i<60*2;i++)s.step(1f/60f);
    check(run.sliding&&run.y>80f,"drop starts to slide and moves down");
    check(s.fogAt(run.x,40f)<.15f,"sliding drop leaves a clear trail behind it");
    check(s.fogAt(run.x+60f,40f)>.6f,"fog beside the trail stays");
    for(int i=0;i<60*20;i++)s.step(1f/60f);
    check(!s.drops.contains(run)||!run.sliding,"a running drop ends up gone or stuck, never sliding forever");
    // overlapping resting drops merge and conserve area
    CondensationModel g=fresh(0f,0f);g.drops.clear();
    CondensationModel.Drop p=new CondensationModel.Drop(),q=new CondensationModel.Drop();p.x=100f;p.y=100f;p.r=3f;p.critical=9f;q.x=102f;q.y=100f;q.r=3f;q.critical=9f;g.drops.add(p);g.drops.add(q);
    g.step(.016f);
    check(g.drops.size()==1&&Math.abs(g.drops.get(0).r*g.drops.get(0).r-18f)<.5f,"merge conserves area");
    // droplet count stays under the cap
    CondensationModel c=fresh(1f,0f);
    for(int i=0;i<60*120;i++)c.step(1f/60f);
    check(c.drops.size()<=CondensationModel.dropCap(1f),"cap holds");
    // refog restores the fog
    c.wipeStroke(300f,180f,30f);c.endWipe();c.refog();
    check(c.fogAt(300f,180f)>.55f,"refog restores fog");
    // the grid survives small size jitter without restarting
    CondensationModel j=fresh(.5f,.3f);int before=j.drops.size();j.drops.add(new CondensationModel.Drop());
    check(!j.ensure(601f,361f)&&j.drops.size()==before+1,"small size changes keep the simulation");
    check(j.ensure(300f,200f),"big size changes restart it");
    System.out.println("CondensationModelTest passed");
  }
}
