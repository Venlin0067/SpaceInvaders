/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.Client;


import game.Entity.Enemy;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import game.Entity.Player;
import game.Entity.Shot;
import game.Entity.Weapon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Game extends Canvas implements Runnable{
	
    public static final int WIDTH = 320;
    public static final int HEIGHT = WIDTH / 12 * 9;
    public static final int SCALE = 2;
    public final String TITLE = "Space Invaders";
    
    private boolean running = false;
    private Thread thread;
    
    private BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
    
    //space of left and right border
    int BORDER_RIGHT = 50;
    int BORDER_LEFT = 5;
    //move down range
    int GO_DOWN = 30;
    //the bottom where the spaceship at
    int GROUND = 290;
    //enemy height and width
    int ENEMY_HEIGHT = 46;
    int ENEMY_WIDTH = 55;
    //enemy default position
    int ENEMY_INIT_X = 100;
    int ENEMY_INIT_Y = 0;
    //game goal
    int NUMBER_OF_ENEMIES_TO_DESTROY = 24;
    //timer speed delay <- affect enemy speed?
    int DELAY = 15;
    private int direction = -1;
    private int deaths = 0;
    
    private ArrayList<Weapon> w;
    private boolean start = true;
    private boolean stop = false;
    
    private Instant buffTimeStart;
    private boolean startBuff = false;
    
    private final int bulletSpeed = 2;
    private int BulletTemSpeed = bulletSpeed;
    
    private boolean isShooting = false;
    private int enemyKilled = 0;
    public boolean buff = false;
    
    private Random r = new Random();
    private Weapon useW;
    
    private Instant timestart;
    private Player player;
    private List<Enemy> enemies;
    public Controller c;
    public LinkedList<Shot> es;
    
    
    public void init(){
        player = new Player();
        this.requestFocus();
        addKeyListener(new TAdapter(this));
        c = new Controller(this);
        es = c.getEs();
        w = c.getW();
        enemyInit();
    }
    
    private void enemyInit() {

        enemies = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {

                var enemy = new Enemy(ENEMY_INIT_X + 50 * j,
                        ENEMY_INIT_Y + 50 * i);
                enemies.add(enemy);
            }
        }
    }
    
    private synchronized void start(){
        if(running){
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }
    
    private synchronized void stop(){
        if(!running)
            return;
        
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.exit(1);
    }
    private void render(){
        
        BufferStrategy bs = this.getBufferStrategy();
        
        if(bs == null){
            createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.drawImage(image, 0, 0, getWidth(),getHeight(),this);
        g.drawImage(player.getImage(), (int)player.getX(), (int)player.getY(), 45, 40,this);
        c.render(g);
        drawEnemies(g);
        
            
        g.dispose();
        bs.show();
    }
    
    private void drawEnemies(Graphics g) {
            for (Enemy enemy : enemies) {
                if (enemy.isVisible()) {
                    g.drawImage(enemy.getImage(), (int) enemy.getX(), (int) enemy.getY(), this);
                }
                if (enemy.isDying()) {
                    enemy.dead();
            }
        }
    }
    
    private void RandomWeapon() {
        double x = r.nextInt(Game.WIDTH * Game.SCALE);
        double y = r.nextInt(Game.WIDTH * Game.SCALE);
        Instant time = Instant.now();
        c.addWeapon(new Weapon(x,y,time));
    }
    
    
    public static void main(String[] args){
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
        double timePerTick = 1000000000/fps;
        double delta = 0;
        long now ;
        int updates = 0;
        long lastTime = System.nanoTime();
        
        while(running)
        {
            now = System.nanoTime();
            delta += (now-lastTime)/timePerTick;
            lastTime = now;
            
            if(delta >= 1){
            tick();
            render();
            delta--;
            update();
            }
            
            if(start){
                timestart = Instant.now();
                start = false;
            }
            if(!stop){
                Instant stopt = Instant.now();
                Duration tt = Duration.between(timestart,stopt);
                if(tt.getSeconds() == 6){
                    RandomWeapon();
                    start = true;
                }
            }
           
            if(!c.isWEmpty()){
                    for(int i=0;i<c.getW().size();i++){
                        Instant endTime = Instant.now();
                        Duration interval = Duration.between(w.get(i).getStartTime(), endTime);
                        if(interval.getSeconds() == 5){
                            c.removeWeapon((Weapon) c.getW().get(i));
                        }
                    } 
                }
            
        }
        stop();
    }

    private void tick() {
        c.tick();
        
        if(Collision(player,w)){
            int s = getRandomInRange(12,15);
            c.addWeaponW(new Weapon(s));
            System.out.println("WeaponAdd");
        }
        
        if(startBuff){
            startBuff();
            startBuff = false;
        }
        
        if(buff){
            Instant endBuffTime = Instant.now();
            Duration interval = Duration.between(useW.getStartTime(),endBuffTime);
            if(interval.getSeconds() == 6){
                 BulletTemSpeed = bulletSpeed;
                 buff = false;
                 System.out.println("Stop Buff");
                 useW = null;
            }
        }
        
        
    }
    
    public int getRandomInRange(int start, int end){
        return start + r.nextInt(end - start + 1);
    }
    
     private void update() {
        // enemies
        for (Enemy enemy : enemies) {

            int x = (int) enemy.getX();

            if (x >= WIDTH * SCALE - BORDER_RIGHT && direction != -1) {

                direction = -1;

                Iterator<Enemy> i1 = enemies.iterator();

                while (i1.hasNext()) {

                    Enemy a2 = i1.next();
                    a2.setY(a2.getY() + GO_DOWN);
                }
            }

            if (x <= BORDER_LEFT && direction != 1) {

                direction = 1;

                Iterator<Enemy> i2 = enemies.iterator();

                while (i2.hasNext()) {

                    Enemy a = i2.next();
                    a.setY(a.getY() + GO_DOWN);
                }
            }
        }

        Iterator<Enemy> it = enemies.iterator();

        while (it.hasNext()) {

            Enemy enemy = it.next();

            if (enemy.isVisible()) {

                int y = (int) enemy.getY();

                enemy.move(direction);
            }
        }
    }

     private class TAdapter extends KeyAdapter{
         
         private Game game;
         
         public TAdapter(Game game){
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
            
            switch (key) {
                case KeyEvent.VK_RIGHT:
                    player.setDx(5);
                    break;
                case KeyEvent.VK_LEFT:
                    player.setDx(-5);
                    break;
                case KeyEvent.VK_DOWN:
                    player.setDy(5);
                    break;
                case KeyEvent.VK_UP:
                    player.setDy(-5);
                    break;
                default:
                    break;
            }
            if(key == KeyEvent.VK_SPACE && !isShooting){
                isShooting = true;
                c.addBullet(new Shot(player.getX(),player.getY(),game,c,BulletTemSpeed));
            }else if(key == KeyEvent.VK_Z && !buff){
                if(!c.isEmptyWaitingW()){
                    useW = c.removeWeaponW();
                    startBuff = true;
                }
            }
                    
        }

        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e); //To change body of generated methods, choose Tools | Templates.
        }
         
     }
     
     public boolean Collision(Player p, ArrayList<Weapon> w){
         for(int i=0;i<w.size();i++){
            if(p.getBounds().intersects(w.get(i).getBounds())) {
                System.out.println("Weapon Detected");
                c.removeWeapon((Weapon) c.getW().get(i));
                return true;
            }

         }
         return false;
     }
     
     
     public void startBuff(){
         System.out.println("Start Buff");
         buffTimeStart = Instant.now();
         useW.setStartTime(buffTimeStart);
         BulletTemSpeed = useW.getSpeed();
         buff = true;
     }
}


