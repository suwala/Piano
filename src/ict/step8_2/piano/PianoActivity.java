package ict.step8_2.piano;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class PianoActivity extends Activity implements OnTouchListener{

	static final Integer numWk = 11, numBk=7,numKeys=numWk+numBk;//凄いマニュアル設定です
	public Region[] kb = new Region[numKeys];
	public MediaPlayer[] key = new  MediaPlayer[numKeys];
	public Integer sw,sh;
	public Integer[] activePointers = new Integer[numKeys];
	public Drawable drawble_white,drawable_black,drawble_white_pressd,drawble_black_pressed;
	public Timer timer;
	public Bitmap bitmap_keyboard;
	public ImageView iv;
	public Boolean[] lastPlayingNotes;
	/*
	 * やたらMediaPlayerでエラーがでる
	 * 音量調節が利いてない
	 */
	
    @Override
	protected void onPause() {//Timerの破棄
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
		
		this.timer.cancel();
	}

	@Override
	protected void onResume() {//Timerの処理
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				//各MediaPlayerオブジェクトの再生状態を取得
				Boolean[] playingNotes = new Boolean[numKeys];
				for(int i =0;i<playingNotes.length;i++){
					playingNotes[i]=key[i].isPlaying();
					
					//前回実行時とは再生状態が変わった場合のみ画面書き換えを実行
					if(!Arrays.equals(playingNotes, lastPlayingNotes)){
						bitmap_keyboard = drawKeys();
						
						//UIスレッドでImageViewに画像をセット
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO 自動生成されたメソッド・スタブ
								
								iv.setImageBitmap(bitmap_keyboard);
								
							}
						});
					}
					
					//再生状態を変数に保存
					lastPlayingNotes = playingNotes;
				}
			}
		}, 0, 100);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);
        
        TypedArray notes = getResources().obtainTypedArray(R.array.notes);
        for(int i =0;i<notes.length();i++){
        	int k = notes.getResourceId(i,1);
        	if(k!=-1){
        		key[i]=MediaPlayer.create(this, k);//notes.xmlのリストでリソースの取得
        	}else{
        		key[i]=null;
        	}
        }
        
        Resources res = getResources();
        this.drawble_white = res.getDrawable(R.drawable.white);
        this.drawable_black = res.getDrawable(R.drawable.black);
        this.drawble_white_pressd = res.getDrawable(R.drawable.white_pressed);
        this.drawble_black_pressed = res.getDrawable(R.drawable.black_pressed);
        
        Display disp = ((WindowManager)this.getSystemService(
        		Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.sw = disp.getWidth();
        this.sh = disp.getHeight();
        
        makeRegions();
        for(int i=0;i<numKeys;i++){
        	this.activePointers[i]=-1;
        }
        
        this.iv = (ImageView)findViewById(R.id.imageView1);
        this.iv.setOnTouchListener(this);
    }

    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		
		Integer pointerIndex = event.getActionIndex();
		Float x = event.getX(pointerIndex);
		Float y = event.getY(pointerIndex);
		
		for(int j=0;j<numKeys;j++){
			if(this.kb[j].contains(x.intValue(),y.intValue())){
				switch (event.getActionMasked()){
				//タッチしたとき
				case MotionEvent.ACTION_DOWN://ACTION_POINTER_DOWNが実行される
				case MotionEvent.ACTION_POINTER_DOWN:
					this.playNote(this.key[j]);
					this.activePointers[pointerIndex]=j;
					break;
				//離したとき
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					this.stopNote(key[j]);
					this.activePointers[pointerIndex]=-1;
					break;
					//ドラッグしたとき
				case MotionEvent.ACTION_MOVE:
					if(this.activePointers[pointerIndex]!=j){
						if(this.activePointers[pointerIndex]!=-1){
							this.stopNote(this.key[this.activePointers[pointerIndex]]);
						}
					}
					break;
				
				}
			}			
		}
		return true;
	}
	
	public void makeRegions(){
		
		Integer kw,kh,bkw,bkh;
		
		//画面サイズからキーの大きさを計算する
		kw = (int)(this.sw/this.numWk);
		kh = (int)(this.sh*0.8);
		bkw = (int)(kw*0.6);
		bkh = (int)(this.sh*0.5);
		
		//キーに合わせたPathオブジェクトの作成
		Path[] path = new Path[]{new Path(),new Path(),new Path(),new Path(),};
		
		//右に黒鍵のある白鍵
		path[0].lineTo(0, kh);
		path[0].lineTo(kw, kh);
		path[0].lineTo(kw, bkh);
		path[0].lineTo(kw-(bkw/2), bkh);
		path[0].lineTo(kw-(bkw/2), 0);
		path[0].close();
		
		//左右に黒鍵のある白鍵
		path[1].moveTo(bkw/2, 0);
		path[1].lineTo(bkw/2, bkh);
		path[1].lineTo(0, bkh);
		path[1].lineTo(0, kh);
		path[1].lineTo(kw, kh);
		path[1].lineTo(kw, bkh);
		path[1].lineTo(kw-(bkw/2), bkh);
		path[1].lineTo(kw-(bkw/2), 0);
		path[1].close();
		
		//左に黒鍵のある白鍵
		path[2].moveTo(bkw/2, 0);
		path[2].lineTo(bkw/2, bkh);
		path[2].lineTo(0, bkh);
		path[2].lineTo(0, kh);
		path[2].lineTo(kw, kh);
		path[2].lineTo(kw, 0);
		path[2].close();
		
		//黒鍵
		path[3].addRect(0,0,bkw,bkh,Direction.CCW);
		
		//Pathオブジェクトの情報を利用してRegionオブジェクトを作成し、キーごとに割り当てる
		Region region = new Region(0,0,this.sw,this.sh);
		Integer[] kt = new Integer[] {0,1,2,0,1,1,2,0,1,2,0,3,3,-1,3,3,3,-1,3,3};
		
		for(int i=0;i<numWk;i++){
			
			this.kb[i] = new Region();
			Path pathtmp = new Path();
			pathtmp.addPath(path[kt[i]],i*kw,0);
			this.kb[i].setPath(pathtmp, region);
			
		}
		Integer j = numWk;
		
		for(int i =numWk;i<kt.length;i++){
			if(kt[i]!=-1){
				this.kb[j] = new Region();
				Path pathtmp = new Path();
				pathtmp.addPath(path[kt[i]],(i-numWk+1)*kw-(bkw/2),0);
				this.kb[j].setPath(pathtmp, region);
				j = j+1;
			}
		}
	}
	
	public Bitmap drawKeys(){//画面サイズに合わせた画像の描画
		
		Bitmap bm = Bitmap.createBitmap(sw,sh,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		
		for(int i = 0;i<numWk;i++){
			if(this.key[i].isPlaying()){
				this.drawble_white_pressd.setBounds(this.kb[i].getBounds());
				this.drawble_white_pressd.draw(canvas);
			}else{
				this.drawble_white.setBounds(this.kb[i].getBounds());
				this.drawble_white.draw(canvas);
			}
		}
		
		for(int i =numWk;i<numKeys;i++){
			if(key[i].isPlaying()){
				this.drawble_black_pressed.setBounds(this.kb[i].getBounds());
				this.drawble_black_pressed.draw(canvas);
			}else{
				this.drawable_black.setBounds(this.kb[i].getBounds());
				this.drawable_black.draw(canvas);
			}
		}
		return bm;
	}
	
	private void playNote(MediaPlayer mp){
		mp.seekTo(0);
		mp.start();
	}
    
	private void stopNote(MediaPlayer mp){
		mp.pause();
	}
	
	
}
