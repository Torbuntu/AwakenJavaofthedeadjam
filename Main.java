import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.Castpixel16;
import femto.font.TIC80;
import Math;
import backgrounds.Playfield;
import backgrounds.Inventory;
import backgrounds.Shop;
import entities.hero.Hero;
import entities.enemies.zombie.Zombie;
import entities.plant.Coffea;
import item.Heart;
import item.Sprout;
import item.Shovel;
import item.Yoyo;
import item.Sword;
import item.Gun;
import item.NotHas;
import ZombieImpl;



class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Playfield playField;
    Inventory inventoryScreen;
    Shop shop;
    Hero hero;

    Heart heart;
    NotHas notHas;
    Coffea[] plants;

    Shovel shovel;
    Sprout sprout;
    Yoyo yoyo;
    Sword sword;
    Gun gun;

    ZombieImpl zombies;

    float time;

    int hx, hy, left, right, cooldown, plantCount, seeds, timeToPlant, coins, lives, waveNum, purchaceSelect, ammo;
    int state; //0 = title, 1=game, 2=pre-day, 3=pause/inventory, 4=game-over

    //inventory variables
    int handSelect; //0=left, 1=right
    
    boolean hasYoyo, hasSword, hasGun;
    
    String message;

    // start the game using Main as the initial state
    public static void main(String[] args) {
        Game.run(TIC80.font(), new Main());
    }

    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init() {
        System.out.println("::: Start init :::");
        screen = new HiRes16Color(Castpixel16.palette(), TIC80.font());
        playField = new Playfield();
        inventoryScreen = new Inventory();
        shop = new Shop();
        hero = new Hero();
        shovel = new Shovel();
        sprout = new Sprout();
        yoyo = new Yoyo();
        sword = new Sword();
        gun = new Gun();
        
        heart = new Heart();
        notHas = new NotHas();
        
        restart();
    }
    
    void restart(){
        hx = 1;
        hy = 1;
        time = 8.0f;

        left = 0; //planter
        right = 1; //shovel
        
        waveNum = 2;
        zombies = new ZombieImpl(waveNum);

        lives = 5;
        coins = 0;
        seeds = 1; //Start you off with one lonely seed. Don't screw it up! :D
        timeToPlant = 0;
        handSelect = 0;
        
        hasYoyo = false;
        hasGun = false;
        hasSword = false;
        purchaceSelect = 0;
        ammo = 0;
        plants = new Coffea[45];
        message = "";
        System.out.println("::: Finished init :::");
    }

    // Might help in certain situations
    void shutdown() {
        screen = null;
    }

    // update is called by femto.Game every frame
    void update() {
        screen.clear(0);
        switch (state) {
            case 0:
                if (Button.C.justPressed()) {
                    state = 1;
                }
                if(Button.Up.justPressed()){
                    hasYoyo = true;
                    hasGun = true;
                    hasSword = true;
                    coins = 10000;
                }

                hero.walk();
                hero.draw(screen, 20.0f, 20.0f);

                screen.setTextColor(11);
                screen.setTextPosition(10, 10);
                screen.print("Press C to play");

                for (int i = 0; i < 15; i++) {
                    screen.setTextColor(i);
                    screen.setTextPosition(110, i * 8);
                    screen.print("(" + i + ")");
                }
                break;
            case 1:
                playField.draw(screen, 0.0f, 0.0f);
                
                if (cooldown > 0) cooldown--;
                
                moveZombies();
                if (zombieHitPlayer() && cooldown == 0) {
                    lives--;
                    if(lives == 0) {
                        state = 4;
                        break;   
                    }
                    cooldown = 100;
                }
                moveHero();

                drawPlants();
                hero.draw(screen);
                drawZombies();
                drawLives();

                time += 0.1f;
                if (time >= 158) {
                    time = 8.0f;
                    message = "";
                    state = 2;
                }

                //Day meter
                screen.drawLine(8.0f, 32.0f, time, 32.0f, 14, false);

                screen.setTextColor(11);
                screen.setTextPosition(100, 12);
                screen.print("Coins: " + coins);

                screen.setTextColor(11);
                screen.setTextPosition(100, 20);
                screen.print("Seeds: " + seeds);
                
                if(ammo > 0){
                    screen.setTextColor(11);
                    screen.setTextPosition(100, 28);
                    screen.print("Ammo: " + ammo);
                }

                if (Button.C.justPressed()) state = 3;

                break;
            case 2://Shop screen
                shop.draw(screen, 0.0f, 0.0f);
                if(Button.Down.justPressed() && purchaceSelect < 5) purchaceSelect++;
                if(Button.Up.justPressed() && purchaceSelect > 0)purchaceSelect--;
                
                if (Button.A.justPressed()) {
                    switch(purchaceSelect){
                        case 0://seeds
                            if(coins >= 5){
                                coins -= 5;
                                seeds++;
                                message = "Purchaced a seed for 5 coins.";
                            }else{
                                message = "Not enough coins for seeds.";
                            }
                            break;
                        case 1://ammo
                            if(hasGun && coins >= 10){
                                coins -= 10;
                                ammo += 5;
                                message = "Purchaced ammo for 10 coins.";
                            }else{
                                message = "Not enough coins for ammo.";
                            }
                            break;
                        case 2://health
                            if(coins >= 100 && lives < 5){
                                coins -= 100;
                                lives++;
                                message = "Purchaced life for 100 coins.";
                            }else{
                                if(lives == 5){
                                    message = "Max lives already reached.";
                                }else{
                                    message = "Not enough coins for lives.";
                                }
                            }
                            break;
                        case 3://Yoyo
                            if (!hasYoyo && coins >= 50) {
                                hasYoyo = true;
                                coins -= 50;
                                message = "Purchaced Yoyo for 50 coins.";
                            }else{
                                if(hasYoyo){
                                    message = "You already own the Yoyo.";    
                                } else{
                                    message = "Not enough coins for yoyo.";
                                }
                            }
                            break;
                        case 4://Sword
                            if(!hasSword && coins >= 150) {
                                hasSword = true;
                                coins -= 150;
                                message = "Purchaces Sword for 150 coins.";
                            }else{
                                if(hasSword){
                                    message = "You already own the Sword.";    
                                } else{
                                    message = "Not enough coins for Sword.";
                                }
                            }
                            break;
                        case 5://Gun
                            if(!hasGun && coins >= 1000 ){
                                hasGun = true;
                                coins -= 1000;
                                message = "Purchaced Gun for 1000 coins.";
                            }else{
                                if(hasGun){
                                    message = "You already own the Gun.";    
                                } else{
                                    message = "Not enough coins for Gun.";
                                }
                            }
                            
                            break;
                        default:
                        break;
                    }
                }
                
                if (Button.C.justPressed()) {
                    state = 1;
                    waveNum += 2;
                    zombies = new ZombieImpl(waveNum);
                }
                
                if(!hasYoyo)notHas.draw(screen, 12, 90);
                if(!hasSword)notHas.draw(screen, 12, 116);
                if(!hasGun){
                    notHas.draw(screen, 12, 38);
                    notHas.draw(screen, 12, 142);
                }

                screen.setTextColor(11);
                screen.setTextPosition(34, 20);
                screen.print("Coins: " + coins);
                
                screen.setTextColor(11);
                screen.setTextPosition(34, 100);
                screen.print("Press C to start the next day");
                
                screen.setTextColor(9);
                screen.setTextPosition(34, 130);
                screen.print(message);
                
                //draw purchaceSelect
                screen.drawRect(12, 12 + purchaceSelect * 26, 17, 17, 9);

                break;
            case 3:
                inventoryScreen.draw(screen, 0.0f, 0.0f);
                drawLives();
                if (Button.C.justPressed()) state = 1;
                if (Button.Left.justPressed() && handSelect > 0) handSelect--;
                if (Button.Right.justPressed() && handSelect < 4) handSelect++;

                if (Button.A.justPressed() && right != handSelect) left = handSelect;
                if (Button.B.justPressed() && left != handSelect) right = handSelect;
                
                if(!hasYoyo && right == 2) right = -1;
                if(!hasYoyo && left == 2) left = -1;
                if(!hasYoyo)notHas.draw(screen, 56, 38);
                
                if(!hasSword && right == 3) right = -1;
                if(!hasSword && left == 3) left = -1;
                if(!hasSword)notHas.draw(screen, 80, 38);
                
                if(!hasGun && right == 4) right = -1;
                if(!hasGun && left == 4) left = -1;
                if(!hasGun)notHas.draw(screen, 104, 38);

                //draw
                drawInventory(8, left);
                drawInventory(56, right);

                //draw handSelect
                screen.drawRect(8 + handSelect * 24, 38, 17, 17, 9);

                break;
            case 4:
                screen.setTextColor(11);
                screen.setTextPosition(10, 100);
                screen.print("Game Over...");
                if(Button.C.justPressed()) {
                    restart();
                    state = 0;
                    
                }
                break;
        }

        screen.flush();
    }

    void moveZombies() {
        for (int i = 0; i < zombies.getSize(); i++) {
            if (zombies.getHealth(i) <= 0) {
                zombies.setHealth(i, 10);
                zombies.getZombie(i).x = 222;
                zombies.getZombie(i).y = 60 + Math.random(0, 5) * 24;
                coins++;
            }
            if (zombies.getCooldown(i) > 0) {
                zombies.setCooldown(i, zombies.getCooldown(i) - 1);
                zombies.getZombie(i).hurt();
            } else {
                zombies.getZombie(i).x -= 0.1f;
                zombies.getZombie(i).walk();
            }
            if (zombies.getZombie(i).x < 0) zombies.getZombie(i).x = 220;
        }
    }
    void drawZombies() {
        for (Zombie z: zombies.getAllZombies()) {
            z.draw(screen);
        }
    }

    void moveHero() {

        if (!Button.Up.isPressed() && !Button.Down.isPressed() && !Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed() && cooldown == 0) {
            hero.idle();
            timeToPlant = 0;
        }
        if (Button.Down.justPressed() && hy < 4) {
            hy += 1;
        }
        if (Button.Up.justPressed() && hy > 0) {
            hy -= 1;
        }
        if (Button.Right.justPressed() && hx < 8) {
            hx += 1;
        }
        if (Button.Left.justPressed() && hx > 0) {
            hx -= 1;
        }

        if (Button.A.isPressed() && cooldown == 0) {
            itemAction(left);
        }
        if (Button.B.isPressed() && cooldown == 0) {
            itemAction(right);
        }

        if (cooldown > 0) hero.hurt();

        //Translate to grid
        hero.x = 6 + hx * 24;
        hero.y = 60 + hy * 24;
    }

    void itemAction(int hand) {
        switch (hand) {
            case 0: //planter. Player starts with shovel so always has shovel.
            
                if (!containsPlant() && seeds > 0 && timeToPlant > 45) {
                    timeToPlant = 0;
                    Coffea n = new Coffea();
                    n.x = 6 + hx * 24;
                    n.y = 60 + hy * 24;
                    for (int i = 0; i < plants.length; i++) {
                        if (plants[i] == null) {
                            plants[i] = n;
                            break;
                        }
                    }
                    seeds--;
                } else if (seeds > 0) {
                    timeToPlant++;
                }
                hero.plant();
            
                break;
            case 1: //shovel. Player starts with shovel
                hero.shovel();
                for (int i = 0; i < zombies.getSize(); i++) {
                    if (zombies.getZombie(i).y == hero.y) {
                        if (hit(zombies.getZombie(i).x, hero.x, 26, 8)) {
                            zombies.getZombie(i).x += 6;
                            zombies.setCooldown(i, 80);
                            zombies.setHealth(i, zombies.getHealth(i) - 4);
                        }
                    }
                }
                break;
            case 2: //Yoyo.
                if( !hasYoyo ) break;
                hero.yoyo();
                for (int i = 0; i < zombies.getSize(); i++) {
                    if (zombies.getZombie(i).y == hero.y) {
                        if (hit(zombies.getZombie(i).x, hero.x, 30, 8)) {
                            zombies.getZombie(i).x += 1;
                            zombies.setCooldown(i, 10);
                            zombies.setHealth(i, zombies.getHealth(i) - 1);
                        }
                    }
                }
                break;
            case 3://sword
                if(!hasSword)break;
                hero.sword();
                for (int i = 0; i < zombies.getSize(); i++) {
                    if (zombies.getZombie(i).y == hero.y) {
                        if (hit(zombies.getZombie(i).x, hero.x, 18, 8)) {
                            zombies.getZombie(i).x += 20;
                            zombies.setCooldown(i, 15);
                            zombies.setHealth(i, zombies.getHealth(i) - 10);
                        }
                    }
                }
                break;
            case 4://gun
                if(!hasGun)break;
                break;
            default:
                //do nothing on no item
                break;
        }
    }

    boolean zombieHitPlayer() {
        for (Zombie z: zombies.getAllZombies()) {
            if (z.y == hero.y && z.x <= hero.x + 12 && z.x > hero.x + 3) return true;
        }
        return false;
    }

    boolean hit(float zx, float hx, int adist, int bdist) {
        return zx <= hx + adist && zx >= hx + bdist;
    }

    boolean containsPlant() {
        for (Coffea c: plants) {
            if (c == null) continue;
            if (c.x == 6 + hx * 24 && c.y == 60 + hy * 24) return true;
            // if(c.x == hx && c.y == hy) return true;
        }
        return false;
    }

    void drawLives() {
        switch (state) {
            case 1:
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(8 + i * 24), 0.0f);
                }
                break;
            case 3:
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(12 + i * 24), 80.0f);
                }
                break;
        }
    }

    void drawPlants() {
        for (Coffea c: plants) {
            if (c == null) continue;
            c.idle();
            c.draw(screen);
        }
    }

    void drawInventory(int handX, int item) {
        switch (item) {
            case 0://Planter
                sprout.draw(screen, handX, 8);
                break;
            case 1://Shovel
                shovel.draw(screen, handX, 8);
                break;
            case 2://Yoyo
                yoyo.draw(screen, handX, 8);
                break;
            case 3://sword
                sword.draw(screen, handX, 8);
                break;
            case 4://gun
                gun.draw(screen, handX, 8);
                break;
        }
    }

}