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




class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Playfield playField;
    Inventory inventoryScreen;
    Hero hero;
    Zombie[] wave;
    int lives;
    Heart heart;
    Coffea[] plants;
    
    int plantCount;
    
    int hx, hy, left, right, t, cooldown;
    int state;//0 = title, 1=game, 2=pre-day, 3=pause, 4=game-over
    
    int[] eCool;
    
    float time;
    
    //inventory variables
    int handSelect;//0=left, 1=right
    
    // start the game using Main as the initial state
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(Castpixel16.palette(), TIC80.font());
        playField = new Playfield();
        inventoryScreen = new Inventory();
        hero = new Hero();
        hx = 1;
        hy = 1;
        time = 8.0f;
        
        left = 0;//shovel
        right = 1;//yoyo
      
        makeWave(2);      
        t = 0;
        
        lives = 5;
        heart = new Heart();
        
        handSelect = 0;
        
        plants = new Coffea[45];
 
    }

    void makeWave(int amount){
        wave = new Zombie[amount];
        eCool = new int[amount];
        for(int i = 0; i < amount; i++){
            wave[i] = new Zombie();
            wave[i].x = 220;
            wave[i].y = 60+Math.random(0,5)*24;60+Math.random(0,5)*24;
            eCool[i] = 0;
        }
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
        
        switch(state){
            case 0:
                screen.clear( 0 );
                if( Button.C.justPressed() ){
                    state = 1;
                }
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
                if(time >= 190) time = 8.0f;
                screen.drawLine(8.0f, 32.0f, time, 32.0f, 14, false);
                
                if( Button.C.justPressed() ) state = 3;
                
                break;
            case 2:
                
                break;
            case 3:
                screen.clear(0);
                inventoryScreen.draw(screen, 0.0f, 0.0f);
                drawLives();
                if( Button.C.justPressed() ) state = 1;
                if( Button.Left.justPressed() ) handSelect = 0;
                if( Button.Right.justPressed() ) handSelect = 1;
                
                if( Button.Up.justPressed() ){
                    if(handSelect == 0){
                        if( left > 0 ) left--;
                    }else{
                        if( right > 0 )right--;
                    }
                }
                if( Button.Down.justPressed() ){
                    if(handSelect == 0){
                        if( left < 4 ) left++;
                    }else{
                        if( right < 4 )right++;
                    }
                }
                
                if(handSelect == 0){
                    screen.drawRect(8, 51+left*24, 17, 17, 9);
                }else{
                    screen.drawRect(56, 51+right*24, 17, 17, 9);
                }
                
                break;
            case 4:
                
                break;
        }
        
        screen.flush();
        
    }
    
    void moveZombies(){
        for(int i = 0; i < wave.length; i++){
            
            if(eCool[i] > 0) {
                eCool[i] -= 1;
                wave[i].hurt();
            } else{
                wave[i].x -= 0.1f;
                wave[i].walk();
            }
            if(wave[i].x < 0) wave[i].x = 220;
        }
    }
    void drawZombies(){
        for(Zombie z : wave){
            z.draw(screen);
        }
    }
    
    void moveHero(){
        
        if (!Button.Up.isPressed() && !Button.Down.isPressed() && !Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed() && cooldown == 0) {
            hero.idle();
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
                for(int i = 0; i < wave.length; i++){
                    if (wave[i].y == hero.y ){
                        if(hit(wave[i].x, hero.x, 18, 8)){
                            wave[i].x += 6;
                            eCool[i] = 50;
                        }
                    }
                }
                
                break;
            case 1://yoyo
                hero.yoyo();
                for(int i = 0; i < wave.length; i++){
                    if(wave[i].y == hero.y){
                        if(hit(wave[i].x, hero.x, 27, 12) ){
                            wave[i].x += 1;
                            eCool[i] = 10;
                        }
                    }
                }
                break;
            case 2://plant

                if( !containsPlant() ){
                    Coffea n = new Coffea();
                    n.x = 6+hx*24;
                    n.y = 60+hy*24;
                    for(int i = 0; i < plants.length; i++){
                        if(plants[i] == null){
                            plants[i] = n;
                            break;
                        }
                    }
                }
                hero.plant();
                
                break;
        }
    }
    
    boolean zombieHitPlayer(){
        for(Zombie z : wave){
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
                    heart.draw(screen, 96.0f, (float)(48+i*24));
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

}
