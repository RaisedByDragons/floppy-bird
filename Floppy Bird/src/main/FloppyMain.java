package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
public class FloppyMain implements ActionListener, MouseListener, KeyListener{

	public static FloppyMain bird;
	private Renderer renderer;
	private Rectangle birdHitbox;
	private ArrayList<Rectangle> pipes;
	private Random rand;
	
	private final String BACKGROUND = "FloppyBirdBGR.png";
	private BufferedImage bgrImg = GetSpriteAtlas(BACKGROUND);
	private final String FLOPPYBIRD = "FloppyBird.png";
	private BufferedImage birdImg = GetSpriteAtlas(FLOPPYBIRD);
	private final String PIPE = "FloppyPipe.png";
	private BufferedImage pipeImg = GetSpriteAtlas(PIPE);
	private final int ANISPEED = 10;
	private BufferedImage[] birdAni;
//	NOTE: Keep at or above 2
	private final int SCALE = 1;
	private final int WIDTH = (int) (300 * SCALE), HEIGHT = (int) (168 * SCALE);
	private int ticks, yMotion, highScore;
	private float score;
	private boolean gameOver, started;
	private boolean givePoint = false;
	int ani, aniIndex = 0;
	
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
		animations();
	    loadHighscore();
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
		ani++;
    	if (ani >= ANISPEED) {
    		ani = 0;
    		aniIndex++;
    		if (aniIndex >= birdAni.length) {
    			aniIndex = 0;
    		}
    	}
		g.setColor(Color.green);
		g.drawImage(bgrImg, 0, 0, WIDTH, HEIGHT, null);
		if (!gameOver) {
			BufferedImage sprite = birdAni[aniIndex];
			g.drawImage(sprite, birdHitbox.x, birdHitbox.y, birdHitbox.width, birdHitbox.height, null);
		} else {
			birdHitbox.width = 12 * SCALE;
			birdHitbox.height = 17 * SCALE;
			g.drawImage(birdImg.getSubimage(0, 12, 12, 17), birdHitbox.x, birdHitbox.y, birdHitbox.width, birdHitbox.height, null);

		}
//	To Debug the hitbox
//		g.drawRect(birdHitbox.x, birdHitbox.y, birdHitbox.width, birdHitbox.height);
		for (Rectangle p : pipes) {
			drawPipes(g, p, pipeImg);

		}
		Color color = new Color(235,235,235);
		g.setColor(color);
		Font useFont = new Font("Arial", 1, (int) (33 * SCALE));
		g.setFont(useFont);
		
		Graphics2D g2d = (Graphics2D) g.create();
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(useFont);
        
		if (!started) {
			centerText("Press any key", (int) ((HEIGHT / 2) - (15 * SCALE)), useFont, g2d);
			centerText("to start!", (int) ((HEIGHT / 2) + (15 * SCALE)), useFont, g2d);
		} else if (gameOver) {
			centerText("Game Over!", (int) ((HEIGHT / 2) - (10 * SCALE)), useFont, g2d);
		}
//		g.drawString(String.valueOf((int) (score)), (int) (WIDTH / 2 - (8.34 * SCALE)), (int) (33.34 * SCALE));
//		g.drawString(String.valueOf(highScore), (int) (8.34 * SCALE), (int) (33.34 * SCALE));
		drawTextWithBorder(g2d, String.valueOf((int) (score)), (int) (WIDTH / 2 - (8.34 * SCALE)), (int) (33.34 * SCALE), useFont, Color.WHITE, Color.BLACK, 2);
		drawTextWithBorder(g2d, String.valueOf((int) (highScore)), (int) (8.34 * SCALE), (int) (33.34 * SCALE), useFont, Color.WHITE, Color.BLACK, 2);
        g2d.dispose();
	}
//	Made with chatGPT;
	public void centerText(String text, int y, Font font, Graphics2D g2d) {
		// Get font metrics to calculate text position
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);

        // Calculate the position to center the text
        int x = (WIDTH - textWidth) / 2;

        // Draw the centered text
//        g.drawString(text, x, y);
        
        drawTextWithBorder(g2d, text, x, y, font, Color.WHITE, Color.BLACK, 2);

	}
