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