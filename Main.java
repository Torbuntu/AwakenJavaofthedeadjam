import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.Castpixel16;
import femto.font.TIC80;
import Math;
import backgrounds.Playfield;
import entities.hero.Hero;
import entities.enemies.zombie.Zombie;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    float time;
    Playfield playField;
    Hero hero;
    int hx, hy;
    int state;//0 = title, 1=game, 2=pre-day, 3=pause, 4=game-over
    Zombie zombie;
    
    Zombie[] wave;
    int[] eCool;
    
    int t;
    
    int left, right;
    // start the game using Main as the initial state
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(Castpixel16.palette(), TIC80.font());
        playField = new Playfield();
        hero = new Hero();
        hx = 1;
        hy = 1;
        time = 32.0f;
        zombie = new Zombie();
        zombie.x = 220;
        zombie.y = 59+Math.random(0,5)*24;
        
        left = 0;//shovel
        right = 1;//yoyo
      
        makeWave(2);      
        t = 0;
    }
    
    void makeWave(int amount){
        wave = new Zombie[amount];
        eCool = new int[amount];
        for(int i = 0; i < amount; i++){
            wave[i] = new Zombie();
            wave[i].x = 220;
            wave[i].y = 59+Math.random(0,5)*24;59+Math.random(0,5)*24;
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
        if(t>300){
            t=0;
        }
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
                
                if(zombie.x < 0) {
                    zombie.x = 220;
                    zombie.y = 59+Math.random(0,5)*24;
                }
                zombie.walk();
                zombie.x -= 0.1f;
                
                
                moveZombies();
                moveHero();
                
                hero.draw(screen);
                zombie.draw(screen);
                drawZombies();
                
                time += 0.1f;
                if(time >= 190) time = 32.0f;
                screen.drawLine(32.0f,16.0f, time, 16.0f, 14, false);
                // Update the screen with everything that was drawn
                if( Button.C.justPressed() ) //state = 3;
                break;
            case 2:
                
                break;
            case 3:
                
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
        }
    }
    void drawZombies(){
        for(Zombie z : wave){
            z.draw(screen);
        }
    }
    
    void moveHero(){
        
        if (!Button.Up.isPressed() && !Button.Down.isPressed() && !Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed()) {
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
        
        if( Button.A.isPressed() ){
            itemAction(left);
        }
        if( Button.B.isPressed() ){
            itemAction(right);
        }
        
        //Translate to grid
        hero.x = 6+hx*24;
        hero.y = 59+hy*24;
    }
    
    void itemAction(int hand){
        switch(hand){
            case 0:
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
            case 1:
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
        }
    }
    
    boolean hit(float zx, float hx, int adist, int bdist){
        return zx <= hx+adist && zx >= hx+bdist;
    }
    
}
