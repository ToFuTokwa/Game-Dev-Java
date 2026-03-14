public class LevelManager {

    private TileMap[] levels;
    private int currentLevel = 0;

    public LevelManager() {
        int totalLevels = 3; 
        levels = new TileMap[totalLevels];
        for (int i = 0; i < totalLevels; i++) {
            levels[i] = new TileMap("levels/level" + (i + 1) + ".txt");
        }
    }

    public TileMap getCurrentLevel() {
        return levels[currentLevel];
    }

    public void loadLevel(int level) {
        if(level >= 0 && level < levels.length) {
            currentLevel = level;
        }
    }

    public void loadNextLevel() {
        if(currentLevel < levels.length - 1) {
            currentLevel++;
        } else {
            currentLevel = 0; // Wrap back to first level
        }
        System.out.println("Loading Level " + (currentLevel + 1));
    }

    public int getCurrentLevelIndex() {
        return currentLevel;
    }

    public int getTotalLevels() {
        return levels.length;
    }
}