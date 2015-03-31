/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.rassun;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
            copyFileFromJar(pluginFile, file, "config_ja.yml");
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

    /**
     * jarファイルの中に格納されているテキストファイルを、jarファイルの外にコピーするメソッド<br/>
     * WindowsだとS-JISで、MacintoshやLinuxだとUTF-8で保存されます。
     * @param jarFile jarファイル
     * @param targetFile コピー先
     * @param sourceFilePath コピー元
     */
    private static void copyFileFromJar(
            File jarFile, File targetFile, String sourceFilePath) {

        JarFile jar = null;
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File parent = targetFile.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        try {
            jar = new JarFile(jarFile);
            ZipEntry zipEntry = jar.getEntry(sourceFilePath);
            is = jar.getInputStream(zipEntry);

            fos = new FileOutputStream(targetFile);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( jar != null ) {
                try {
                    jar.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( fos != null ) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
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
