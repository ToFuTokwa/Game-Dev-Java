public class LevelManager {
    private TileMap[] levels = new TileMap[3];
    private String[] backgrounds = {"Assets/Mocap1.png", "Assets/Mocap2.png", "Assets/Mocap3.png"};
    private String[] names = {"Home", "Mob Cave", "Mine Shaft"};
    private int currentIndex = 0;
    private boolean[] levelValid = new boolean[3];

    public LevelManager() {
        for (int i = 0; i < 3; i++) {
            String levelFile = "levels/level" + (i + 1) + ".txt";
            System.out.println("Loading: " + levelFile);
            
            try {
                levels[i] = new TileMap(levelFile);
                levelValid[i] = isValidTileMap(levels[i]);
                if (!levelValid[i]) {
                    System.out.println("Invalid map data, creating fallback");
                    createFallbackLevel(i);
                } else {
                    System.out.println("✓ " + names[i] + " loaded OK");
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to load " + levelFile);
                createFallbackLevel(i);
            }
        }
        
        // Start on first valid level
        findFirstValidLevel();
    }
    
    private boolean isValidTileMap(TileMap tm) {
        if (tm == null || tm.getMap() == null) return false;
        int[][] map = tm.getMap();
        return map.length >= 23 && map[0] != null && map[0].length >= 40;
    }
    
    private void createFallbackLevel(int index) {
        levels[index] = new TileMap(23, 40); // Needs this constructor in TileMap!
        levelValid[index] = true;
    }
    
    private void findFirstValidLevel() {
        for (int i = 0; i < 3; i++) {
            if (levelValid[i]) {
                currentIndex = i;
                return;
            }
        }
        currentIndex = 0;
    }

    public void setLevel(int i) { 
        if (i >= 0 && i < 3) currentIndex = i; 
    }
    
    public int getCurrentLevelIndex() { return currentIndex; }
    
    public String[] getLevelNames() { 
        String[] display = new String[3];
        for (int i = 0; i < 3; i++) {
            display[i] = names[i] + (levelValid[i] ? " ✓" : " ⚠");
        }
        return display;
    }
    
    public TileMap getCurrentLevel() { return levels[currentIndex]; }
    public String getCurrentBackgroundPath() { return backgrounds[currentIndex]; }
    public boolean isCurrentLevelValid() { return levelValid[currentIndex]; }
    public int getFirstValidLevelIndex() { 
        for (int i = 0; i < 3; i++) if (levelValid[i]) return i;
        return 0;
    }
}