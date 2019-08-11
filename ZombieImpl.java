import Math;
import entities.enemies.zombie.Zombie;

class ZombieImpl {
    int[] eCool;
    Zombie[] wave;
    int[] health;
    
    public ZombieImpl(int start){
        makeWave(start);
    }
    
    void makeWave(int amount){
        wave = new Zombie[amount];
        eCool = new int[amount];
        health = new int[amount];
        for(int i = 0; i < amount; i++){
            wave[i] = new Zombie();
            wave[i].x = 220;
            wave[i].y = 60+Math.random(0,5)*24;
            eCool[i] = 0;
            health[i] = 10;
        }
    }
    
    //updates the zombie positions and manages coin drops
    int moveZombies(int coins) {
        for (int i = 0; i < getSize(); i++) {
            if (getHealth(i) <= 0) {
                setHealth(i, 10);
                getZombie(i).x = 222;
                getZombie(i).y = 60 + Math.random(0, 5) * 24;
                coins++;
            }
            if (getCooldown(i) > 0) {
                setCooldown(i, getCooldown(i) - 1);
                getZombie(i).hurt();
            } else {
                getZombie(i).x -= 0.1f;
                getZombie(i).walk();
            }
            if (getZombie(i).x < 0) getZombie(i).x = 220;
        }
        return coins
    }
    
    void checkHit(float herox, float heroy, int x, int cooldown, int back, int damage){
        for (int i = 0; i < getSize(); i++) {
            if (getZombie(i).y == heroy) {
                if (hit(getZombie(i).x, herox, x, 8)) {
                    getZombie(i).x += back;
                    setCooldown(i, cooldown);
                    setHealth(i, getHealth(i) - damage);
                }
            }
        }
    }
    
    //shovel hit
    void checkShovel(float herox, float heroy){
        checkHit(herox, heroy, 26, 80, 6, 4);
    }
    
    //yoyo hit
    void checkYoyo(float herox, float heroy){
        checkHit(herox, heroy, 30, 10, 1, 1);
    }
    
    //sword hit
    void checkSword(float herox, float heroy){
        checkHit(herox, heroy, 18, 15, 20, 10);
    }
    
    //gun hit
    
    
    
    boolean hit(float zx, float hx, int adist, int bdist) {
        return zx <= hx + adist && zx >= hx + bdist;
    }
    
    public int getSize(){
        return wave.length;
    }
    
    public Zombie[] getAllZombies(){
        return wave;
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

}