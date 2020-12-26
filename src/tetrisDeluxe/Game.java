package tetrisDeluxe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game extends JPanel implements Runnable
{
	private static final long serialVersionUID = 1L;
	private int speed,boxSize,windowHeight,windowWidth,templateSize,currentScore,bestScore;
	private int randomShapeNumber,nextShapeNumber,heldShapeNumber,verticalCursor,horizontalCursor;
	private int startX,nextStartY,heldStartY;
	private boolean dialogueBoxShown,canRun,hasHeld,isReadyToBePlayed;
	private boolean[][] blocked;
	private Color[][] board;
	private Thread runner;
	private Shape S;
	private Graphics2D G;
	private File scoreSheet,lineMusic,gameoverMusic,fallMusic,successMusic;
	private Clip myClip;
	private AudioInputStream myAudioInputStream;
	
	private Game()
	{
		super();
		dialogueBoxShown = false;
		canRun = true;
		hasHeld = false;
		isReadyToBePlayed = false;
		scoreSheet = new File("HighScore.dat");
		lineMusic = new File("src/Resources/line.wav");
		gameoverMusic = new File("src/Resources/gameover.wav");
		fallMusic = new File("src/Resources/fall.wav");
		successMusic = new File("src/Resources/success.wav");
		boxSize = 36;
		templateSize = 4;
		startX = 13;
		nextStartY = 3;
		heldStartY = 14;
		windowHeight = 20;
		windowWidth = 10;
		verticalCursor = 4;
		horizontalCursor = 0;
		currentScore = 0;
		bestScore = 0;
		speed = 500;
		nextShapeNumber = -1;
		heldShapeNumber = -1;
		board = new Color[windowHeight][windowWidth];
		blocked = new boolean[windowHeight][windowWidth];
		setPreferredSize(new Dimension(2*boxSize*windowWidth,boxSize*windowHeight));
		runner = new Thread(this);
		runner.start();
		S = new Shape();
		generateRandomNumber();
		eventHandler();
		initScoreSheet();
		playSound(lineMusic);
		for(int i=0;i<windowHeight;i++)
		{
			for(int j=0;j<windowWidth;j++)
			{
				board[i][j] = Color.BLACK;
				blocked[i][j] = false;
			}
		}
	}
	
	private void initScoreSheet()
	{
		if(!scoreSheet.exists())
		{
			try 
			{
				scoreSheet.createNewFile();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			this.setHighScore();
		}
		else
		{
			String temp = new String();
			temp = this.getHighScore();
			bestScore = Integer.parseInt(temp);
		}
	}
	
	private void setHighScore()
	{
		try 
		{
			FileWriter WRITER = new FileWriter(scoreSheet);
			BufferedWriter BWRITER = new BufferedWriter(WRITER);
			BWRITER.write(""+currentScore);
			BWRITER.close();
			WRITER.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private String getHighScore()
	{
		String STR = "";
		try
		{
			FileReader READER = new FileReader(scoreSheet);
			BufferedReader BREADER = new BufferedReader(READER);
			STR = new String(BREADER.readLine());
			BREADER.close();
			READER.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return STR;
	}
	
	private void playSound(File F)
	{
		try
		{
			myAudioInputStream = AudioSystem.getAudioInputStream(F);
			myClip = AudioSystem.getClip();
			myClip.open(myAudioInputStream);
		}
		catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
		{
			e.printStackTrace();
		}
		if(isReadyToBePlayed)
		{
			myClip.start();
		}
		else
		{
			isReadyToBePlayed = true;
		}
	}
	
	private void updateWindow()
	{
		revalidate();
		repaint();
	}	
	private void generateRandomNumber()
	{
		if(nextShapeNumber == -1)
		{
			nextShapeNumber = (int)(Math.random()*S.shapes.length);
		}
		randomShapeNumber = nextShapeNumber;
		nextShapeNumber = (int)(Math.random()*S.shapes.length);
		int w = findLeftOffset();
		int x = findTopOffset();
		int y = findBottomOffset();
		int z = templateSize-x-y;
		for(int i=0;i<z;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					if(blocked[horizontalCursor+i-x][verticalCursor+j-w])
					{
						if(!dialogueBoxShown)
						{
							if(currentScore > bestScore)
							{
								playSound(successMusic);
								setHighScore();
							}
							else
							{
								playSound(gameoverMusic);
							}
							JOptionPane.showMessageDialog(this, "GAME OVER!", "Tetris Deluxe", JOptionPane.PLAIN_MESSAGE);
							dialogueBoxShown = true;
							canRun = false;
						}
					}
				}
			}
		}
	}
	
	private boolean isRotatable()
	{
		int x = findLeftOffset();
		int y = findTopOffset();
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					if(blocked[horizontalCursor+i-y][verticalCursor+j-x])
						return false;
				}
			}
		}
		return true;
	}
	
	private void rotateShape()
	{
		for(int i=0;i<templateSize;i++)
		{
			for(int j=i+1;j<templateSize;j++)
			{
				int temp = S.shapes[randomShapeNumber][i][j];
				S.shapes[randomShapeNumber][i][j] = S.shapes[randomShapeNumber][j][i];
				S.shapes[randomShapeNumber][j][i] = temp;
			}
		}
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize/2;j++)
			{
				int temp = S.shapes[randomShapeNumber][i][j];
				S.shapes[randomShapeNumber][i][j] = S.shapes[randomShapeNumber][i][templateSize-j-1];
				S.shapes[randomShapeNumber][i][templateSize-j-1] = temp;
			}
		}
		int prevVerticalCursor = verticalCursor;
		if(verticalCursor+templateSize-findLeftOffset()-findRightOffset() >= windowWidth)
		{
			verticalCursor = windowWidth-templateSize+findLeftOffset()+findRightOffset();
		}
		if(!isRotatable())
		{
			verticalCursor = prevVerticalCursor;
			for(int i=0;i<templateSize;i++)
			{
				for(int j=0;j<templateSize/2;j++)
				{
					int temp = S.shapes[randomShapeNumber][i][j];
					S.shapes[randomShapeNumber][i][j] = S.shapes[randomShapeNumber][i][templateSize-j-1];
					S.shapes[randomShapeNumber][i][templateSize-j-1] = temp;
				}
			}
			for(int i=0;i<templateSize;i++)
			{
				for(int j=i+1;j<templateSize;j++)
				{
					int temp = S.shapes[randomShapeNumber][i][j];
					S.shapes[randomShapeNumber][i][j] = S.shapes[randomShapeNumber][j][i];
					S.shapes[randomShapeNumber][j][i] = temp;
				}
			}
		}
	}
	
	private void eventHandler()
	{
		setFocusable(true);
		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent keyEvent)
			{
				if(keyEvent.getKeyCode() == KeyEvent.VK_S)
				{
					speed = 50;
				}
				if(keyEvent.getKeyCode() == KeyEvent.VK_A)
				{
					 if(!checkSidewaysCollision(-1))
					 {
						 verticalCursor = Math.max(verticalCursor-1,0);
						 updateWindow();
					 }
				}
				if(keyEvent.getKeyCode() == KeyEvent.VK_D)
				{
					if(!checkSidewaysCollision(1))
					{
						verticalCursor = Math.min(verticalCursor+1,windowWidth-(4-findLeftOffset()-findRightOffset()));
						updateWindow();
					}
				}
				if(keyEvent.getKeyCode() == KeyEvent.VK_H)
				{
					if(!hasHeld)
					{
						hasHeld = true;
						if(heldShapeNumber == -1)
						{
							heldShapeNumber = randomShapeNumber;
							generateRandomNumber();
						}
						else
						{
							int temp = heldShapeNumber;
							heldShapeNumber = randomShapeNumber;
							randomShapeNumber = temp;
						}
						horizontalCursor = 0;
						verticalCursor = 4;
						updateWindow();
					}
				}
			}
			
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				if(keyEvent.getKeyCode() == KeyEvent.VK_S)
				{
					speed = 500;
				}
				if(keyEvent.getKeyCode() == KeyEvent.VK_SPACE)
				{
					 rotateShape();
					 updateWindow();
				}
			}
		});
	}
	
	private int findLeftOffset()
	{
		int offset = 0;
		for(int j=0;j<templateSize;j++)
		{
			boolean ok = false;
			for(int i=0;i<templateSize;i++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					ok = true;
				}
			}
			if(ok)
				break;
			else
				offset += 1;
		}
		return offset;
	}
	
	private int findTopOffset()
	{
		int offset = 0;
		for(int i=0;i<templateSize;i++)
		{
			boolean ok = false;
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					ok = true;
				}
			}
			if(ok)
				break;
			else
				offset += 1;
		}
		return offset;
	}
	
	private int findRightOffset()
	{
		int offset = 0;
		for(int j=templateSize-1;j>=0;j--)
		{
			boolean ok = false;
			for(int i=0;i<templateSize;i++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					ok = true;
				}
			}
			if(ok)
				break;
			else
				offset += 1;
		}
		return offset;
	}
	
	private int findBottomOffset()
	{
		int offset = 0;
		for(int i=templateSize-1;i>=0;i--)
		{
			boolean ok = false;
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					ok = true;
				}
			}
			if(ok)
				break;
			else
				offset += 1;
		}
		return offset;
	}
	
	private void drawShape()
	{
		G.setColor(S.colors[randomShapeNumber]);
		int x = findLeftOffset();
		int y = findTopOffset();
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				G.fillRect((verticalCursor+j-x)*boxSize, (horizontalCursor+i-y)*boxSize, boxSize, boxSize);
			}
		}
	}
	
	private void drawNextShape()
	{
		Shape S1 = new Shape();
		G.setColor(S1.colors[nextShapeNumber]);
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S1.shapes[nextShapeNumber][i][j] != 0)
				G.fillRect((13+j)*boxSize, (3+i)*boxSize, boxSize, boxSize);
			}
		}
	}
	
	private void drawHeldShape()
	{
		Shape S2 = new Shape();
		G.setColor(S2.colors[heldShapeNumber]);
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S2.shapes[heldShapeNumber][i][j] != 0)
				G.fillRect((13+j)*boxSize, (14+i)*boxSize, boxSize, boxSize);
			}
		}
	}
	
	private void rayTrace()
	{
		int x = findLeftOffset();
		int y = findTopOffset();
		int z = findBottomOffset();
		int level = windowHeight-horizontalCursor-templateSize+y+z;
		for(int i=0;i<templateSize;i++)
		{	
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0 && (i == templateSize-1 || S.shapes[randomShapeNumber][i+1][j] == 0))
				{
					for(int k=horizontalCursor+i-y+1;k<windowHeight;k++)
					{
						if(blocked[k][verticalCursor+j-x])
						{
							level = Math.min(level,k-horizontalCursor-i+y-1);
							break;
						}
					}
				}
			}
		}
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					G.setColor(S.colors[randomShapeNumber]);
					BasicStroke bs = new BasicStroke(2);
					G.setStroke(bs);
					G.drawRect((verticalCursor+j-x)*boxSize, (level+horizontalCursor+i-y)*boxSize, boxSize, boxSize);
				}
			}
		}
	}
	
	private void updateBoard()
	{
		int x = findLeftOffset();
		int y = findTopOffset();
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					board[horizontalCursor+i-y][verticalCursor+j-x] = S.colors[randomShapeNumber];
					blocked[horizontalCursor+i-y][verticalCursor+j-x] = true;
				}
			}
		}
	}
	
	private void sleepCurrentThread(int milliseconds)
	{
		try 
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void animateLine(int i)
	{
		Color[] rowToBeCleared = new Color[windowWidth];
		for(int j=0;j<windowWidth;j++)
		{
			rowToBeCleared[j] = board[i][j];
			board[i][j] = Color.BLACK;
		}
		updateWindow();
		sleepCurrentThread(100);
		for(int j=0;j<windowWidth;j++)
		{
			board[i][j] = rowToBeCleared[j];
		}
		updateWindow();
		sleepCurrentThread(100);
	}
	
	private void checkLine()
	{
		for(int i=0;i<windowHeight;i++)
		{
			boolean ok = true;
			for(int j=0;j<windowWidth;j++)
			{
				if(!blocked[i][j])
					ok = false;
			}
			if(ok)
			{
				playSound(lineMusic);
				currentScore += 1;
				animateLine(i);
				for(int k=i;k>0;k--)
				{
					for(int j=0;j<windowWidth;j++)
					{
						board[k][j] = board[k-1][j];
						blocked[k][j] = blocked[k-1][j];
					}
				}
			}
			for(int j=0;j<windowWidth;j++)
			{
				board[0][j] = Color.BLACK;
				blocked[0][j] = false;
			}
		}
	}
	
	private boolean checkCollision()
	{
		int x = findLeftOffset();
		int y = findTopOffset();
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					if(horizontalCursor+i-y+1 < windowHeight && blocked[horizontalCursor+i-y+1][verticalCursor+j-x])
						return true;
				}
			}
		}
		return false;
	}
	
	private boolean checkSidewaysCollision(int dir)
	{
		int x = findLeftOffset();
		int y = findTopOffset();
		for(int i=0;i<templateSize;i++)
		{
			for(int j=0;j<templateSize;j++)
			{
				if(S.shapes[randomShapeNumber][i][j] != 0)
				{
					if(verticalCursor+j-x+dir < windowWidth && verticalCursor+j-x+dir >= 0 && blocked[horizontalCursor+i-y][verticalCursor+j-x+dir])
						return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		setBackground(Color.BLACK);
		G = (Graphics2D)g;
		G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G.setColor(Color.WHITE);
		Font font = new Font("Times New Roman",Font.BOLD,30);
		G.setFont(font);
		G.drawString("NEXT", startX*boxSize, nextStartY*boxSize-boxSize/2);
		G.drawString("HELD", startX*boxSize, heldStartY*boxSize-boxSize/2);
		G.drawString("SCORE: "+currentScore, startX*boxSize,(nextStartY+templateSize+heldStartY-1)*boxSize/2);
		G.drawString("BEST: "+getHighScore(),startX*boxSize,(nextStartY+templateSize+heldStartY+1)*boxSize/2);
		for(int i=0;i<windowHeight;i++)
		{
			for(int j=0;j<windowWidth;j++)
			{
				G.setColor(board[i][j]);
				G.fillRect(j*boxSize, i*boxSize, boxSize, boxSize);
			}
		}
		drawShape();
		G.setColor(new Color(40,40,40));
		for(int i=0;i<=windowWidth;i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(i*boxSize, 0, i*boxSize, windowHeight*boxSize);
		}
		for(int i=0;i<=windowHeight;i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(0, i*boxSize, windowWidth*boxSize, i*boxSize);
		}
		G.setColor(Color.WHITE);
		G.drawLine(0,0,0,windowHeight*boxSize);
		G.drawLine(windowWidth*boxSize,0,windowWidth*boxSize,windowHeight*boxSize);
		G.drawLine(0,0,windowWidth*boxSize,0);
		G.drawLine(0,windowHeight*boxSize,windowWidth*boxSize,windowHeight*boxSize);
		rayTrace();
		drawNextShape();
		G.setColor(new Color(40,40,40));
		for(int i=startX+1;i<(startX+templateSize);i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(i*boxSize, nextStartY*boxSize, i*boxSize, (nextStartY+templateSize)*boxSize);
		}
		for(int i=nextStartY+1;i<(nextStartY+templateSize);i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(startX*boxSize, i*boxSize, (startX+templateSize)*boxSize, i*boxSize);
		}
		G.setColor(Color.WHITE);
		G.drawRect(startX*boxSize, nextStartY*boxSize, templateSize*boxSize, templateSize*boxSize);
		if(heldShapeNumber != -1)
		{
			drawHeldShape();
		}
		G.setColor(new Color(40,40,40));
		for(int i=startX+1;i<(startX+templateSize);i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(i*boxSize, heldStartY*boxSize, i*boxSize, (heldStartY+templateSize)*boxSize);
		}
		for(int i=heldStartY+1;i<(heldStartY+templateSize);i++)
		{
			BasicStroke bs = new BasicStroke(2);
			G.setStroke(bs);
			G.drawLine(startX*boxSize, i*boxSize, (startX+templateSize)*boxSize, i*boxSize);
		}
		G.setColor(Color.WHITE);
		G.drawRect(startX*boxSize, heldStartY*boxSize, templateSize*boxSize, templateSize*boxSize);
		G.dispose();
	}
	
	@Override
	public void run() 
	{
		while(canRun)
		{
			updateWindow();
			sleepCurrentThread(speed);
			if(checkCollision() || horizontalCursor == windowHeight-(4-findTopOffset()-findBottomOffset()))
			{
				playSound(fallMusic);
				updateBoard();
				horizontalCursor = 0;
				verticalCursor = 4;
				hasHeld = false;
				generateRandomNumber();
				checkLine();
			}
			else
			{
				horizontalCursor += 1;
			}
		}
	}
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JFrame F = new JFrame("Tetris Deluxe");
				Game game = new Game();
				F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				F.add(game);
				F.pack();
				F.setLocationRelativeTo(null);
				F.setVisible(true);
			}
		});
	}
}
