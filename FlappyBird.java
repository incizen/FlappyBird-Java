import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;



public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth= 360;
    int boardHeight= 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;


    //For the birdies size 
    int birdieX= boardWidth/8;
    int birdieY= boardHeight/2;
    int birdieWidth= 34;
    int birdieHeight= 44;
    
    class Birdie{
        int x = birdieX;
        int y = birdieY;
        int widht= birdieWidth;
        int height= birdieHeight;
        Image img;

        Birdie(Image img){
            this.img= img;
        }

    }

    //For the obstacles
    int pipeX= boardWidth;
    int pipeY= 0;
    int pipeWidth= 64;
    int pipeHeight= 512;

    class Pipe {
        int x= pipeX;
        int y= pipeY;
        int width= pipeWidth;
        int height= pipeHeight;
        Image img;
        boolean passed= false; //To keep track of the score 

        Pipe(Image img) { //The constructor
            this.img= img;
        }
    }


//Game physics and logic
Birdie birdie;
int velocityX= -4; //To move the pipes to the left 
int velocityY = 0; //To move the birdie (i took the upper left corner of the JFrame as point zero coordinate); -y is up, +y is down
int gravity = 1;
Timer gameLoopTimer;
Timer PlaceObstacleTimer;

ArrayList<Pipe> pipes; //Because we have many obstacles we need to store them somwhere
Random random= new Random();
boolean gameOver= false;
double score= 0;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLUE); //For background color
        setFocusable(true); //To take the key events
        addKeyListener(this); //To check the three functions(Key typed, press, release)

        //For loading images and sound
        backgroundImg= new ImageIcon(getClass().getResource("./Background.png")).getImage();
        birdImg= new ImageIcon(getClass().getResource("./Bird.png")).getImage();
        topPipeImg= new ImageIcon(getClass().getResource("./Pipe2.png")).getImage();
        bottomPipeImg= new ImageIcon(getClass().getResource("./Pipe1.png")).getImage();
        
        

        birdie= new Birdie(birdImg);
        pipes= new ArrayList<Pipe>();
        
        //The game loop
        gameLoopTimer= new Timer(1000/60, this); //60 frames per second loop
        gameLoopTimer.start();
        
        
        //Timer for obstacles
        PlaceObstacleTimer= new Timer(1500, new ActionListener() { //This will call obstacles every 1.5 secs
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaceObstacle();
            }
        });
        PlaceObstacleTimer.start();
    }

    //To place the obstacles
    public void PlaceObstacle() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        //To generate random heights, it gives a value between 0-1 and multiplies with pipeheight/2; if zero then pipe - pipeheight/4, if one pipe - pipeheight/2

        int spaceBetween= boardHeight/4; //The space between the obstacles for the birdie to go through                                                                            
        Pipe topPipe= new Pipe(topPipeImg);                                            
        topPipe.y= randomPipeY;                                                        
        pipes.add(topPipe);  
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y= topPipe.y+ pipeHeight+ spaceBetween;
        pipes.add(bottomPipe); //To set the obstacle position                                                        
    }                                                                                  

public void paintComponent(Graphics g ){ 
    super.paintComponent(g);
    draw(g);
}

public void draw(Graphics g ) {
    //System.out.println("draw"); To draw 60 times per second frame (This was just for testing if the loop was working properly)
    g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null); //For the background
    g.drawImage(birdie.img, birdie.x, birdie.y, birdie.height, birdie.widht, null); //For the birdie

    //To draw the obstacles
    for (int i= 0; i< pipes.size(); i++){ //The for loop
        Pipe pipe= pipes.get(i);
        g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
    }
    //To show the score
    g.setColor(Color.black);
    g.setFont(new Font("Arial", Font.PLAIN, 32));
    if (gameOver) {
        g.drawString("Game Over:"+ String.valueOf((int)score), 10, 20);
    }
    else {
        g.drawString(String.valueOf((int)score), 10, 35);
    }

}

public void move() {
    birdie.y+= velocityY;
    birdie.y= Math.max(birdie.y, 0); //So that the birdie doesnt fly off the screen
    velocityY+= gravity;

    //To move the obstacles
    for (int i= 0; i< pipes.size(); i++) {
        Pipe pipe= pipes.get(i);
        pipe.x+= velocityX;

        if (!pipe.passed && birdie.x> pipe.x+ pipeWidth) { //When you move past the obstacle
            pipe.passed= true;
            score+= 0.5; //Because there are 2 pipes
        }

        if (collision(birdie, pipe)) { //If birdie collides with the pipes 
            gameOver= true;
        }
    }
    if (birdie.y> boardHeight) { //If birdie falls down
        gameOver= true;
    }
}

public boolean collision(Birdie a, Pipe b) {
    return a.x < b.x + b.width && //a's top left doesnt reach b's top right corner
           a.x + a.widht> b.x && //a's top right doesnt reach b's top left corner
           a.y< b.y+ b.height && //a's top left doesnt reach b's bottom left corner
           a.y+ a.height> b.y; //a's bottom left passes b's top left corner
    
}

public void soundEffect(String soundFile) { //For the jumping sound effect
    try {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(soundFile)); //For reading the audio file 
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    } catch (Exception e) { //Used try and catch block so the game doesnt crash if it doesnt play the sound
        e.printStackTrace();
    }
}


@Override
public void actionPerformed(ActionEvent e) { //The action performs 60 times per second
    move();
    repaint();
    if (gameOver) {
        PlaceObstacleTimer.stop();
        gameLoopTimer.stop();
    }
}


@Override
public void keyPressed(KeyEvent e) {
if (e.getKeyCode()== KeyEvent.VK_SPACE) { //When player presses space to move the bird up
    velocityY= -9;
    
    //To reset the game for playing again 
    if (gameOver){
        birdie.y= birdieY;
        velocityY= 0;
        pipes.clear();
        score= 0;
        gameOver= false;
        gameLoopTimer.start();
        PlaceObstacleTimer.start();
    }
     soundEffect("./blurp_x.wav"); //For playing the jump sound
}
}

@Override
public void keyTyped(KeyEvent e) {
 
}
@Override
public void keyReleased(KeyEvent e) {

}
}
