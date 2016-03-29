/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.rassun;

import java.io.File;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * コンフィグ管理クラス
 * @author ucchy
 */
public class RassunConfig {

    private boolean aprilfoolTimer;
    private ItemStack award;
    private int maxAwardPerDay;
    private List<String> questions;
    private List<String> answers;
    private int time;
    private int penaltyDamage;

    private File dataFolder;
    private File pluginFile;

    /**
     * コンストラクタ
     * @param dataFolder
     * @param pluginFile
     */
    public RassunConfig(File dataFolder, File pluginFile) {
        this.dataFolder = dataFolder;
        this.pluginFile = pluginFile;
        reload();
    }

    /**
     * コンフィグデータのリロードを行う。
     */
    public void reload() {

        if ( !dataFolder.exists() ) {
            dataFolder.mkdirs();
        }
        File file = new File(dataFolder, "config.yml");
        if ( !file.exists() ) {
            Utility.copyFileFromJar(pluginFile, file, "config_ja.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        aprilfoolTimer = config.getBoolean("aprilfoolTimer", true);
        award = config.getItemStack("award", new ItemStack(Material.DIAMOND));
        maxAwardPerDay = config.getInt("maxAwardPerDay", 3);
        questions = config.getStringList("questions");
        answers = config.getStringList("answers");
        time = config.getInt("time", 20);
        penaltyDamage = config.getInt("penaltyDamage", 10);

        if ( questions.size() == 0 ) {
            questions.add("ラッスンゴレライ説明してね");
        }
        if ( answers.size() == 0 ) {
            answers.add("ちょっと待ってちょっと待ってお兄さん");
        }
    }

    public boolean isAprilfoolTimer() {
        return aprilfoolTimer;
    }

    public ItemStack getAward() {
        return award;
    }

    public int getMaxAwardPerDay() {
        return maxAwardPerDay;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getTime() {
        return time;
    }

    public int getPenaltyDamage() {
        return penaltyDamage;
    }
}
