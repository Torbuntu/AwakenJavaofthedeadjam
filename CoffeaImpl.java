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
    
    boolean tileContainsPlant(int x, int y) {
        for (Coffea c: plants) {
            if (c == null) continue;
            if (c.x == 6 + x * 24 && c.y == 60 + y * 24) return true;
            // if(c.x == hx && c.y == hy) return true;
        }
        return false;
    }
    
    int tileContainsItem(int x, int y){
        for(int i = 0; i < plants.length; i++){
            if(plants[i] == null)continue;
            if(plants[i].x == 6+x*24 && plants[i].y == 60+y*24){
                if(plantStates[i] == 4){
                    plants[i] = null;
                    plantStates[i] = 0;
                    
                    return Math.random(0, 3);
                }
            }
        }
        return 6//no item
    }
    
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
    
    Coffea[] getAllPlants(){
        return plants;
    }

}
