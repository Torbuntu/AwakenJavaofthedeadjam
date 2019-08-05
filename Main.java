import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.Castpixel16;
import femto.font.TIC80;
import Math;
import backgrounds.Playfield;
import backgrounds.Inventory;
import entities.hero.Hero;
import entities.enemies.zombie.Zombie;
import entities.plant.Coffea;
import item.Heart;
import item.Sprout;
import item.Shovel;
import item.Yoyo;
import ZombieImpl;



class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Playfield playField;
    Inventory inventoryScreen;
    Hero hero;
 
    Heart heart;
    Coffea[] plants;
    
    Shovel shovel;
    Sprout sprout;
    Yoyo yoyo;
    
    ZombieImpl zombies;
    
    float time;
    int lives, kills;
    
    int hx, hy, left, right, t, cooldown, plantCount, seeds, timeToPlant;
    int state;//0 = title, 1=game, 2=pre-day, 3=pause, 4=game-over
    
    //inventory variables
    int handSelect;//0=left, 1=right
    
    // start the game using Main as the initial state
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        System.out.println("::: Start init :::");
        screen = new HiRes16Color(Castpixel16.palette(), TIC80.font());
        playField = new Playfield();
        inventoryScreen = new Inventory();
        hero = new Hero();
        
        shovel = new Shovel();
        sprout = new Sprout();
        yoyo = new Yoyo();
        
        hx = 1;
        hy = 1;
        time = 8.0f;
        
        left = 0;//shovel
        right = 1;//yoyo
      
        zombies = new ZombieImpl(2);
      
        t = 0;
        
        lives = 5;
        kills = 0;
        heart = new Heart();
        seeds = 1;//Start you off with one lonely seed. Don't screw it up! :D
        timeToPlant = 0;
        
        handSelect = 0;
        
        plants = new Coffea[45];
        System.out.println("::: Finished init :::");
 
    }

    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        t += 1;
        if(t>300) t=0;
        if(cooldown > 0) cooldown--;
        
        screen.clear(0);
        switch(state){
            case 0:
                if( Button.C.justPressed() ){
                    state = 1;
                }
                screen.setTextColor(1);
                screen.setTextPosition(10, 10);
                screen.print("Press C to play");
                break;
            case 1:
                screen.clear( 0 );
                
                
                playField.draw(screen, 0.0f, 0.0f);
                
                moveZombies();
                if( zombieHitPlayer() && cooldown == 0){
                    lives--;
                    cooldown = 100;
                }
                moveHero();
                
                drawPlants();
                hero.draw(screen);
                drawZombies();
                drawLives();
                
                time += 0.05f;
                if(time >= 190){
                    time = 8.0f;  
                    state = 2;
                } 
                
                //Day meter
                screen.drawLine(8.0f, 32.0f, time, 32.0f, 14, false);
                
                if( Button.C.justPressed() ) state = 3;
                
                break;
            case 2:
                if ( Button.C.justPressed() ) {
                    state = 1;
                }
                screen.setTextColor(1);
                screen.setTextPosition(10, 10);
                screen.print("Press C to start the next day");
                break;
            case 3:
                inventoryScreen.draw(screen, 0.0f, 0.0f);
                drawLives();
                if( Button.C.justPressed() ) state = 1;
                if( Button.Left.justPressed() && handSelect > 0) handSelect --;
                if( Button.Right.justPressed() && handSelect < 4) handSelect ++;
                
                if( Button.A.justPressed() && right != handSelect) left = handSelect;
                if( Button.B.justPressed() && left != handSelect) right = handSelect;
                
                //draw
                drawInventory(8, left);
                drawInventory(56, right);
                
                //draw handSelect
                screen.drawRect(8+handSelect*24, 38, 17, 17, 9);
                
                break;
            case 4:
                
                break;
        }
        
        screen.flush();
    }
    
    void moveZombies(){
        for(int i = 0; i < zombies.getSize(); i++){
            if(zombies.getHealth(i) < 0) {
                zombies.setHealth(i, 10);
                zombies.getZombie(i).x = 220;
                zombies.getZombie(i).y = 60+Math.random(0,5)*24;
                kills++;
            }
            if(zombies.getCooldown(i) > 0) {
                zombies.setCooldown(i, zombies.getCooldown(i)-1);
                zombies.getZombie(i).hurt();
            } else{
                zombies.getZombie(i).x -= 0.1f;
                zombies.getZombie(i).walk();
            }
            if(zombies.getZombie(i).x < 0) zombies.getZombie(i).x = 220;
        }
    }
    void drawZombies(){
        for(Zombie z : zombies.getAllZombies()){
            z.draw(screen);
        }
    }
    
    void moveHero(){
        
        if (!Button.Up.isPressed() && !Button.Down.isPressed() && !Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed() && cooldown == 0) {
            hero.idle();
            timeToPlant = 0;
        }
        if( Button.Down.justPressed() && hy < 4 ){
            hy += 1;
        }
        if( Button.Up.justPressed() && hy > 0 ){
            hy -= 1;
        }
        if( Button.Right.justPressed() && hx < 8 ){
            hx += 1;
        }
        if( Button.Left.justPressed() && hx > 0 ){
            hx -= 1;
        }
        
        if( Button.A.isPressed() && cooldown == 0){
            itemAction(left);
        }
        if( Button.B.isPressed() && cooldown == 0 ){
            itemAction(right);
        }
        
        if(cooldown > 0)hero.hurt();
        
        //Translate to grid
        hero.x = 6+hx*24;
        hero.y = 60+hy*24;
    }
    
    void itemAction(int hand){
        switch(hand){
            case 0://shovel
                hero.shovel();
                for(int i = 0; i < zombies.getSize(); i++){
                    if (zombies.getZombie(i).y == hero.y ){
                        if(hit(zombies.getZombie(i).x, hero.x, 18, 8)){
                            zombies.getZombie(i).x += 6;
                            zombies.setCooldown(i, 80);
                            zombies.setHealth(i, zombies.getHealth(i) - 4);
                        }
                    }
                }
                
                break;
            case 1://yoyo
                hero.yoyo();
                for(int i = 0; i < zombies.getSize(); i++){
                    if(zombies.getZombie(i).y == hero.y){
                        if(hit(zombies.getZombie(i).x, hero.x, 27, 12) ){
                            zombies.getZombie(i).x += 1;
                            zombies.setCooldown(i, 10);
                            zombies.setHealth(i, zombies.getHealth(i) - 1);
                        }
                    }
                }
                break;
            case 2://plant

                if( !containsPlant() && seeds > 0 && timeToPlant > 45){
                    timeToPlant = 0;
                    Coffea n = new Coffea();
                    n.x = 6+hx*24;
                    n.y = 60+hy*24;
                    for(int i = 0; i < plants.length; i++){
                        if(plants[i] == null){
                            plants[i] = n;
                            break;
                        }
                    }
                }else{
                    System.out.println(timeToPlant);
                    timeToPlant++;
                }
                hero.plant();
                
                break;
        }
    }
    
    boolean zombieHitPlayer(){
        for(Zombie z : zombies.getAllZombies()){
            if(z.y == hero.y && z.x <= hero.x+12 && z.x > hero.x+3) return true;
        }
        return false;
    }
    
    boolean hit(float zx, float hx, int adist, int bdist){
        return zx <= hx+adist && zx >= hx+bdist;
    }
    
    boolean containsPlant(){
        for(Coffea c : plants){
            if(c==null)continue;
            if(c.x == 6+hx*24 && c.y == 60+hy*24) return true;
            // if(c.x == hx && c.y == hy) return true;
        }
        return false;
    }
    
    void drawLives(){
        switch(state){
            case 1:
                for(int i = 0; i < lives; i++){
                    heart.draw(screen, (float)(8+i*24), 0.0f);
                }
                break;
            case 3:
                for(int i = 0; i < lives; i++){
                    heart.draw(screen, (float)(12+i*24), 80.0f);
                }
                break;
        }
    }
    
    void drawPlants(){
        for(Coffea c : plants){
            if(c==null)continue;
            c.idle();
            c.draw(screen);
        }
    }
    
    void drawInventory(int handX, int item){
        switch(item){
            case 0:
                shovel.draw(screen, handX, 8);
                break;
            case 1:
                yoyo.draw(screen, handX, 8);
                break;
                
            case 2:
                sprout.draw(screen, handX, 8);
                break;
            case 3:
                
                break;
                
            case 4:
                
                break;
        }
    }

}
