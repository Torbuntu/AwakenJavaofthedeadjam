# Awaken (Working title) a Java game for the Java of the dead POkitto game jam

## Day 1
| Showcase |
|----|
| ![](Showcase/day1.gif?raw=true) |

Day one was just figuring out what to make and playing around with Aseprite to see what I could do quickly.
Created a Hero character and got him working inside the Java code in FemtoIDE. Initial game design plans have been drawn up as follows (pretty rough sketch):
```
[[ Awaken ]] <- working title

5 stages. 
Each stage in a different location with unique undead foes and a mega boss.

Player travels to one of the areas to research the culture and awakens the
undead inadvertantly. The character must then defeat all the undead and also
the megaboss to discover the treasures of the region.

Weapons: Yo-yo, Shovel 


1. North America : Skeletons and the Skinwalker 

2. China : Terracotta Army and Qin Shi Huang :

3. Romania : Vampyres and Dracula :

4. Scandinavia : Draugr and Ivar the Boneless :

5. Egypt : Mummies and Anubis : 
```
This is obviously just an initial run at setting up the game and planning a rough "sketch" of what it can be. The idea is that it is a treasure hunting game gone wrong. Your hero travels to a distant location to hunt for mystical coffee beans, but instead ends up waking the dead! Fighting off hordes of undead monsters until a megaboss arrives. When the megaboss is destroyed the Hero then collects the mystical coffee beans and enjoys the joe of the land. (WIP)

## Day 2

| Showcase |
|----|
| ![](Showcase/day2.gif?raw=true) |

Created a Git repo with the WIP title Awaken. Pushed the project sources to it so I can now work from any computer.
Downloaded and set up the [FemtoIDE Game Jam Edition](https://github.com/felipemanga/FemtoIDE/releases/tag/v0.0.15b)

Playing with a state machine using a switch case and int variiable.

Started using `CGArne` as the default color palette. Might change in the future. Set the assets to indexed mode in Aseprite so switching palette might be easier.

List of palettes from [here (16 color)](https://lospec.com/palette-list)

Finished the shovel attack animation.

Tried to fix the walking animation.

## Day 3

| Showcase |
|----|
| ![](Showcase/day3.gif?raw=true) |

Decided I did not want to continue down the path of making a platformer, as that seems to always be my "safe zone" and the type of project I always default to.

I restarted making a new type of game that is a sort of cross between Megaman Battle Network games and Plants Vs. Zombies.

I liked the Shovel and the Yoyo too much to abandoned those ideas.

See ![Design docs](DesignDocs/Overview) for more details.

## Day 4

| Showcase |
|----|
| ![](Showcase/day4.gif?raw=true) |

Adding a better background. Multiple enemies and attacks actually trigger hurting animations. 


## Day 5

| Showcase |
|----|
| ![](Showcase/day5.gif?raw=true) |

Added:
* Health and cooldown including health sprite
* Inventory Screen
* Inventory system with swappable items for left and right hand
* Planter item (will eventually be limited by Seed quantity)
* Plantable tiles and a Sprout sprite

Updated:
* The playfield background art has been updated
* Added some animations to Hero

TODO:
* Make the plants grow per day.
* Droppable items from plants
* Killable zombies (and dropped items)
* Add Seed items for planting
* Update the art assets to be not terrible (which includes better animations). This will be after the game logic is implemented, as that is more important to finalize first.

## Day 6

| Showcase |
|----|
| ![](Showcase/day6.gif?raw=true) |

Short on time today, so quickly reworked the inventory GUI and system. Inspired by older Zelda games, makes much more sense and leaves room for other more important things on the screen.

## Day 7

| Showcase |
|----|
| ![](Showcase/day7.gif?raw=true) |

Added:
* Zombies now take damage and die, releasing new zombies.
* ZombieImpl now contains all wave information for enemies.
* Seed item added, not functional.
* Now takes time to plant a seed.
* Added New Day screen (nothing exciting on it yet)

Updated:
* Cleaned up some game logic in the code, was a good overhaul effort. 


## Day8

| Showcase |
|----|
| ![](Showcase/day8.gif?raw=true) |

Added:
* Shop page (end of each day)
* Sword, Gun and Ammo assets
* Player can use the sword to attack (after purchace)
* Items are hidden until purchaced

Updated:
* Default items are planter and shovel
* Day is shorter
* Collisions updated
* Mechanic updates

## Day9


| Showcase |
|----|
| ![](Showcase/day9.gif?raw=true) |

Added: 
* MaxLives container, buying lives now means number of containers.
* Plants now grow and are harvestable with the planter.
* New updated assets

Updated:
* A few asset updates
* Shop screen fixed to show prices
* Added a new java file for handling plants. 
* Cleaned up placements of printed text.


## Day10

| Showcase |
|----|
| ![](Showcase/day10.gif?raw=true) |

Updated:
* Zombies now eat plants
* Plants have a dead state
* Player can clean up dead plants with the planter tool



## Day11

| Showcase |
|----|
| ![](Showcase/day11.gif?raw=true) |

Added:
* Gun now shoots and kills zombies using ammo.

Updated:
* Fixed a bug when harvesting and seed count is greater than 0

TODO:
* Create a Survival Meter
* Update the A:B icons in the Pause Menu to be flipped to correspond to the hardware buttons
* Add/Update all Artwork Assets
* Add craftable items.


## Day12

| Showcase |
|----|
| ![](Showcase/day12.gif?raw=true) |

Added:
* Added new icons for items
* Work on adding the craftable items started
* Added survival meter

Updated:
* Zombies now have a 50/50 chance to spawn as sprinters
* Zombies can now eat plants correctly
* UI/UX has been overhauled on the game screen.
* A:B buttons switched to match Pokitto



## Day13

| Showcase |
|----|
| ![](Showcase/day13.gif?raw=true) |

Added:
* Added Juice, Tea and Coffee consumables 

Updated:
* Items are now craftable
* Can consume crafted items

## Day14

| Showcase |
|----|
| ![](Showcase/day14.gif?raw=true) |

Added:
* Loot drops! Now when killing zombies, loot bags will be dropped. Collect these to gain bonus coins, sapplings or extra hearts!
* New icons for Tea, Juice and Coffee. 

Updated:
* Inventory screen now has crafting icons instead of ugly big letters.
* Wife helped make a MUCH better flower icon that matches more the coffee plant's flower. 8x8 flowers with 5 pedals is tough!

TODO:
* Potentially make the loot bags drop randomly by chance ratio?
* Update All the art,


## Day15

Added:
* Sound! The menu now has a 'selected' sound
* String constants for performance
* Itch.io release page

## Day16

Added:
* Shooting sprite

Updated:
* Shrank the hero sprite so uses less memory. 
* More constants
* Updated the backgrounds to contain more info
* Removed Survival Meter 
* Removed edibles. Now collect them for scores.

