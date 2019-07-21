import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.Cgarne;
import femto.font.TIC80;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Hero hero;
    BG_TEST bg;

    int gameState = 0; //0=title, 1=menu, 2=lvl1, 3=lvl2, 4=lvl3, 5=lvl4, 6=lvl5

    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args) {
        Game.run(TIC80.font(), new Main());
    }

    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init() {
        screen = new HiRes16Color(Cgarne.palette(), TIC80.font());
        System.out.println("Width: " + screen.width() + " Height: " + screen.height());
        bg = new BG_TEST();

        hero = new Hero();
        hero.x = 160;
        hero.y = 90;
    }

    // Might help in certain situations
    void shutdown() {
        screen = null;
    }

    // update is called by femto.Game every frame
    void update() {
        screen.clear(0);
        switch (gameState) {
            case 0: //title
                screen.setTextColor(15); //white
                screen.setTextPosition(100, 100);
                screen.print("Press C");
                if(Button.C.isPressed()){
                    gameState+=1;
                }
                break;
            case 1: //menu
                demoStage();
                break;
        }

        // Update the screen with everything that was drawn
        screen.flush();
    }

    public void demoStage() {
        bg.draw(screen, 0.0f, 0.0f);

        if (Button.A.isPressed()) {
            hero.yoyo();
        }

        if (Button.B.isPressed()) {
            hero.shovel();
        }

        if (Button.Left.isPressed()) {

            hero.x -= 1;
            hero.setMirrored(true);
            hero.walk();
        }
        if (Button.Right.isPressed()) {
            hero.x += 1;
            hero.walk();
            hero.setMirrored(false);
        }

        if (!Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed()) {
            hero.idle();
        }

        hero.draw(screen);
    }

}