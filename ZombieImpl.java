/**
 * 
 * Container object for all Zombie enemies and related variables. * 
 * 
 */
 
import Math;
import entities.enemies.zombie.Zombie;
import entities.enemies.Death;
import entities.plant.Coffea;
import CoffeaImpl;
import item.Loot;

class ZombieImpl {
    Zombie[] wave;   // array of zombies
    Death[] corpses; // corpse animations for dead zombies
    int[] deadTime;  // timer for dying animation to play
    int[] eCool;     // cooldown for zombie being hit
    int[] health;    // health per zombie
    int[] eating;    // animation for eating
    float[] speeds;  // speed of zombies.
    
    /**
     * constructor takes an integer for starting number of zombies. 
     */
    public ZombieImpl(int start){
        makeWave(start);
    }
    
    /**
     * Makes the next wave using the integer from the constructor.
     * 
     * int amount: used to set the size of the arrays. Maximize at 40 enemies.
     * 
     * wave[i].x = 220; sets the start of each zombie at the edge of the screen.
     * 
     * wave[i].y = 60+Math.random(0,5)*24;  sets the y position to a random row between 0 (top) and 5 (bottom)
     * 
     * speeds[i] = Math.random(0, 2) == 1 ? 0.26f : 0.1f; set the speed based on a random 50/50 chance.
     * 
     */ 
    void makeWave(int amount){
        corpses = new Death[amount];
        deadTime = new int[amount];
        wave = new Zombie[amount];
        
        eCool = new int[amount];
        health = new int[amount];
        eating = new int[amount];
        speeds = new float[amount];
        for(int i = 0; i < amount; i++){
            deadTime[i] = 0;
            wave[i] = new Zombie();
            wave[i].x = 220;
            wave[i].y = 60+Math.random(0,5)*24;
            eCool[i] = 0;
            health[i] = 10;
            eating[i] = 0;
            speeds[i] = Math.random(0, 2) == 1 ? 0.26f : 0.1f;
        }
    }
    
    /**
     * For each zombie
     * 
     * 1. check if the zombie is dying. If yes, update deadTime and corpse.
     * 2. check if zombie is on a plant tile. If the tile is not dead already, update eating. If eating is updated over 100, kill the plant and continue on.
     * 3. check health. If less than 0, kill the zombie and begin corpse/death animation. Create new zombie and start at the beginning point (220). Add to coins.
     * 4. check cooldown after getting hit by player.
     * 5. check if zombie is off screen, wrap around if so.
     * 
     */ 
    int moveZombies(int coins, CoffeaImpl plants, Loot[] tileLoot) {
        for (int i = 0; i < getSize(); i++) {
            // 1.
            if(deadTime[i] > 0){
                deadTime[i]--;
                if(deadTime[i] <= 0){
                    corpses[i] = null;
                }
                continue;
            }
            
            // 3.
            if (getHealth(i) <= 0) {
                corpses[i] = new Death();
                corpses[i].x = getZombie(i).x;
                corpses[i].y = getZombie(i).y;
                corpses[i].explode();
                deadTime[i] = 100;
                
                setHealth(i, 10);
                for(int k = 0; k < 45; k++){
                    if(tileLoot[k] == null){
                        Loot l = new Loot();
                        l.x = 6 + Math.round(getZombie(i).x / 24)*24;
                        if(l.x > 220) l.x = 6 + 8*24;
                        l.y = getZombie(i).y;
                        l.idle();
                        tileLoot[k] = l;
                        break;
                    }
                }
                getZombie(i).x = 222;
                getZombie(i).y = 60 + Math.random(0, 5) * 24;
                coins++;
                continue;
            }
            
            // 2. for each tile in the playField
            for(int j = 0; j < 45; j++){
                //if the plant is null or already dead continue to next plant
                if(plants.getPlant(j) == null) continue;
                if(getZombie(i).y != plants.getPlant(j).y) continue;
                
                //if zombie is on plant tile
                if(getZombie(i).x < plants.getPlant(j).x + 16 
                && getZombie(i).x > plants.getPlant(j).x + 8
                ){
                    if(plants.getState(j) == 5) {
                        eating[i] = 0;
                        continue;
                    }
                    eating[i]++;
                    if(eating[i] == 1) getZombie(i).eat();
                    
                    if(eating[i] > 100){
                        plants.getPlant(j).dead();  
                        plants.setState(5, j);
                        eating[i] = 0;
                    } 
                }
            }
            
            // 4.
            if (getCooldown(i) > 0) {
                setCooldown(i, getCooldown(i) - 1);
                getZombie(i).hurt();
            } else {
                if(eating[i] > 0) continue;
                getZombie(i).x -= speeds[i];
                getZombie(i).walk();
            }
            //5. wrap around the board.
            if (getZombie(i).x < 0) getZombie(i).x = 220;
        }
        return coins;
    }
    
    /**
     *  For each zombie in the wave, check if it is hitting the player.
     */
    boolean zombieHitPlayer(float herox, float heroy) {
        for (Zombie z: wave) {
            if (z.y == heroy && z.x <= herox + 12 && z.x > herox + 3) return true;
        }
        return false;
    }
    
    //generic checkHit used for weapons.
    boolean checkHit(float herox, float heroy, int x, int cooldown, int back, int damage){
        for (int i = 0; i < getSize(); i++) {
            if (getZombie(i).y == heroy) {
                if (hit(getZombie(i).x, herox, x, 8)) {
                    getZombie(i).x += back;
                    setCooldown(i, cooldown);
                    setHealth(i, getHealth(i) - damage);
                    eating[i] = 0;
                    return true;
                }
            }
        }
        return false;
    }
    
    //shovel hit
    boolean checkShovel(float herox, float heroy){
        return checkHit(herox, heroy, 26, 80, 6, 4);
    }
    
    //yoyo hit
    boolean checkYoyo(float herox, float heroy){
        return checkHit(herox, heroy, 30, 10, 1, 1);
    }
    
    //sword hit
    boolean checkSword(float herox, float heroy){
        return checkHit(herox, heroy, 18, 15, 20, 10);
    }
    
    //player hits a zombie
    boolean hit(float zx, float hx, int adist, int bdist) {
        return zx <= hx + adist && zx >= hx + bdist;
    }
    
    //Getter/Setters
    public int getSize(){
        return wave.length;
    }
    
    public Zombie[] getAllZombies(){
        return wave;
    }
    public Death[] getAllDeath(){
        return corpses;
    }
    
    public Zombie getZombie(int index){
        return wave[index];
    }
    public int getCooldown(int index){
        return eCool[index];
    }
    public void setCooldown(int idx, int rate){
        eCool[idx] = rate;
    }
    public int getHealth(int index){
        return health[index];
    }
    public void setHealth(int idx, int h){
        health[idx] = h;
    }

    public int getEating(int idx){
        return eating[idx];
    }
    
    public void setEating(int idx, int e){
        eating[idx] = e;
    }

}