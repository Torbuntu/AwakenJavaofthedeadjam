import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.Castpixel16;
import femto.font.TIC80;
import femto.sound.Mixer;

import Math;
import backgrounds.Playfield;
import backgrounds.Inventory;
import backgrounds.Shop;
import backgrounds.Title;
import backgrounds.WinGame;
import backgrounds.GameOver;

import entities.hero.Hero;
import entities.enemies.zombie.Zombie;
import entities.enemies.Death;
import entities.plant.Coffea;

import item.Heart;
import item.Sprout;
import item.Shovel;
import item.Yoyo;
import item.Sword;
import item.Gun;
import item.NotHas;
import item.Loot;

import audio.Select;
import audio.LootPickup;
import audio.Hit;
import audio.Planted;
import audio.ShootSound;

import ZombieImpl;
import CoffeaImpl;
import Constants;


public class TitleScreenState extends State {
    Title titleScreen;
    Hero hero;
    int bx;
    boolean shooting;
    int status = 4;

    Select selectSound;
    
    void init(){
        titleScreen = new Title();
        hero = new Hero();
        shooting = false;
        selectSound = new Select(0);
        Mixer.init(8000);
    }
    
    void update(){
        var screen = Main.screen;
        titleScreen.draw(screen, 0.0f, 0.0f);
        if (Button.C.justPressed()) {
            selectSound.play();
            Main.state = 1;
            shooting = false;
            Game.changeState(new Main());
        }
        if(Button.Left.justPressed()){
            Main.hardMode = false;
            Main.mode = 1;
        }
        if(Button.Right.justPressed()){
            Main.hardMode = true;
            Main.mode = 2;
        }
        
        if(Button.B.justPressed() || Button.A.justPressed()){
            int r = Math.random(0, 4);
            if(r == status){
                if(status >= 3){
                    status = 0;
                }else{
                    status++;
                }
            }else{
                status = r;
            }
            shooting = false;
            switch(status){
                case 0:
                    hero.shovel();
                    break;
                case 1:
                    hero.shoot();
                    shooting = true;
                    bx = 100;
                    break;
                case 2:
                    hero.yoyo();
                    break;
                case 3:
                    hero.sword();
                    break;
                default:
                    hero.idle();
                    break;
            }
        }
        
        if(shooting){
            bx++;
            if(bx > 180) bx = 100;
            screen.fillCircle(bx, 150, 2, 1);
        }

        hero.draw(screen, 80.0f, 140.0f);

        screen.setTextColor(11);
        
        screen.setTextPosition(63, 130);
        screen.print(Constants.PRESS_C_TO_PLAY);
        
        screen.setTextPosition(78, 120);
        screen.println("< - mode - >");
        
        screen.setTextColor(10);
        if(Main.hardMode){
            screen.setTextPosition(148, 120);
            screen.print("Hard!");
        }else{
            screen.setTextPosition(34, 120);
            screen.println("Normal.");
        }
        
        screen.flush();
    }
}

public class Main extends State {

    static final HiRes16Color screen = new HiRes16Color(Castpixel16.palette(), TIC80.font()); // the screenmode we want to draw with

    static boolean hasYoyo = false, hasSword = false, hasGun = false, hardMode = false;
    static int mode = 1, state = 0, ammo = 0, coins = 0, fruit = 0, saplling = 1, maxLives = 3, waveNum = 2;
    int day = 1;
    
    Playfield playField;
    Inventory inventoryScreen;
    Shop shop;
    WinGame winGameScreen;
    GameOver gameOverScreen;
    
    Hero hero;

    Heart heart;
    NotHas notHas;
    CoffeaImpl plants;//0 = planted, 1 = day 1, 2 = day 2, 3 = day 3, 4 = harvestable, 5 = destroyed
    ZombieImpl zombies;
    Shovel shovel;
    Sprout sprout;
    Yoyo yoyo;
    Sword sword;
    Gun gun;
    
    Select selectSound;
    LootPickup lootSound;
    Hit hitSound;
    Planted plantSound;
    ShootSound shootSound;
    
    int hx, hy, left, right, cooldown, plantCount, timeToPlant, lives, purchaceSelect, handSelect;
    // handSelect: 0=left, 1=right
    
    float time;
    
    String message;

    //tmp bullet stuff
    int bx, by;
    boolean shooting;
    
    Loot[] tileLoot;//0 = empty, 1 = 5 coins, 2 = seed

