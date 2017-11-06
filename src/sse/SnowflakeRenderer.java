package sse;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Snowflake Simulation Environment (SSE)
 * November 2017
 * 
 * Solution
 * 
 * @author Pascal Gadient (gadient@inf.unibe.ch) 
 * 
 * SCG University of Bern, Concurrency Course
 * 
 */
public class SnowflakeRenderer {

	private JFrame frame;
	private int levelOfDetail = 2;									// how many fractal iterations we should perform (greatly impacts performance!)
	private int snowflakeSize = 30;									// how large the snowflakes should be
	private float gravity = 9.81f;									// how fast the snowflakes should fall to the bottom
	private int frameHeight = 500;									// window height
	private int frameWidth = 500;									// window width
	private int snowflakeIncreaseRate = 2;							// how many snowflakes will be added each addSnowflakesEachNthRound
	private Semaphore semaphore = new Semaphore(0);					// ensures synchronisation that every thread starts painting within the same time period
	private Semaphore threadsFinished = new Semaphore(0);			// ensures synchronisation that every thread finishes painting within the same time period
	private int addSnowflakesEachNthRound = 10;						// adds [snowflakeIncreaseRate] snowflakes after each ...th  snowflake draw call session (that should be synchronized by you :)
	private ArrayList<Thread> threads = new ArrayList<Thread>();	// maintains list of active snowflake threads (used for semaphores)
	private int waitBeforeRefreshDuration = 50;						// duration in ms how long we will wait before repainting the whole (updated) state again
	private boolean lazyCanvasUpdate = false;						// speeds up the painting when enabled, but increases flickering
	
	public static void main(String[] args) {
		SnowflakeRenderer instance = new SnowflakeRenderer();
		instance.letItSnow();
	}
	
	public SnowflakeRenderer() {
		this.frame = new JFrame();
		this.frame.setTitle("SCG - Snowflake Simulation Environment (SSE) - Solution");
		this.frame.setBackground(new Color(255, 255, 255));
		this.frame.setSize(this.frameWidth, this.frameHeight);
		this.frame.setVisible(true);
		this.frame.setIgnoreRepaint(true);
		this.frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent we) {
                 System.exit(0);
             }
         } );
		
		if (this.lazyCanvasUpdate) {
			this.threadsFinished = null;
		}
		
		URL iconURL = getClass().getResource("/sse/scg-logo.png");
		ImageIcon icon = new ImageIcon(iconURL);
		this.frame.setIconImage(icon.getImage());
	}
	
	/**
	 * As it says: Let it snow! :)
	 */
	private void letItSnow() {
		Graphics2D g = (Graphics2D) this.frame.getGraphics();
		int currentBatch = 0;
		int currentSnowflakes = 0;
		
		// set draw color to blue
		g.setColor(new Color(0, 0, 255));
		
		while (true) {
			System.out.println("Active Threads: " + currentSnowflakes);
			
			// introduces new snowflakes when we finally reached the nth run of draw calls
			if ((currentBatch % this.addSnowflakesEachNthRound) == 0) {
				createNewSnowflakes(g);
				currentSnowflakes = currentSnowflakes + this.snowflakeIncreaseRate;
				
				if (currentBatch > 0) {
					currentBatch = 0;
				}
			}
			
			// clean-up old snowflakes that are out of view (threads in died state)
			ArrayList<Thread> elementsToRemove = new ArrayList<Thread>();
			for (Thread t : this.threads) {
				if (!t.isAlive()) {
					currentSnowflakes--;
					elementsToRemove.add(t);
				}
			}
			this.threads.removeAll(elementsToRemove);
		
			currentBatch++;
			
			// clean canvas
			this.frame.paintComponents(g);
			
			// let the snowflakes draw themselves on the new canvas
			this.semaphore.release(currentSnowflakes);
			
			// optionally: wait until they finished their drawing
			if (this.threadsFinished != null) {
				try {
					this.threadsFinished.acquire(currentSnowflakes);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			// sleep some time before we start the next cycle
			try {
				Thread.sleep(this.waitBeforeRefreshDuration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Here we instantiate the snowflakes.
	 * @param g is the Graphics2D object to paint on
	 */
	private void createNewSnowflakes(Graphics2D g) {
		for (int i = 0; i < this.snowflakeIncreaseRate; i++) {
			Random r = new Random();
			int xPosSnowflake = (int) (r.nextDouble() * this.frameWidth); 
			Snowflake s = new Snowflake(this.levelOfDetail, this.snowflakeSize, xPosSnowflake, this.gravity, g, this.frameHeight, this.semaphore, this.threadsFinished);
			Thread t = new Thread(s);
			t.start();
			this.threads.add(t);
		}
	}
	

}
