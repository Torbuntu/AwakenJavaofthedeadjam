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
import item.Fruit;
import item.Flower;
import item.Sapling;
import item.Bean;
import item.Ammo;
import item.Coin;
import item.Juice;
import item.Coffee;
import item.Tea;
import item.Loot;

import audio.Select;

import ZombieImpl;
import CoffeaImpl;
import Constants;


class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    Playfield playField;
    Inventory inventoryScreen;
    Shop shop;
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
    
    Fruit fruitIcon;
    Flower flowerIcon;
    Sapling saplingIcon;
    Bean beanIcon;
    Ammo ammoIcon;
    
    Coin coin;
    
    Juice juiceIcon;
    Coffee coffeeIcon;
    Tea teaIcon;
    
    Select selectSound;
    
    //Item drops
    int flower, fruit, beans; //0,1,2
    int juice, tea, coffee;

    //inventory items:
    int coins, saplling;

    int hx, hy, left, right, cooldown, plantCount, timeToPlant, lives, maxLives, waveNum, purchaceSelect, ammo, day;
    int state; //0 = title, 1=game, 2=pre-day, 3=pause/inventory, 4=game-over

    //inventory variables
    int handSelect; //0=left, 1=right
    
    float time;
    
    boolean hasYoyo, hasSword, hasGun;
    
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
        coin = new Coin();
        
        juiceIcon = new Juice();
        coffeeIcon = new Coffee();
        teaIcon = new Tea();
        
        fruitIcon = new Fruit();
        flowerIcon = new Flower();
        saplingIcon = new Sapling();
        beanIcon = new Bean();
        
        heart = new Heart();
        notHas = new NotHas();
        ammoIcon = new Ammo();
        
        selectSound = new Select(0);
        
        
        restart();
        
        
        // Initialize the Mixer at 8khz
        Mixer.init(8000);
    }
    
    void restart(){
        hx = 1;
        hy = 1;
        time = 0.0f;
        day = 1;
        
        left = 0; //planter
        right = 1; //shovel
        
        waveNum = 2;
        zombies = new ZombieImpl(waveNum);
        plants = new CoffeaImpl();

        lives = 3;
        maxLives = 3;
        coins = 0;
        saplling = 1; //Start you off with one lonely seed. Don't screw it up! :D
        timeToPlant = 0;
        handSelect = 0;
        
        hasYoyo = false;
        hasGun = false;
        hasSword = false;
        purchaceSelect = 0;
        ammo = 0;
        flower = 0;
        fruit = 0;
        beans = 0;
        
        juice = 0;
        tea = 0;
        coffee = 0;
        
        message = "";
        
        //tmp bullet
        shooting = false;
        
        tileLoot = new Loot[45];
    }

    // Might help in certain situations
    void shutdown() {
        screen = null;
    }

    // update is called by femto.Game every frame
    void update() {
        screen.clear(0);
        switch (state) {
            case 0://title screen
                if (Button.C.justPressed()) {
                    selectSound.play();
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
                screen.print(Constants.PRESS_C_TO_PLAY);

                for (int i = 0; i < 15; i++) {
                    screen.setTextColor(i);
                    screen.setTextPosition(110, i * 8);
                    screen.print("(" + i + ")");
                }
                break;
            case 1: //Game play screen
                playField.draw(screen, 0.0f, 0.0f);
                
                if (cooldown > 0) cooldown--;
                
                if( shooting ){
                    updateBullet();
                    screen.fillCircle(bx, by, 2, 1);
                }
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
                
                moveHero();
                
                drawPlants();
                
                hero.draw(screen);
                
                drawZombies();
                drawLives();
                drawLoot();

                drawQuantities();
                
                screen.setTextPosition(0, 16);
                screen.print(Constants.DAY+day);
                
                if (Button.C.justPressed()) state = 3;
                
                //Day meter
                updateTime();
                screen.drawLine(0.0f, 12.0f, time, 12.0f, 14, false);

                break;
            case 2://Shop screen
                shop.draw(screen, 0.0f, 0.0f);
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
                                ammo += 5;
                                message = Constants.PURCHASED_AMMO;
                            }else{
                                message = Constants.NOT_ENOUGH_COIN_AMMO;
                            }
                            break;
                        case 2://health
                            if(coins >= 500 ){
                                coins -= 500;
                                if(maxLives < 9){
                                    maxLives++;
                                }
                                if(lives < maxLives){
                                    lives++;
                                }
                                message = Constants.PURCHASED_EXTRA_LIFE;
                            }else{
                                if(maxLives == 9){
                                    message = Constants.MAX_LIVES_REACHED;
                                }else{
                                    message = Constants.NOT_ENOUGH_COIN_LIVES;
                                }
                            }
                            break;
                        case 3://Yoyo
                            if (!hasYoyo && coins >= 500) {
                                hasYoyo = true;
                                coins -= 500;
                                message = Constants.PURCHASED_YOYO;
                            }else{
                                if(hasYoyo){
                                } else{
                                    message = Constants.NOT_ENOUGH_COIN_YOYO;
                                }
                            }
                            break;
                        case 4://Sword
                            if(!hasSword && coins >= 750) {
                                hasSword = true;
                                coins -= 750;
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
                            if(!hasGun && coins >= 1000 ){
                                hasGun = true;
                                coins -= 1000;
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
                    waveNum += 2;
                    zombies = new ZombieImpl(waveNum);
                }
                
                if(!hasYoyo)notHas.draw(screen, 50, 90);
                if(!hasSword)notHas.draw(screen, 50, 116);
                if(!hasGun){
                    notHas.draw(screen, 50, 38);
                    notHas.draw(screen, 50, 142);
                }

                screen.setTextColor(11);
                screen.setTextPosition(109, 20);
                screen.print(Constants.X+coins);
                coin.draw(screen, 100, 20);
                
                screen.setTextPosition(0, 170);
                screen.print(Constants.C_TO_START_NEXT_DAY);
                
                screen.setTextPosition(110, 92);
                screen.print(Constants.JUICE + juice);
                juiceIcon.draw(screen, 100, 90);
                
                screen.setTextPosition(110, 102);
                screen.print(Constants.TEA + tea);
                teaIcon.draw(screen, 100, 100);
                
                screen.setTextPosition(110, 112);
                screen.print(Constants.COFFEE+coffee);
                coffeeIcon.draw(screen, 100, 110);
                
                screen.setTextPosition(110, 128);
                screen.print(Constants.FRUIT+ fruit);
                fruitIcon.draw(screen, 100, 126);
                
                screen.setTextPosition(110, 138);
                screen.print(Constants.FLOWER + flower);
                flowerIcon.draw(screen, 100, 136);
                
                screen.setTextPosition(110, 148);
                screen.print(Constants.BEANS+beans);
                beanIcon.draw(screen, 100, 146);
                
                screen.setTextColor(9);
                screen.setTextPosition(0, 0);
                screen.print(message);
                
                //draw purchaceSelect
                screen.drawRect(50, 12 + purchaceSelect * 26, 17, 17, 9);
                drawPrices();
                break;
            case 3:// Inventory Screen
                inventoryScreen.draw(screen, 0.0f, 0.0f);
                drawLives();
                if (Button.C.justPressed()){
                    selectSound.play();
                    state = 1;  
                } 
                if (Button.Left.justPressed() && handSelect > 0) handSelect--;
                if (Button.Right.justPressed() && handSelect < 7) handSelect++;

                if(Button.B.justPressed()){
                    if(handSelect > 4){
                        switch(handSelect){
                            case 5:
                                if(fruit >= 5){
                                    fruit-=5;
                                    juice++;
                                }
                                break;
                            case 6:
                                if(flower >= 5){
                                    flower-=5;
                                    tea++;
                                }
                                break;
                            case 7:
                                if(beans >= 5){
                                    beans-=5;
                                    coffee++;
                                }
                                break;
                        }  
                    }else{
                        if(right != handSelect && handSelect < 5) left = handSelect;
                    }
                }
                if (Button.A.justPressed() && left != handSelect && handSelect < 5) right = handSelect;
                
                if(!hasYoyo && right == 2) right = -1;
                if(!hasYoyo && left == 2) left = -1;
                if(!hasYoyo)notHas.draw(screen, 8+24*2, 38);
                
                if(!hasSword && right == 3) right = -1;
                if(!hasSword && left == 3) left = -1;
                if(!hasSword)notHas.draw(screen, 8+24*3, 38);
                
                if(!hasGun && right == 4) right = -1;
                if(!hasGun && left == 4) left = -1;
                if(!hasGun)notHas.draw(screen, 8+24*4, 38);
                
                screen.setTextPosition(0, 160);
                if(handSelect == 5 && fruit >= 5) screen.print(Constants.PRESS_B_TO_CRAFT + Constants.J);
                if(handSelect == 6 && flower >= 5) screen.print(Constants.PRESS_B_TO_CRAFT + Constants.T);
                if(handSelect == 7 && beans >= 5) screen.print(Constants.PRESS_B_TO_CRAFT + Constants.C);
                
                
                screen.setTextPosition(10, 92);
                screen.print(Constants.JUICE + juice);
                juiceIcon.draw(screen, 0, 90);
                
                screen.setTextPosition(10, 102);
                screen.print(Constants.TEA + tea);
                teaIcon.draw(screen, 0, 100);
                
                screen.setTextPosition(10, 112);
                screen.print(Constants.COFFEE+coffee);
                coffeeIcon.draw(screen, 0, 110);
                
                screen.setTextPosition(10, 128);
                screen.print(Constants.FRUIT+ fruit);
                fruitIcon.draw(screen, 0, 126);
                
                screen.setTextPosition(10, 138);
                screen.print(Constants.FLOWER + flower);
                flowerIcon.draw(screen, 0, 136);
                
                screen.setTextPosition(10, 148);
                screen.print(Constants.BEANS+beans);
                beanIcon.draw(screen, 0, 146);
                
                //draw
                drawInventory(8, left);
                drawInventory(56, right);

                //draw handSelect
                screen.drawRect(8 + handSelect * 24, 38, 17, 17, 9);

                break;
            case 4:
                screen.setTextColor(11);
                screen.setTextPosition(10, 100);
                screen.print(Constants.GAME_OVER);
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
                    timeToPlant = 0;
                    plants.plantSeed(hx, hy);
                    saplling--;
                }else if(plants.tileContainsPlant(hx, hy) ){
                    switch(plants.tileContainsItem(hx, hy)){
                        case 0:
                            flower++;
                            break;
                        case 1:
                            fruit++;
                            break;
                        case 2:
                            beans++;
                            break;
                        default:
                            break;
                    }
                } else if (saplling > 0) {
                    timeToPlant++;
                }
                hero.plant();
                break;
            case 1: //shovel. Player starts with shovel
                hero.shovel();
                zombies.checkShovel(hero.x, hero.y);
                break;
            case 2: //Yoyo.
                if( !hasYoyo ) break;
                hero.yoyo();
                zombies.checkYoyo(hero.x, hero.y);
                break;
            case 3://sword
                if(!hasSword)break;
                hero.sword();
                zombies.checkSword(hero.x, hero.y);
                break;
            case 4://gun
                if(!hasGun || ammo == 0 || shooting)break;
                hero.shoot();
                shooting = true;
                ammo--;
                bx = (int)hero.x + 6;
                by = (int)hero.y + 5;
                
                break;
            default:
                //do nothing on no item
                break;
        }
    }
    
    void updateBullet(){
        for (int i = 0; i < zombies.getSize(); i++) {
            if (zombies.getZombie(i).x > hero.x && bx + 1 >= zombies.getZombie(i).x && by >= zombies.getZombie(i).y && by <= zombies.getZombie(i).y+8 ) {
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
            case 1:
                for(int j = 0; j < maxLives; j++){
                    screen.drawRect((j * 12), 0, 9, 9, 7);
                }
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(1 + i * 12), 1.0f);
                }
                break;
            case 3:
                for (int i = 0; i < lives; i++) {
                    heart.draw(screen, (float)(12 + i * 12), 80.0f);
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
    
    //draws the quantities of items in game screen
    void drawQuantities(){
        screen.setTextColor(11);

        screen.setTextPosition(10, 27);
        screen.print(Constants.X + coins);
        coin.draw(screen, 0, 24);
        
        screen.setTextPosition(10, 38);
        screen.print(Constants.X+ammo);
        ammoIcon.draw(screen, 0, 36);
        
        screen.setTextPosition(121, 10);
        screen.print(Constants.X + saplling);
        saplingIcon.draw(screen, 112, 8);
        
        screen.setTextPosition(121, 34);
        screen.print(Constants.X + flower);
        flowerIcon.draw(screen, 112, 30);
        
        screen.setTextPosition(169, 10);
        screen.print(Constants.X + fruit);
        fruitIcon.draw(screen, 160, 8);
        
        screen.setTextPosition(169, 34);
        screen.print(Constants.X + beans);
        beanIcon.draw(screen, 160, 30);
    }
    
    //Draws the prices of items in the shop screen
    void drawPrices(){
        coin.draw(screen, 1, 16);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18);
        screen.print(Constants.X + Constants.FIVE);//seed
        
        coin.draw(screen, 1, 16 + 1 * 26);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18 + 1 * 26);
        screen.print(Constants.X + Constants.TEN);//ammo
        
        coin.draw(screen, 1, 16 + 2 * 26);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18 + 2 * 26);
        screen.print(Constants.X + Constants.FIVE_HUNDRED);//health
        
        coin.draw(screen, 1, 16 + 3 * 26);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18 + 3 * 26);
        screen.print(Constants.X + Constants.FIVE_HUNDRED);//yoyo
        
        coin.draw(screen, 1, 16 + 4 * 26);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18 + 4 * 26);
        screen.print(Constants.X + Constants.SEVEN_FIFTY);//sword
        
        coin.draw(screen, 1, 16 + 5 * 26);
        screen.setTextColor(11);
        screen.setTextPosition(9, 18 + 5 * 26);
        screen.print(Constants.X + Constants.ONE_THOUSAND);//gun
    }
    
    void drawLoot(){
        for(Loot l : tileLoot){
            if(l == null) continue;
            l.draw(screen);
        }
    }

}