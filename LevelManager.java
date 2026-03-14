public class LevelManager {
    private TileMap[] levels;
    private String[] backgrounds;
    private String[] levelNames; 
    private int currentLevel = 0;

    public LevelManager() {
        int total = 3; 
        levels = new TileMap[total];
        backgrounds = new String[total];
        levelNames = new String[total];

        // Level Names - Change these to update the menu!
        levelNames[0] = "Sector 7 Slums";
        levelNames[1] = "Mako Reactor 1";
        levelNames[2] = "Shinra HQ";

        for (int i = 0; i < total; i++) {
            levels[i] = new TileMap("levels/level" + (i + 1) + ".txt");
            backgrounds[i] = "Assets/backgrounds/bg_level" + (i + 1) + ".jpg";
        }
    }

    public void setLevel(int i) { if (i >= 0 && i < levels.length) currentLevel = i; }
    public String[] getLevelNames() { return levelNames; }
    public TileMap getCurrentLevel() { return levels[currentLevel]; }
    public String getCurrentBackgroundPath() { return backgrounds[currentLevel]; }
}