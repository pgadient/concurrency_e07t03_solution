package sse;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

public class SnowflakeRenderer {

	private JFrame frame;
	private int levelOfDetail = 3;
	private int snowflakeSize = 30;
	private float gravity = 9.81f;
	private int frameHeight = 500;
	private int frameWidth = 500;
	private int snowflakeIncreaseRate = 30;
	private Semaphore semaphore = new Semaphore(0);
	private int addSnowflakesEachNthRound = 10;
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	
	public static void main(String[] args) {
		SnowflakeRenderer instance = new SnowflakeRenderer();
		instance.letItSnow();
	}
	
	public SnowflakeRenderer() {
		this.frame = new JFrame();
		this.frame.setBackground(new Color(255, 255, 255));
		this.frame.setSize(this.frameWidth, this.frameHeight);
		this.frame.setVisible(true);
		this.frame.setIgnoreRepaint(true);
		this.frame.addWindowListener( new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent we) {
                 System.exit(0);
             }
         } );
	}
	
	private void letItSnow() {
		Graphics2D g = (Graphics2D) this.frame.getGraphics();
		int currentBatch = 0;
		int currentSnowflakes = 0;

		while (true) {
			System.out.println("Active Threads: " + currentSnowflakes);
			if ((currentBatch % this.addSnowflakesEachNthRound) == 0) {
				createNewSnowflakes(g);
				currentSnowflakes = currentSnowflakes + this.snowflakeIncreaseRate;
				
				if (currentBatch > 0) {
					currentBatch = 0;
				}
			}
			
			ArrayList<Thread> elementsToRemove = new ArrayList<Thread>();
			for (Thread t : this.threads) {
				if (!t.isAlive()) {
					currentSnowflakes--;
					elementsToRemove.add(t);
				}
			}
			this.threads.removeAll(elementsToRemove);
		
			currentBatch++;
			
			this.frame.paintComponents(g);
			this.semaphore.release(currentSnowflakes);
		
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createNewSnowflakes(Graphics2D g) {
		for (int i = 0; i < this.snowflakeIncreaseRate; i++) {
			Random r = new Random();
			int xPosSnowflake = (int) (r.nextDouble() * this.frameWidth); 
			Snowflake s = new Snowflake(this.levelOfDetail, this.snowflakeSize, xPosSnowflake, this.gravity, g, this.frameHeight, this.semaphore);
			Thread t = new Thread(s);
			t.start();
			this.threads.add(t);
		}
	}
	

}
