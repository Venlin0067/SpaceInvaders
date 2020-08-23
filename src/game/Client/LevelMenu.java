package game.Client;

/**
 *
 * @author Ben
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class LevelMenu {
    public Rectangle beginnerButton = new Rectangle(Game.WIDTH /2 + 100, 150, 150, 50);
    public Rectangle normalButton = new Rectangle(Game.WIDTH /2 + 100, 250, 150, 50);
    public Rectangle intermediateButton = new Rectangle(Game.WIDTH /2 + 100, 350, 150, 50);
    
    public void levelMenu(Graphics g){
        
        Graphics2D g2d = (Graphics2D)g;
        
        Font fnt0 = new Font("arial", Font.BOLD, 50);
        g.setFont(fnt0);
        g.setColor(Color.red);
        g.drawString("Select Level", Game.WIDTH / 2, 100);
        
        Font fnt1 = new Font("arial", Font.BOLD, 30);
        g.setFont(fnt1);
        g.drawString("Level 1", beginnerButton.x + 19, beginnerButton.y + 30);
        g2d.draw(beginnerButton);
        
        Font fnt2 = new Font("arial", Font.BOLD, 30);
        g.setFont(fnt2);
        g.drawString("Level 2", normalButton.x + 19, normalButton.y + 30);
        g2d.draw(normalButton);
        
        Font fnt3 = new Font("arial", Font.BOLD, 30);
        g.setFont(fnt3);
        g.drawString("Level 3", intermediateButton.x + 19, intermediateButton.y + 30);
        g2d.draw(intermediateButton);
        
        String img = "src/images/menu.png";
        ImageIcon icon = new ImageIcon(img);
        
        g.drawImage(icon.getImage(), 0, 340, 136, 145, null);
    
}
}
