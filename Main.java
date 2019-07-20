import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.JmpJapaneseMachinePalette;
import femto.font.TIC80;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Hero hero;
    BG_TEST bg;
    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(JmpJapaneseMachinePalette.palette(), TIC80.font());
        System.out.println("Width: " + screen.width() + " Height: " + screen.height());
        bg = new BG_TEST();
        
        hero = new Hero();
        hero.x = 160;
        hero.y = 90;
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
        bg.draw(screen, 0.0f, 0.0f);
        // Change to a new state when A is pressed
        
        if( Button.A.isPressed() ){
            //Game.changeState( new Main() );
            hero.yoyo();
        }
        
        if(Button.Left.isPressed()){
            
            hero.x -= 1;
            hero.setMirrored(true);
            hero.walk();
        }
        if(Button.Right.isPressed()){
            hero.x+= 1;
            hero.walk();
            hero.setMirrored(false);
        }
        
        if(!Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed()){
            hero.idle();
        }
    
        hero.draw(screen);
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
}