    // start the game using Main as the initial state
    public static void main(String[] args) {
        Game.run(TIC80.font(), new Main());
    }

    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init() {
        playField = new Playfield();
        inventoryScreen = new Inventory();
        winGameScreen = new WinGame();
        gameOverScreen = new GameOver();
        
        shop = new Shop();
        hero = new Hero();
        hero.idle();
        shovel = new Shovel();
        sprout = new Sprout();
        yoyo = new Yoyo();
        sword = new Sword();
        gun = new Gun();
        
        heart = new Heart();
        notHas = new NotHas();
        
        selectSound = new Select(0);
        lootSound = new LootPickup(0);
        hitSound = new Hit(1);
        plantSound = new Planted(1);
        shootSound = new ShootSound(2);
        
        restart();
        
        // Initialize the Mixer at 8khz
        Mixer.init(8000);
    }
    
    void restart(){
        hx = 1;
        hy = 1;
        time = 0.0f;
        
        left = 0; //planter
        right = 1; //shovel
        
        zombies = new ZombieImpl(waveNum);
        plants = new CoffeaImpl();

        lives = 3;
        timeToPlant = 0;
        handSelect = 0;
        
        hasYoyo = false;
        hasGun = false;
        hasSword = false;
        purchaceSelect = 0;
        
        message = "";
        
        //tmp bullet
        shooting = false;
        
        tileLoot = new Loot[45];
        hero.idle();
        ammo = 0;
        coins = 0;
        fruit = 0;
        saplling = 1;
        maxLives = 3;
        waveNum = 2;
    }

