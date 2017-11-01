package sse;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.concurrent.Semaphore;

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
public class Snowflake implements Runnable {

	private int levelOfDetail;
	private int snowflakeSize;
	private int xPos;
	private int yPos;
	private float gravity;
	private Graphics2D g;
	private int outOfViewLimit;
	private Semaphore semaphore;
	private Semaphore semaphoreThreadsFinished;
	private float horizontalMovementAmplifier;
		
	public Snowflake(int levelOfDetail, int snowflakeSize, int xPos, float gravity, Graphics2D g, int frameHeight, Semaphore s, Semaphore s2) {
		Random r = new Random(System.nanoTime());
		r.nextFloat(); // better results (shouldn't be)
		this.levelOfDetail = (int) (levelOfDetail * r.nextFloat()) + 2;
		this.snowflakeSize = (int) (snowflakeSize * r.nextFloat() + 20);
		this.xPos = xPos;
		this.yPos = 0;
		this.gravity = gravity * (r.nextFloat() * 2) + 5;
		this.g = g;
		this.outOfViewLimit = frameHeight;
		this.semaphore = s;
		this.semaphoreThreadsFinished = s2;
		this.horizontalMovementAmplifier = r.nextFloat() * 10 + 1;
	}
	
	public void render() {
			int x1 = (int) (this.horizontalMovementAmplifier * Math.sin((this.yPos / this.gravity) / 2)) + this.xPos;
			int x2 = x1 + this.snowflakeSize;
			
			int y1 = this.yPos;
			int y2 = y1;
			
			this.yPos = this.yPos + (int) this.gravity;
			Point2D.Double p3 = this.get3rdStartingPoint(x1, y1, x2, y2);
			
			koch(this.g, x1, y1, x2, y2, this.levelOfDetail);
			koch(this.g, p3.getX(), p3.getY(), x1, y1, this.levelOfDetail);
			koch(this.g, x2, y2, p3.getX(), p3.getY(), this.levelOfDetail);
	}
	
	private void koch(Graphics g, double x1, double y1, double x2, double y2, int level) {
		
		double a1, b1, a2, b2, a3, b3;
		
		if (level > 1) {
			a1 = (2 * x1 + x2) / 3;
			b1 = (2 * y1 + y2) / 3;
			a2 = (x1 + x2) / 2 + (Math.sqrt(3) * (y2 - y1) / 6);
			b2 = (y1 + y2) / 2 + (Math.sqrt(3) * (x1 - x2) / 6);
			a3 = (2 * x2 + x1) / 3;
			b3 = (2 * y2 + y1) / 3;
			
			koch(g, x1, y1, a1, b1, level - 1);
			koch(g, a1, b1, a2, b2, level - 1);
			koch(g, a2, b2, a3, b3, level - 1);
			koch(g, a3, b3, x2, y2, level - 1);
		} else {
			g.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
		}
	}
	
	private Point2D.Double get3rdStartingPoint(double x1, double y1, double x2, double y2) {
		double length = x2 - x1;
		double height = (Math.sqrt(3) / 2) * length;
		
		return new Point2D.Double(x1 + length / 2, y1 + height);
	}

	@Override
	public void run() {
		while (!isOutOfView()) {
			try {
				this.semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.render();
			
			if (this.semaphoreThreadsFinished != null) {
				this.semaphoreThreadsFinished.release();
			}
		}
		
		System.out.println("A Snowflake reaches the end of life.");
	}
	
	private boolean isOutOfView() {
		if (this.yPos > this.outOfViewLimit) {
			return true;
		} else {
			return false;
		}
	}
}
 
 
