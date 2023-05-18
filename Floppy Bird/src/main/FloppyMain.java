package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
public class FloppyMain implements ActionListener, MouseListener, KeyListener{

	public static FloppyMain bird;
	public Renderer renderer;
	public Rectangle birdHitbox;
	public ArrayList<Rectangle> pipes;
	public Random rand;
	
	private final String BACKGROUND = "FloppyBirdBGR.png";
	private BufferedImage bgrImg = GetSpriteAtlas(BACKGROUND);
	private final String FLOPPYBIRD = "FloppyBird.png";
	private BufferedImage birdImg = GetSpriteAtlas(FLOPPYBIRD);
	private final String PIPE = "FloppyPipe.png";
	private BufferedImage pipeImg = GetSpriteAtlas(PIPE);
//	NOTE: Keep at or above 2
	public final int SCALE = 3;
	public final int WIDTH = (int) (300 * SCALE), HEIGHT = (int) (168 * SCALE);
	public int ticks, yMotion, highScore;
	public float score;
	public boolean gameOver, started;
	
	public FloppyMain() {
		JFrame jframe = new JFrame();
		Timer timer = new Timer(20, this);
		
		renderer = new Renderer();
		rand = new Random();
		
		jframe.add(renderer);
		jframe.setTitle("Floppy Bird");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(WIDTH + 16, HEIGHT + 39);
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setLocation(400, 100);
		jframe.setVisible(true);
		
		birdHitbox = new Rectangle((int) (WIDTH / 5), (int) (HEIGHT / 2), (int) (17 * SCALE), (int) (12 * SCALE));
		pipes = new ArrayList<>();
		
		addPipes(true);
		addPipes(true);
		addPipes(true);
		addPipes(true);
		
		timer.start();
	}
	public static void main (String[] args) {
		bird = new FloppyMain();
	}
	
	public void repaint(Graphics g) {
		
		g.setColor(Color.green);
		g.drawImage(bgrImg, 0, 0, WIDTH, HEIGHT, null);
		g.drawImage(birdImg, birdHitbox.x, birdHitbox.y, birdHitbox.width, birdHitbox.height, null);
//	To Debug the hitbox
		g.drawRect(birdHitbox.x, birdHitbox.y, birdHitbox.width, birdHitbox.height);
		for (Rectangle p : pipes) {
			drawPipes(g, p, pipeImg);

		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Arial", 1, (int) (33 * SCALE)));
		if (!started) {
			g.drawString("Click to start!", (int) (50 * SCALE), (int) (HEIGHT / 2 - (15 * SCALE)));
		} else if (gameOver) {
			g.drawString("Game Over", (int) (58 * SCALE), (int) (HEIGHT / 2));
		}
		g.drawString(String.valueOf((int) (score)), (int) (WIDTH / 2 - (8.34 * SCALE)), (int) (33.34 * SCALE));
		g.drawString(String.valueOf(highScore), (int) (8.34 * SCALE), (int) (33.34 * SCALE));
	}
	
	public void addPipes(boolean start) {
		int space = (int) (100 * SCALE);
		int width = (int) (26 * SCALE);
		int height = (int) (250 * SCALE);
		int botPipeY = (int) (50 * SCALE) + height;
		int y = (int) ((rand.nextInt(98) + 10) * SCALE);
		
		if (start) {
//			pipes.add(new Rectangle ((WIDTH + width + pipes.size() * 300), HEIGHT - y - 120, width, height));
//			pipes.add(new Rectangle ((WIDTH + width + (pipes.size() - 1) * 300), 0, width, height));
			pipes.add(new Rectangle ((WIDTH + pipes.size() * space), -height + y, width, height));
			pipes.add(new Rectangle ((WIDTH + (pipes.size() - 1) * space), pipes.get(pipes.size() - 1).y + botPipeY, width, height));
		} else {
			pipes.add(new Rectangle ((pipes.get(pipes.size() - 1).x + space), -height + y, width, height));
			pipes.add(new Rectangle ((pipes.get(pipes.size() - 1).x),  pipes.get(pipes.size() - 1).y + botPipeY, width, height));
		}
		
	}
	
	public void drawPipes(Graphics g, Rectangle pipe, BufferedImage img) {
		g.drawImage(img, pipe.x, pipe.y, pipe.width, pipe.height, null);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		ticks++;
		int speed = 0;
		if (!gameOver) {
			speed = (int) (4 * SCALE);
		}
		
		if (started) {
			for (int i = 0; i < pipes.size(); i++) {
				Rectangle pipe = pipes.get(i);
				pipe.x -= speed;
			}
			
			if ((ticks % 2 == 0) && yMotion < 11) {
				yMotion += (int) (1 * SCALE);
			}
			
			for (int i = 0; i < pipes.size(); i++) {
				Rectangle pipe = pipes.get(i);
				
				if ((pipe.x == 0) && (i % 2 == 0)) {
					addPipes(false);
				}
				
				if (pipe.x + pipe.width < 0) {
					pipes.remove(pipe);
				}
			}
			
			birdHitbox.y += yMotion;
			
			for (Rectangle pipe : pipes) {
//				(pipe.y == 0) && 
				
				
				if (pipe.intersects(birdHitbox)) {
					gameOver = true;
					
					if (birdHitbox.x <= pipe.x) {
						birdHitbox.x = pipe.x - birdHitbox.width;
		
					} else {
						if (pipe.y <= 0) {
							birdHitbox.y = pipe.y - birdHitbox.height;
						} else if (birdHitbox.y < pipe.y) {
							birdHitbox.y = pipe.y + pipe.height;
						}
					}
				}
				
				if (!gameOver && (birdHitbox.x + (birdHitbox.width / 2)) > (pipe.x + (pipe.width / 2) - 10) && (birdHitbox.x + (birdHitbox.width / 2)) < (pipe.x + (pipe.width / 2) + 10)) {
					score += .08334 * SCALE;
				}
			}
			
			if (birdHitbox.y > HEIGHT - birdHitbox.height) {
				birdHitbox.y = HEIGHT - birdHitbox.height;
				gameOver = true;
			}
			if ((birdHitbox.y + yMotion >= HEIGHT)) {
				birdHitbox.y = HEIGHT - birdHitbox.height;
			} else if (birdHitbox.y < 0) {
				birdHitbox.y = 0;
			}
			if (gameOver && score > highScore) {
				highScore = (int) score;
			}
		}
		renderer.repaint();
	}
	
	public BufferedImage GetSpriteAtlas(String fileName) {
		
		BufferedImage img = null;
		InputStream is = FloppyMain.class.getResourceAsStream("/" + fileName);
    	
		try {
			img = ImageIO.read(is);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return img;
	}
	
	public void jump() {
		if (gameOver) {
			birdHitbox = new Rectangle((int) (WIDTH / 5), (int) (HEIGHT / 2), (int) (17 * SCALE), (int) (12 * SCALE));
			pipes.clear();
			yMotion = 0;
			score = 0;
			
			addPipes(true);
			addPipes(true);
			addPipes(true);
			addPipes(true);
			
			gameOver = false;
		}
		if (!started) {
			started = true;
		} else if (!gameOver){
			if (yMotion > 0) {
				yMotion = 0;
			}
			yMotion -= (int) (4 * SCALE);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		jump();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			jump();
		}
	}
}