    // update is called by femto.Game every frame
    void update() {
        screen.clear(0);
        switch (state) {
            case 0://title screen
                Game.changeState(new TitleScreenState());
                break;
            case 1: //Game play screen
                playField.draw(screen, 0.0f, 0.0f);
                
                if (cooldown > 0) cooldown--;
             
                //move zombies and add coins for kills
                coins = zombies.moveZombies(coins, plants, tileLoot);
                
                if (zombies.zombieHitPlayer(hero.x, hero.y) && cooldown == 0) {
                    lives--;
                    if(lives == 0) {
                        state = 4;
                        break;   
                    }
                    cooldown = 100;
                }
                
                if (Button.C.justPressed()) state = 3;
                
                moveHero();
                updateTime();
                
                if( shooting ){
                    updateBullet();
                    screen.fillCircle(bx, by, 2, 1);
                }
                
                //draw equipped inventories
                drawInventory(8, left);
                drawInventory(56, right);
                
                drawPlants();
                
                hero.draw(screen);
                
                drawZombies();
                drawLives();
                drawLoot();

                drawQuantities();
                
                screen.setTextPosition(102, 0);
                screen.print(Constants.X + day);
                
                //Day meter
                screen.drawLine(0.0f, 1.0f, time, 1.0f, 14, false);

                break;
            case 2://Shop screen
                shop.draw(screen, 0.0f, 0.0f);
                
                if(purchaceSelect < 0 && (Button.Down.justPressed() || Button.Up.justPressed())) purchaceSelect = 0;
                if(Button.Down.justPressed() && purchaceSelect < 5) purchaceSelect++;
                if(Button.Up.justPressed() && purchaceSelect > 0)purchaceSelect--;
                
                if (Button.A.justPressed()) {
                    switch(purchaceSelect){
                        case 0://saplling
                            if(coins >= 5){
                                coins -= 5;
                                saplling++;
                                message = Constants.PURCHASE_SEED_FOR_COINS;
                            }else{
                                message = Constants.NOT_ENOUGH_COIN_SAPLING;
                            }
                            break;
                        case 1://ammo
                            if(hasGun && coins >= 10){
                                coins -= 10;
                                ammo += 15;
                                message = Constants.PURCHASED_AMMO;
                            }else{
                                if(hasGun){
                                    message = Constants.NOT_ENOUGH_COIN_AMMO;
                                }else{
                                    message = "You do not own the Gun.";
                                }
                            }
                            break;
                        case 2://health
                            if(coins >= 50 ){
                                
                                if(hardMode && (maxLives < 4) || !hardMode && (maxLives < 9)){
                                    maxLives++;
                                }
                                if(lives < maxLives){
                                    lives++;
                                    coins -= 50;
                                    message = Constants.PURCHASED_EXTRA_LIFE;
                                }else{
                                    message = Constants.MAX_LIVES_REACHED;
                                }
                                
                            }else{
                                if(hardMode && maxLives == 4 || maxLives == 9){
                                    message = Constants.MAX_LIVES_REACHED;
                                }else{
                                    message = Constants.NOT_ENOUGH_COIN_LIVES;
                                }
                            }
                            break;
                        case 3://Yoyo
                            if (!hasYoyo && coins >= 50) {
                                hasYoyo = true;
                                coins -= 50;
                                message = Constants.PURCHASED_YOYO;
                            }else{
                                if(hasYoyo){
                                } else{
                                    message = Constants.NOT_ENOUGH_COIN_YOYO;
                                }
                            }
                            break;
                        case 4://Sword
                            if(!hasSword && coins >= 150) {
                                hasSword = true;
                                coins -= 150;
                                message = Constants.PURCHASED_SWORD;
                            }else{
                                if(hasSword){
                                    message = Constants.ALREADY_OWN_SWORD;
                                } else{
                                    message = Constants.NOT_ENOUGH_COIN_SWORD;
                                }
                            }
                            break;
                        case 5://Gun
                            if(!hasGun && coins >= 250 ){
                                hasGun = true;
                                coins -= 250;
                                message = Constants.PURCHASED_GUN;
                            }else{
                                if(hasGun){
                                    message = Constants.ALREADY_OWN_GUN;
                                } else{
                                    message = Constants.NOT_ENOUGH_COIN_GUN;
                                }
                            }
                            
                            break;
                        default:
                        break;
                    }
                }
                
                if (Button.C.justPressed()) {
                    selectSound.play();
                    state = 1;
                    if(waveNum < 20) waveNum += 2;
                    zombies = null;
                    tileLoot = null;
                    plants.cleanupDeadPlants();
                    System.gc();
                    zombies = new ZombieImpl(waveNum);
                    tileLoot = new Loot[45];
                    System.gc();
                    break;
                }
                
                //draw fruit meter
                screen.drawCircle(125, 60, 25, 5, false);
                if(fruit >= 25*mode) {
                    screen.fillCircle(125, 60, 25, 7, false);   
                }else{
                    screen.fillCircle(125, 60, fruit/mode, 1, false);
                }
            
                drawShopText();
                
                //draw purchaceSelect
                screen.drawRect(50, 12 + purchaceSelect * 26, 17, 17, 9);
                drawPrices();
                break;
            case 3:// Inventory Screen
                inventoryScreen.draw(screen, 0.0f, 0.0f);
                
                // Continue
                if (Button.C.justPressed()){
                    selectSound.play();
                    state = 1; 
                    break;
                } 
                
                if (Button.Left.justPressed() && handSelect > 0) handSelect--;
                if (Button.Right.justPressed() && handSelect < 5) handSelect++;

                if(Button.B.justPressed() && right != handSelect){
                    switch(handSelect){
                        case 2:
                            if(hasYoyo){
                                left = handSelect;
                            }
                            break;
                        case 3:
                            if(hasSword){
                                left = handSelect;
                            }
                            break;
                        case 4:
                            if(hasGun){
                                left = handSelect;
                            }
                            break;
                        case 5:
                            if(fruit >= 25*mode){
                                state = 5;
                            }
                            break;
                        default:
                            left = handSelect;
                            break;
                    }
                }
                if (Button.A.justPressed() && left != handSelect && handSelect < 5) {
                    switch(handSelect){
                        case 2:
                            if(hasYoyo){
                                right = handSelect;
                            }
                            break;
                        case 3:
                            if(hasSword){
                                right = handSelect;
                            }
                            break;
                        case 4:
                            if(hasGun){
                                right = handSelect;
                            }
                            break;
                        default:
                            right = handSelect;
                            break;
                    }
                }
                
                drawInventoryImages();
                drawInventoryText();
                
                break;
            case 4://GAME OVER
                gameOverScreen.draw(screen, 0,0);
                zombies.moveZombies(coins, plants, tileLoot);
                drawZombies();
                screen.setTextColor(11);
                screen.setTextPosition(78, 100);
                screen.print(Constants.GAME_OVER);
                if(Button.C.justPressed()) {
                    restart();
                    state = 0;
                }
                break;
            case 5://WIN
                winGameScreen.draw(screen, 0, 0);
                hero.sword();
                hero.draw(screen, 90, 30);
                screen.setTextColor(11);
                screen.setTextPosition(30, 70);
                screen.print("YOU WIN! The World is saved!");
                screen.setTextPosition(30, 80);
                screen.print("Days: " + day);
                screen.setTextPosition(30, 90);
                screen.print("You crafted the cure!");
                if(Button.C.justPressed()) {
                    restart();
                    state = 0;
                }
                break;
        }

        screen.flush();
    }
    
