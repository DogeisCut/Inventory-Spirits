# Initial Release
- [X] Curios Support
- [X] Cosmetic Armor Reworked Support
- [X] Aeronautics support (just need the proper entity tags so they attach to sublevels)
- [X] Punch to destroy and drop items (now takes a few hits instead of one, minecart/boat-style)
- [X] Right click to collect into inventory
- [X] Make the spirits bouncy against blocks :3
  - I dont think this works properly but im too lazy to fix it so
- [X] custom sounds (one for breaking, hitting, collecting, spawning, and ambient)
- [ ] (more) custom particles (one for breaking, hitting, collecting, and spawning)
  - ok well technically there's already collecting particles but idk if i'm satisfied with them?
  - and the spawning particles are probably fine tbh...
- [X] solid block handling
  - yeah i ended up just relying on mc to do it for me
- [X] void handling
- [X] Lava handling
  - [X] Take into account the lava level when pushing the spirits out of lava
- [X] Jade support
  - [X] make it look cooler
  - [X] add owner
- [X] make it not crash if you dont have curios (and make sure the same doesnt happen for Cosmetic Armor Reworked)
- [X] make them not spawn if you dont have litterally anything
- [ ] Make the spirits regen health like minecarts and boats
- [ ] Store date of death, death location/rotation, and death reason on the entity for future config options 
- [ ] maybe make spirits do what items do where they pop up if they are inside of a block

# Next Release

- [ ] Config Options 
  - [ ] Create on void death (default: true)
  - [ ] Create for creative players (default: true)
  - [ ] Create for empty inventories (default: false)
  - [ ] Allow stealing (default: true)
  - [ ] Max spirits per player (drops oldest) (default: Infinity)
  - [ ] Store Behavior on death (default: store experience and items) (options: store experience and drop items, store experience and items, drop experience and store items)
  - [ ] Experience returns on collect (default: whatever minecraft does)
  - [ ] Punch drops items (default: true) (false would make this act like right-clicking)
  - [ ] Float in lava (default: true)
  - [ ] Float in water (default: false)
  - [ ] Glow Effect on spirits (default: false)
  - [ ] Auto-destroy timer (default: Infinity)
  - [ ] Destroy behavior (drop, disappear) (default: drop)
  - [ ] Destroy/punchable spirits (default: true)
  - [ ] Drop overflowing items (default: true) (false would make the spirit stick around, only giving what it can fit until you clear your inventory)
  - [ ] Retain inventory layout (default: true)
  - [ ] Drop existing inventory on collect (default: false)
  - [ ] Void death height offset (default: 0 blocks)
  - [ ] Cap void death to minimum block height (default: true)
  - [ ] Enable /dropspirits command (default: false)
  - [ ] Remove spirit on collection (default: true)
  - [ ] Show GUI on interact (default: false) (just auto collects instead)
  - [ ] Moveable spirits (default: false)
  - [ ] Solid spirits (default: false)
  - [ ] Pushable spirits (default: false)
  - [ ] Leashable spirits (default: false)
  - [ ] Push spirits out of these blocks (default: bedrock, void_air, end_portal_frame, end_portal, end_gateway, nether_portal)
  - [ ] Spirits go through portals (default: false)
  - [ ] Auto collect distance (default: disabled) (is normally a radius)
  - [ ] Spirit ambient noises (default: true)
  - [ ] Spirit particles (default: true)
  - [ ] Show spirit owner on hover (default: false) (will be done through fancy outlined text rather than a nametag)
  - [ ] Require shift right click to collect (default: false)
  - [ ] Allow spirits to pick up items (default: false)
  - [ ] Allow spirits to pick up experience (default: false)
  - [ ] Mob inventory spirit drop behavior (default: never) (options: always, when holding player items, never)
  - Some of these are mutually exclusive and need to be merged accordingly
  - Also a lot of these are ideas and havent been updated with me developing the mod so they probably don't make sense











Actually, no health display, jade doesnt do that for minecarts and boats so why should we here?



As for the items, we can keep the item count, just to make the items *list* dissapear if there's nothing. For the actual items list, I'm picturing something akin to Create's item vault Jade display. (I don't know if it's Jade, Create, or Jade Addons that adds that honestly.)



For xp, I'm thinking something custom, if possible, it'd be cool to display an actual xp bar (or maybe a mini one, may need custom textures with that) and the xp level you would get for it, in the font and color of the xp bar.