/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.Client;

import game.Entity.Enemy;
import game.ADT.ArrayListWithIterator;
import game.ADT.ArrListWithIteratorInterface;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import game.Entity.Player;
import game.Entity.Shot;
import game.Entity.Weapon;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Game extends Canvas implements Runnable {
    //gameboard width
    public static final int WIDTH = 320;
    //gameboard height
    public static final int HEIGHT = WIDTH / 12 * 9;
    //scaling used in adjusting gameboard height
    public static final int SCALE = 2;
    //game title
    public final String TITLE = "Space Invaders";
    //initialize menuSong
    private File menuSong = new File("src//sounds//Menu.wav");
    //default running status
    private boolean running = false;
    //declare thread
    private Thread thread;
    //initialize BufferedImage
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    
    //space to the left and right border
    int BORDER_RIGHT = 50;
    int BORDER_LEFT = 5;
    //move down range
    int GO_DOWN = 30;
    //enemy height and width
    final int ENEMY_HEIGHT = 30;
    final int ENEMY_WIDTH = 40;
    //enemy default position
    final int ENEMY_INIT_X = 0;
    final int ENEMY_INIT_Y = 35;
    //timer speed delay <- affect enemy speed?
    final int DELAY = 15;
    //enemy default moving speed and direction, positive is right, negative is left
    private int direction = 2;
    //random number match trigger then enemy shoot
    int trigger;
    //laser width and height
    final int LASER_HEIGHT = 10;
    final int LASER_WIDTH = 5;
    //player width and height
    final int PLAYER_WIDTH = 45;
    final int PLAYER_HEIGHT = 40;
    //declare weapon
    private ArrayList<Weapon> w;
    //status
    private boolean start = true;
    private boolean stop = false;
    //buffTime
    private Instant buffTimeStart;
    private boolean startBuff = false;
    //bullet
    private final int bulletSpeed = 2;
    private int BulletTemSpeed = bulletSpeed;
    //declare level menu
    private LevelMenu lvlmenu;
    //declare menu
    private Menu menu;
    //player shooting status
    private boolean isShooting = false;
    //counter for enemy killed
    private int enemyKilled = 0;
    //initialize buff
    public boolean buff = false;
    //declare sound path
    String filepathM = "src/sounds/Menu.wav";
    String filepathG = "src/sounds/Game.wav";
    //random number
    private Random r = new Random();
    //declare weapon
    private Weapon useW;
    //declare timestart
    private Instant timestart;
    //declare player
    private Player player;
    private Player p;
    private ArrayList<Player> ship = new ArrayList();
    //declare enemy
    private ArrListWithIteratorInterface<Enemy> enemyList;
    private ArrayList<Enemy> enemy = new ArrayList();
    private Enemy en;
    //declare controller
    public Controller c;
    //declare bullets
    public LinkedList<Shot> es;
    public Shot shot;
    
    int numberLives = 3;
    int score = 0;
    int level = 1;
        
    public static enum STATE {
        MENU,
        GAME,
        LVLMENU
    };

    public static STATE state = STATE.MENU;

    public void init() {
        player = new Player();
        this.requestFocus();
        addKeyListener(new TAdapter(this));
        addMouseListener(new MouseInput(this));
        c = new Controller(this);
        menu = new Menu();
        es = c.getEs();
        w = c.getW();
        enemyInit();
    }

    private void enemyInit() {
        enemyList = new ArrayListWithIterator<>();
        //row
        for (int i = 0; i < 2; i++) {
            //column
            for (int j = 0; j < 5; j++) {
                var enemy = new Enemy(ENEMY_INIT_X + 70 * j,
                        ENEMY_INIT_Y + 60 * i, ENEMY_WIDTH, ENEMY_HEIGHT, LASER_WIDTH, LASER_HEIGHT);
                enemyList.add(enemy);
            }
        }
    }

    private static void PlaySound(String musicLocation, boolean play) {

        try {
            File musicPath = new File(musicLocation);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            if (play) {
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.stop();
            }

        } catch (Exception ex) {

        }

    }

    private synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private synchronized void stop() {
        if (!running) {
            return;
        }

        running = false;
        try {
            thread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.exit(1);
    }

    private void render() {

        BufferStrategy bs = this.getBufferStrategy();

        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);

        if (state == STATE.GAME) {
            g.drawImage(player.getImage(), (int) player.getX(), (int) player.getY(), PLAYER_WIDTH, PLAYER_HEIGHT, this);
            
            c.render(g);
            drawEnemies(g);
            drawLaser(g);
            
            //display player lives
            g.setColor(Color.WHITE);
            g.drawString("Lives: ",11 ,20);
            for(int i = 0; i < ship.size(); i++)
            {
                ship.get(i).lifeDraw(g);
            }
            
            //display player score
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 290, 20);
            
            //makes a string that says "+100" on enemy hit
            
            //show level
            g.setColor(Color.WHITE);
            g.drawString("Level " + level, 590, 20);
            

        } else if (state == STATE.MENU) {
            menu.render(g);
        }else if (state == STATE.LVLMENU){
          lvlmenu.levelMenu(g);}

        g.dispose();
        bs.show();
    }

    private void drawEnemies(Graphics g) {
        var iterator = enemyList.getIterator();

        iterator.forEachRemaining(Enemy -> {
            g.drawImage(Enemy.getImage(), (int) Enemy.getX(), (int) Enemy.getY(), ENEMY_WIDTH, ENEMY_HEIGHT, this);
        });
    }

    private void RandomWeapon() {
        double x = r.nextInt(Game.WIDTH * Game.SCALE);
        double y = r.nextInt(Game.WIDTH * Game.SCALE);
        Instant time = Instant.now();
        c.addWeapon(new Weapon(x, y, time));
    }

    public static void main(String[] args) {
        Game game = new Game();

        game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

        JFrame frame = new JFrame(game.TITLE);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.start();
    }

    @Override
    public void run() {
        init();
        int fps = 60;  //frame per second 
        double timePerTick = 1000000000 / fps;
        double delta = 0;
        long now;
        int updates = 0;
        long lastTime = System.nanoTime();

        if (state != STATE.GAME) {
            render();
            PlaySound(filepathG, false);
            PlaySound(filepathM, true);
        }

        while (running) {
            now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            lastTime = now;
            
            if (delta >= 1) {
                tick();
                render();
                delta--;
                if (state == STATE.GAME) {
                    
                    for(int column = 0; column < numberLives; column++)
                    {
                        p = new Player(48 + (column * 20), 10, Color.WHITE);
                        ship.add(p);
                    }
                    
                    update();
                }
            }

            if (state == STATE.GAME) {

                if (start) {
                    timestart = Instant.now();
                    start = false;
                }
                if (!stop) {
                    Instant stopt = Instant.now();
                    Duration tt = Duration.between(timestart, stopt);
                    if (tt.getSeconds() == 6) {
                        RandomWeapon();
                        start = true;
                    }
                }

                if (!c.isWEmpty()) {
                    for (int i = 0; i < c.getW().size(); i++) {
                        Instant endTime = Instant.now();
                        Duration interval = Duration.between(w.get(i).getStartTime(), endTime);
                        if (interval.getSeconds() == 5) {
                            c.removeWeapon((Weapon) c.getW().get(i));
                        }
                    }
                }
            }
        }

        stop();
    }

    private void tick() {
        if (state == STATE.GAME) {
            c.tick();

            if (Collision(player, w)) {
                int s = getRandomInRange(12, 15);
                c.addWeaponW(new Weapon(s));
                System.out.println("WeaponAdd");
            }
            if (Collision(player, enemyList)) {
                //System.out.println("debug");
                stop();
            }

            Collision(enemyList, es);

            if (startBuff) {
                startBuff();
                startBuff = false;
            }

            if (buff) {
                Instant endBuffTime = Instant.now();
                Duration interval = Duration.between(useW.getStartTime(), endBuffTime);
                if (interval.getSeconds() == 6) {
                    BulletTemSpeed = bulletSpeed;
                    buff = false;
                    System.out.println("Stop Buff");
                    useW = null;
                }
            }
        }
    }

    public int getRandomInRange(int start, int end) {
        return start + r.nextInt(end - start + 1);
    }

    private void update() {
        
        if (enemyList.isEmpty()) {
            ship.clear();
            level += 1;
            enemyInit();
        }
        
         var iterator = enemyList.getIterator();

        while (iterator.hasNext()) {

            double x = iterator.next().getX();
            //System.out.println(x);
            //when enemy reach right border it change moving direction and move down
            if (level == 1 && x >= WIDTH * SCALE - BORDER_RIGHT && direction != -2) {

                direction = -2;

                var iterator2 = enemyList.getIterator();

                while (iterator2.hasNext()) {

                    Enemy enemy = iterator2.next();
                    enemy.setY(enemy.getY() + GO_DOWN);
                }
            }else if (level == 2 && x >= WIDTH * SCALE - BORDER_RIGHT && direction!= -2)
            {
            direction = -3;

                var iterator2 = enemyList.getIterator();

                while (iterator2.hasNext()) {

                    Enemy enemy = iterator2.next();
                    enemy.setY(enemy.getY() + GO_DOWN);
                }
            }else if(level == 3 && x >= WIDTH * SCALE - BORDER_RIGHT && direction!= -2)
            {
            direction = -4;

                var iterator2 = enemyList.getIterator();

                while (iterator2.hasNext()) {

                    Enemy enemy = iterator2.next();
                    enemy.setY(enemy.getY() + GO_DOWN);
                }
            }else if(level == 4)
            {JOptionPane.showMessageDialog(null, "You have achieve all the stage, thanks for playing!!");
                System.exit(0);}
              
            //when enemy reach left border it change moving direction and move down
            if (level == 1 && x <= BORDER_LEFT && direction != 2) {

                direction = 2;

                var iterator3 = enemyList.getIterator();

                while (iterator3.hasNext()) {

                    Enemy enemy2 = iterator3.next();
                    enemy2.setY(enemy2.getY() + GO_DOWN);
                }
            }else if(level == 2 && x <= BORDER_LEFT && direction != 2){
            direction = 3;

                var iterator3 = enemyList.getIterator();

                while (iterator3.hasNext()) {

                    Enemy enemy2 = iterator3.next();
                    enemy2.setY(enemy2.getY() + GO_DOWN);
                }
                
            }else if(level == 3 && x <= BORDER_LEFT && direction != 2)
            {
            direction = 4;

                var iterator3 = enemyList.getIterator();

                while (iterator3.hasNext()) {

                    Enemy enemy2 = iterator3.next();
                    enemy2.setY(enemy2.getY() + GO_DOWN);
                }
            }else if (level == 4)
            {
            JOptionPane.showMessageDialog(null, "You have achieve all the stage, thanks for playing!!");
                System.exit(0);
            }
        }

        var it = enemyList.getIterator();
        //enemy movement
        while (it.hasNext()) {

            Enemy enemy = it.next();

            if (enemy.isVisible()) {
                enemy.move(direction);
            }
        }

        // lasers
        var randomNumber = new Random();

        var iterator3 = enemyList.getIterator();

        iterator3.forEachRemaining(Enemy -> {
            // 0-30
            int rand = randomNumber.nextInt(30);

            Enemy.Laser laser = Enemy.getLaser();          
            //System.out.println("rand :"+ rand);
            //System.out.println("trigger :"+trigger);
            //System.out.println("isVisible :"+Enemy.isVisible());
            //System.out.println("isRemove :"+laser.isRemove());
            
            //when random number match trigger number, enemy shoot
            if (rand == trigger && Enemy.isVisible() && laser.isRemove()) {

                laser.setRemove(false);
                laser.setX(Enemy.getX());
                laser.setY(Enemy.getY());
            }
            //when laser is outside of the gameboard, remove laser
             (!laser.isRemove()) {
                //laser moves 1px 
                laser.setY(laser.getY() + 1);

                if (laser.getY() >= HEIGHT * SCALE) {

                    laser.setRemove(true);
                }
            }
        });
        
        

    }

    private class TAdapter extends KeyAdapter {

        private Game game;

        public TAdapter(Game game) {
            this.game = game;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            player.tick();

            switch (key) {
                case KeyEvent.VK_RIGHT:
                    player.setDx(0);
                    break;
                case KeyEvent.VK_LEFT:
                    player.setDx(0);
                    break;
                case KeyEvent.VK_DOWN:
                    player.setDy(0);
                    break;
                case KeyEvent.VK_UP:
                    player.setDy(0);
                    break;
                case KeyEvent.VK_SPACE:
                    isShooting = false;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            player.tick();

            if (state == STATE.GAME) {
                switch (key) {
                    case KeyEvent.VK_RIGHT:
                        player.setDx(5);
                        break;
                    case KeyEvent.VK_LEFT:
                        player.setDx(-5);
                        break;
                    /*case KeyEvent.VK_DOWN:
                        player.setDy(5);
                        break;
                    case KeyEvent.VK_UP:
                        player.setDy(-5);
                        break;*/
                    default:
                        break;
                }
                if (key == KeyEvent.VK_SPACE && !isShooting) {
                    isShooting = true;
                    c.addBullet(new Shot(player.getX(), player.getY(), game, c, BulletTemSpeed));
                } else if (key == KeyEvent.VK_Z && !buff) {
                    if (!c.isEmptyWaitingW()) {
                        useW = c.removeWeaponW();
                        startBuff = true;
                    }
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class MouseInput implements MouseListener {

        private Game game;

        public MouseInput(Game game) {
            this.game = game;
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            int mx = e.getX();
            int my = e.getY();
            
            if(Game.state == Game.state.MENU){
            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220 ){
                if (my >= 150 && my <= 200) {
                    Game.state = Game.state.GAME;
                    PlaySound(filepathM, false);
                    PlaySound(filepathG, true);
                }
            }

            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220) {
                if (my >= 250 && my <= 300) {
                    displayHelp();
                }
            }

            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220) {
                if (my >= 350 && my <= 400) {
                    System.exit(1);
                }
            }
            
            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220){
            if (my >= 450 && my <= 500) {
                    //displayLevel()
                    Game.state = Game.state.LVLMENU;
                    displayLevel();
                    
                }
            }}else if(Game.state == Game.state.LVLMENU){
                if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220) {
                if (my >= 150 && my <= 200) {
                    Game.state = Game.state.GAME;
                    level = 1;
                    PlaySound(filepathM, false);
                    PlaySound(filepathG, true);
                }
            }

            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220) {
                if (my >= 250 && my <= 300) {
                    Game.state = Game.state.GAME;
                    level = 2;
                    PlaySound(filepathM, false);
                    PlaySound(filepathG, true);
                }
            }

            if (mx >= Game.WIDTH / 2 + 120 && mx <= Game.WIDTH / 2 + 220) {
                if (my >= 350 && my <= 400) {
                    Game.state = Game.state.GAME;
                    level = 3;
                    PlaySound(filepathM, false);
                    PlaySound(filepathG, true);
                }
            }}
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }

    public boolean Collision(Player p, ArrayList<Weapon> w) {
        for (int i = 0; i < w.size(); i++) {
            if (p.getBounds().intersects(w.get(i).getBounds())) {
                System.out.println("Weapon Detected");
                c.removeWeapon((Weapon) c.getW().get(i));
                return true;
            }
        }
        return false;
    }

    public boolean Collision(Player p, ArrListWithIteratorInterface<Enemy> enemyList) {
        for (int i = 1; i < enemyList.getLength() + 1; i++) {
            //player touch with enemy or enemy inner class laser, return true
            if (p.getBounds().intersects(enemyList.getEntry(i).getBounds())
                    || p.getBounds().intersects(enemyList.getEntry(i).getLaser().getBounds()))
            {
                JOptionPane.showMessageDialog(null, "You lose the game");
                System.exit(0);
                level = 1;
                
                return true;  
                
                return true;              
                //System.out.println("player bound:" + p.getBounds());

                //System.out.println("laser bound:" + enemyList.getEntry(i).getLaser().getBounds());
                //System.out.println("enemy intersect:" + p.getBounds().intersects(enemyList.getEntry(i).getBounds()));
                //System.out.println("laser intersect:" + p.getBounds().intersects(enemyList.getEntry(i).getLaser().getBounds()));
                //System.out.println(i);
                
            }
            
           

        }
        return false;
    }
    
    public boolean Collision(ArrListWithIteratorInterface<Enemy> enemyList, LinkedList<Shot> es) {
        //when there's at least 1 shot on the gameboard
        if (!es.isEmpty()) {
            for (int i = 1; i < enemyList.getLength() + 1; i++) {

                for (int j = 0; j < es.size(); j++) {
                    //if enemy touch bullet, remove both
                    if (enemyList.getEntry(i).getBounds().intersects(es.get(j).getBounds())) {
                        enemyKilled++;
                        score += 100;
                        enemyList.remove(i);
                        es.remove(j);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void startBuff() {
        System.out.println("Start Buff");
        buffTimeStart = Instant.now();
        useW.setStartTime(buffTimeStart);
        BulletTemSpeed = useW.getSpeed();
        buff = true;
    }

    public void displayLevel()
    {
     Game game = new Game();

        game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

      JFrame frame = new JFrame(game.TITLE); 
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(false);
    }
    
    public void displayHelp() {
        JFrame helpFrame = new JFrame();

        String img = "src/images/help.jpg";
        ImageIcon icon = new ImageIcon(img);

        Image img1 = icon.getImage();
        Image newimg = img1.getScaledInstance(1000, 600, java.awt.Image.SCALE_SMOOTH);
        ImageIcon newIcon = new ImageIcon(newimg);
        JLabel lable = new JLabel(newIcon);

        helpFrame.add(lable);
        helpFrame.setSize(1000, 600);
        helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        helpFrame.setVisible(true);

    }

    private void drawLaser(Graphics g) {
        var iterator = enemyList.getIterator();

        iterator.forEachRemaining(Enemy -> {

            Enemy.Laser laser = Enemy.getLaser();

            if (!laser.isRemove()) {

                g.drawImage(laser.getImage(), (int) laser.getX(), (int) laser.getY(), LASER_WIDTH, LASER_HEIGHT, this);
                //System.out.println(laser.getBounds());
            }
        });
    }
}