    void updateTime(){
        time += 0.05f;
        if (time >= 100) {
            time = 0.0f;
            message = "";
            plants.updatePlants();
            state = 2;
            purchaceSelect = -2;//no select
            day++;
        }
    }

    void moveHero() {

        if (!Button.Up.isPressed() && !Button.Down.isPressed() && !Button.Right.isPressed() && !Button.Left.isPressed() && !Button.A.isPressed() && !Button.B.isPressed() && cooldown == 0) {
            hero.idle();
            timeToPlant = 0;
        }
        if (Button.Down.justPressed() && hy < 4) { 
            hy += 1;
            timeToPlant = 0;
        }
        if (Button.Up.justPressed() && hy > 0) {
            hy -= 1;
            timeToPlant = 0;
        }
        if (Button.Right.justPressed() && hx < 8)  {
            hx += 1;
            timeToPlant = 0;
        }
        if (Button.Left.justPressed() && hx > 0) {
            hx -= 1;
            timeToPlant = 0;
        }
        
        if (Button.A.isPressed() && cooldown == 0) itemAction(right);
        if (Button.B.isPressed() && cooldown == 0)  itemAction(left);

        if (cooldown > 0) hero.hurt();

        //Translate to grid
        hero.x = 6 + hx * 24;
        hero.y = 60 + hy * 24;
        
        for(int i = 0; i < 45; i++){
            if(tileLoot[i] == null) continue;
            if(tileLoot[i].x == hero.x && tileLoot[i].y == hero.y){
                tileLoot[i] = null;
                lootSound.play();
                int t = Math.random(0, 6);
                switch(t){
                    case 1:
                        coins += 5;
                        break;
                    case 2:
                        saplling++;
                        break;
                    case 5:
                        if(lives + 1 <= maxLives){
                            lives++;
                        }else{
                            coins += 10;
                        }
                        break;
                    default:
                        coins++;
                        break;
                }
            }
        }
    }

    void itemAction(int hand) {
        switch (hand) {
            case 0: //planter. Player starts with planter so always has planter. Plants and harvests crops.
            
                if (!plants.tileContainsPlant(hx, hy) && saplling > 0 && timeToPlant > 45) {
                    plantSound.play();
                    timeToPlant = 0;
                    plants.plantSeed(hx, hy);
                    saplling--;
                }else if(plants.tileContainsPlant(hx, hy) ){
                    fruit += plants.tileContainsItem(hx, hy);
                } else if (saplling > 0) {
                    timeToPlant++;
                }
                hero.plant();
                break;
            case 1: //shovel. Player starts with shovel
                hero.shovel();
                if(zombies.checkShovel(hero.x, hero.y)){
                    hitSound.play();
                }
                break;
            case 2: //Yoyo.
                if( !hasYoyo ) break;
                hero.yoyo();
                if(zombies.checkYoyo(hero.x, hero.y)){
                    hitSound.play();   
                }
                break;
            case 3://sword
                if(!hasSword)break;
                hero.sword();
                if(zombies.checkSword(hero.x, hero.y)){
                    hitSound.play();
                }
                break;
            case 4://gun
                if(!hasGun || ammo == 0 || shooting)break;
                hero.shoot();
                shooting = true;
                ammo--;
                shootSound.play();
                bx = (int)hero.x + 20;
                by = (int)hero.y + 10;
                
                break;
            default:
                //do nothing on no item
                break;
        }
    }
    
    void updateBullet(){
        for (int i = 0; i < zombies.getSize(); i++) {
            if (zombies.getZombie(i).x > hero.x && bx + 1 >= zombies.getZombie(i).x && by >= zombies.getZombie(i).y && by <= zombies.getZombie(i).y+14 ) {
                zombies.setHealth(i, 0);
                shooting = false;
                return;
            }else{
                bx++;
            }
            if(bx > 240) {
                shooting = false;
            }
        }
    }

