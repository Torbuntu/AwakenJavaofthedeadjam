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
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        switch(state){
            case 0:
                screen.clear( 0 );
                if( Button.C.justPressed() ){
                    state = 1;
                }
                break;
            case 1:
                screen.clear( 0 );
                moveHero();
                
                playField.draw(screen, 0.0f, 0.0f);
                
                hero.draw(screen);
                
                if(zombie.x < 0) {
                    zombie.x = 220;
                    zombie.y = 59+Math.random(0,5)*24;
                }
                zombie.walk();
                zombie.x -= 0.1f;
                zombie.draw(screen);
                
                time += 0.1f;
                if(time >= 190) time = 32.0f;
                screen.drawLine(32.0f,16.0f, time, 16.0f, 14, false);
                // Update the screen with everything that was drawn
                
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
        
        
        hero.x = 6+hx*24;
        hero.y = 59+hy*24;
    }
    
    void itemAction(int hand){
        switch(hand){
            case 0:
                hero.shovel();
                if(zombie.x <= hero.x+18 && zombie.x >= hero.x+8 && zombie.y == hero.y) zombie.x+=6;
                break;
            case 1:
                hero.yoyo();
                if(zombie.x <= hero.x+27 && zombie.x >= hero.x+20 && zombie.y == hero.y) zombie.x+=1;
                break;
        }
    }
    
}
