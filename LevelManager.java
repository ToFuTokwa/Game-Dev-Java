public class LevelManager {
    private TileMap[] levels = new TileMap[3];
    private String[] backgrounds = new String[3];
    private String[] names = {"Home", "Mob Cave", "Mine Shaft"};
    private int currentIndex = 0;

    public LevelManager() {
        for (int i = 0; i < 3; i++) {
            levels[i] = new TileMap("levels/level" + (i + 1) + ".txt");
            backgrounds[i] = "Assets/Mocap" + (i + 1) + ".png";
        }
    }

    public void setLevel(int i) { if (i >= 0 && i < 3) currentIndex = i; }
    
    // Returns current index for the automatic progression logic
    public int getCurrentLevelIndex() { return currentIndex; }
    
    public String[] getLevelNames() { return names; }
    public TileMap getCurrentLevel() { return levels[currentIndex]; }
    public String getCurrentBackgroundPath() { return backgrounds[currentIndex]; }
}