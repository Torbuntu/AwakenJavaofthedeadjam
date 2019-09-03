/**
* Container for all coffea plants and  helper variables
* 
* 0 = planted, 1 = day 1, 2 = day 2, 3 = day 3, 4 = harvestable, 5 = destroyed
*/

import entities.plant.Coffea;
import Math;

class CoffeaImpl{
    Coffea[] plants;
    int[] plantStates;
    
    CoffeaImpl(){
        plants = new Coffea[45];
        plantStates = new int[45];
    }
    
    /**
     * Plants a new plant at the given x,y coords
     * 
     */ 
    void plantSeed(int x, int y){
        Coffea n = new Coffea();
        n.x = 6 + x * 24;
        n.y = 60 + y * 24;
        n.idle();
        for (int i = 0; i < plants.length; i++) {
            if (plants[i] == null) {
                plants[i] = n;
                plantStates[i] = 0;
                break;
            }
        }
    }
    
    /**
     * Check if the tile at x,y contains a plant.     * 
     */
    boolean tileContainsPlant(int x, int y) {
        for (Coffea c: plants) {
            if (c == null) continue;
            if (c.x == 6 + x * 24 && c.y == 60 + y * 24) return true;
        }
        return false;
    }
    
    /**
     * Checks if the tile contains an item. 
     * 
     * If the plant state is 4 (harvestable) return 1 (fruit++)
     * If the plant is dead, clean it up.
     * If no plant, return 0.
     * 
     */
    int tileContainsItem(int x, int y){
        for(int i = 0; i < plants.length; i++){
            if(plants[i] == null)continue;
           
            if(plants[i].x == 6+x*24 && plants[i].y == 60+y*24){
                //Harvestable!
                if(getState(i) == 4){
                    plants[i] = null;
                    plantStates[i] = 0;
                    
                    return 1;
                }
                //Dead, clean it up.
                if(getState(i) == 5){
                    plants[i] = null;
                    plantStates[i] = 0;
                    continue;
                }
            }
        }
        return 0;//no item
    }
    
    /**
     * Updates the plants at the end of each day.
     * 
     */
    void updatePlants(){
        for(int i = 0; i < plants.length; i++){
            if(plants[i] == null)continue;
            if(plantStates[i] < 4) plantStates[i]++;
            switch(plantStates[i]){
                case 1:
                    plants[i].dayOne();
                    break;
                case 2:
                    plants[i].dayTwo();
                    break;
                case 3:
                    plants[i].dayThree();
                    break;
                case 4:
                    plants[i].harvest();
                    break;
                default:
                    break;
            }
        }
    }
    
    
    //Getter/Setters
    void setState(int st, int idx){
        plantStates[idx] = st;
    }
    int getState(int idx){
        return plantStates[idx];
    }
    
    public Coffea getPlant(int idx){
        return plants[idx];
    }
    
    public Coffea[] getAllPlants(){
        return plants;
    }

}