    void drawLives() {
        switch (state) {
            case 1://play field
                screen.drawRect(6, 32, maxLives*12, 9, 7, false);
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(8 + i * 12), 33.0f);
                }
                break;
            case 3://inventory screen
                screen.drawRect(6, 63, maxLives*12, 9, 7, false);
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(8 + i * 12), 64.0f);
                }
                break;
        }
    }

    void drawPlants() {
        for (Coffea c: plants.getAllPlants()) {
            if (c == null) continue;
            c.draw(screen);
        }
    }
    
    void drawZombies() {
        for (Death d : zombies.getAllDeath()){
            if(null == d) continue;
            d.draw(screen);
        }
        for (Zombie z: zombies.getAllZombies()) {
            z.draw(screen);
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
    
    void drawInventoryImages(){
        drawLives();

        //draw fruit meter
        screen.drawCircle(185, 35, 25, 5, false);
        if(fruit >= 25*mode) {
            screen.fillCircle(185, 35, 25, 7, false);   
        }else{
            screen.fillCircle(185, 35, fruit/mode, 1, false);
        }
        
        //draw
        drawInventory(8, left);
        drawInventory(56, right);
        
        //Draw question for notHas items.
        if(!hasYoyo)notHas.draw(screen, 8+24*2, 38);
        if(!hasSword)notHas.draw(screen, 8+24*3, 38);
        if(!hasGun)notHas.draw(screen, 8+24*4, 38);
        
        //draw handSelect
        screen.drawRect(8 + handSelect * 24, 38, 17, 17, 9);
    }
    
    void drawInventoryText(){
        screen.setTextPosition(6, 160);
        switch(handSelect){
            case 0:
                screen.print("Equip the Planter.");
                break;
            case 1:
                screen.print("Equip the Shovel.");
                break;
            case 2:
                if(hasYoyo){
                    screen.print("Equip the Yoyo.");
                }else{
                    screen.print("You do not yet own the Yoyo.");
                }
                break;
            case 3:
                if(hasSword){
                    screen.print("Equip the Sword.");
                }else{
                    screen.print("You do not yet own the Sword.");
                }
                break;
            case 4:
                if(hasGun){
                    screen.print("Equip the Gun.");
                }else{
                    screen.print("You do not yet own the Gun.");
                }
            default:
                break;
        }
    
        if(handSelect == 5 ) {
           if(fruit >= 25*mode){
               screen.print(Constants.PRESS_B_TO_CRAFT);
           } else {
               screen.print(Constants.FRUIT_TO_WIN + ((25*mode) - fruit));
           }
        }

        screen.setTextPosition(18, 81);
        screen.print(Constants.FRUIT+ fruit);
    }
    
    //draws the quantities of items in game screen
    void drawQuantities(){
        screen.setTextColor(11);

        screen.setTextPosition(150, 8);
        screen.print(Constants.X + coins);
        
        screen.setTextPosition(150, 18);
        screen.print(Constants.X + saplling);
        
        screen.setTextPosition(150, 28);
        screen.print(Constants.X + fruit);
                
        screen.setTextPosition(150, 38);
        screen.print(Constants.X+ammo);
    }
    
    void drawShopText(){
        //Draw current coin amount
        screen.setTextColor(11);
        screen.setTextPosition(109, 12);
        screen.print(Constants.X+coins);
        
        //Draw fruit text.
        screen.setTextPosition(110, 22);
        screen.print(Constants.FRUIT+ fruit);
        
        //Start next day message
        screen.setTextPosition(0, 170);
        screen.print(Constants.C_TO_START_NEXT_DAY);
        
        //Info message draw
        screen.setTextColor(9);
        screen.setTextPosition(0, 0);
        screen.print(message);
    }
    
    //Draws the prices of items in the shop screen
    void drawPrices(){
        screen.setTextColor(11);
        screen.setTextPosition(10, 18);
        screen.print(Constants.X + Constants.FIVE);//seed
        
        screen.setTextPosition(10, 18 + 1 * 26);
        screen.print(Constants.X + Constants.TEN);//ammo
        
        screen.setTextPosition(10, 18 + 2 * 26);
        screen.print(Constants.X + Constants.FIFTY);//health
        
        screen.setTextPosition(10, 18 + 3 * 26);
        screen.print(Constants.X + Constants.FIFTY);//yoyo
        
        screen.setTextPosition(10, 18 + 4 * 26);
        screen.print(Constants.X + Constants.ONE_HUNDRED_FIFTY);//sword
        
        screen.setTextPosition(10, 18 + 5 * 26);
        screen.print(Constants.X + Constants.TWO_HUNDRED_FIFTY);//gun
    }
    
    void drawLoot(){
        for(Loot l : tileLoot){
            if(l == null) continue;
            l.draw(screen);
        }
    }

}