//	Made with chatGPT;
	private void drawTextWithBorder(Graphics2D g2d, String text, int x, int y, Font font, Color textColor, Color borderColor, int borderThickness) {
        // Get font metrics
        FontMetrics metrics = g2d.getFontMetrics(font);

        // Calculate text position
//        int x = (WIDTH - metrics.stringWidth(text)) / 2;
//        int y = (HEIGHT + metrics.getAscent()) / 2;

        // Draw black border
        g2d.setColor(borderColor);
        for (int dx = -borderThickness; dx <= borderThickness; dx++) {
            for (int dy = -borderThickness; dy <= borderThickness; dy++) {
                g2d.drawString(text, x + dx, y + dy);
            }
        }

        // Draw white text
        g2d.setColor(textColor);
        g2d.drawString(text, x, y);
    }
	
	public void addPipes(boolean start) {
		int space = (int) (150 * SCALE);
		int width = (int) (26 * SCALE);
		int height = (int) (250 * SCALE);
		int botPipeY = (int) (50 * SCALE) + height;
		int y = (int) ((rand.nextInt(98) + 10) * SCALE);

		if (start) {
//			pipes.add(new Rectangle ((WIDTH + width + pipes.size() * 300), HEIGHT - y - 120, width, height));
//			pipes.add(new Rectangle ((WIDTH + width + (pipes.size() - 1) * 300), 0, width, height));
			pipes.add(new Rectangle ((WIDTH + pipes.size() * (100 * SCALE)), -height + y, width, height));
			pipes.add(new Rectangle ((WIDTH + (pipes.size() - 1) * (100 * SCALE)), pipes.get(pipes.size() - 1).y + botPipeY, width, height));
		} else {
//			y = calculatePipeHeight(y);
			pipes.add(new Rectangle ((pipes.get(pipes.size() - 1).x + space), -height + y, width, height));
			pipes.add(new Rectangle ((pipes.get(pipes.size() - 1).x),  pipes.get(pipes.size() - 1).y + botPipeY, width, height));
		}
		
	}
	
	public void animations() {
		birdAni = new BufferedImage[3];
		for (int i = 0; i < 3; i++) {
				birdAni[i] = birdImg.getSubimage((i*17), 0, 17, 12);
		}
	}
	
//	public int calculatePipeHeight(int previousPipeHeight) {
//	    // Calculate the minimum and maximum allowed height difference
//	    int maxHeightDifference = (50 * SCALE);
//	    int minHeight = previousPipeHeight - maxHeightDifference;
//	    int maxHeight = previousPipeHeight + maxHeightDifference;
//
//	    // Generate a random height within the allowed range
//	    int pipeHeight = (int) (((Math.random() * (maxHeight - minHeight + 1)) + minHeight) * SCALE);
//
//	    if ((pipeHeight <= (20 * SCALE))) {
//	    	pipeHeight = (20 * SCALE);
//	    } else if ((pipeHeight >= (HEIGHT - (70 * SCALE)))) {
//	    	pipeHeight = (HEIGHT - (70 * SCALE));
//	    }
//	    return pipeHeight;
//	}
	
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
				yMotion += (1 * SCALE);
			}
			
			for (int i = 0; i < pipes.size(); i++) {
				Rectangle pipe = pipes.get(i);
				
				if (((pipe.x + pipe.width) <= 0) && (i % 2 == 0)) {
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
					
					if (birdHitbox.x + birdHitbox.width <= pipe.x + 3) {
						birdHitbox.x = pipe.x - birdHitbox.width;
		
					} else {
						if ((pipe.y <= 0)) {
							birdHitbox.y = pipe.y + pipe.height;
						} else if (birdHitbox.y < pipe.y) {
							birdHitbox.y = pipe.y - birdHitbox.height;
						}
					}
				}
				if (!gameOver && (birdHitbox.x + (birdHitbox.width / 2)) > (pipe.x + (pipe.width / 2) - (2 * SCALE)) && (birdHitbox.x + (birdHitbox.width / 2)) < (pipe.x + (pipe.width / 2) + (2 * SCALE))) {
//					score += .08334 * SCALE;
					if (givePoint) {
						score += 1;
					}
					givePoint = !givePoint;
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
		if (gameOver) {
		    saveHighscore();
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
			yMotion -= (4 * SCALE);
		}
	}
	
	private void saveHighscore(){
	    BufferedWriter bw = null;
	    try {
	        bw = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/highScore.txt", false)); //append - set to false
	        bw.write("" + highScore);
	        bw.flush();
	        bw.close();
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(renderer, e.getMessage(), "Error while saving highScore", JOptionPane.ERROR_MESSAGE);
//	        JOptionPane.showMessageDialog(, e.getMessage(), "Error while saving highScore", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
	private void loadHighscore(){
	    BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/highScore.txt"));
            line = br.readLine();
            br.close();
        } catch (IOException e) {
            line = "";
        }

        if(line != ""){
            highScore = Integer.parseInt(line);
//	            lblHighscore.setText("Highscore: " + highScore);
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
//		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			jump();
//		}
	}
}
