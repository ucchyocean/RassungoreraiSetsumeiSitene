/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.rassun;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ラッスンゴレライ説明してね！！
 * @author ucchy
 */
public class RassungoreraiSetsumeiSitene extends JavaPlugin implements Listener {

    private RassunConfig config;
    private BukkitRunnable setsumeiTimer;
    private Player questioner;
    private HashMap<String, Counter> counters;

    /**
     * プラグインが起動した時に呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // リスナー登録
        getServer().getPluginManager().registerEvents(this, this);

        // コンフィグ読み込み
        config = new RassunConfig(getDataFolder(), getFile());

        // カウンター読み込み
        loadCounter();
    }

    /**
     * プラグインが停止する時に呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {

        // カウンターを保存する
        saveCounter();
    }

    /**
     * プレイヤーがチャット発言をしたときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

        // エイプリルフールタイマーが有効なら、4月1日じゃないときは動作しない
        if ( config.isAprilfoolTimer() ) {
            Calendar cal = Calendar.getInstance();
            if ( cal.get(Calendar.MONTH) != Calendar.APRIL ||
                    cal.get(Calendar.DAY_OF_MONTH) != 1 ) {
                return;
            }
        }

        if ( setsumeiTimer == null ) {
            // 質問待ち

            // 質問文が含まれていないなら無視
            if ( !checkContains(event.getMessage(), config.getQuestions()) ) {
                return;
            }

            // システムメッセージ
            delayedBroadcast(
                    ChatColor.YELLOW + "" + config.getTime() + "秒以内に説明してね！");

            // タイマー生成と開始
            questioner = event.getPlayer();
            setsumeiTimer = new BukkitRunnable() {
                public void run() {
                    // 時間切れ、罰ゲーム
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "時間切れ！");
                    penalty(questioner);
                    setsumeiTimer = null;
                }
            };
            setsumeiTimer.runTaskLater(this, config.getTime() * 20);

        } else {
            // 回答待ち

            // 回答文が含まれていないなら無視
            if ( !checkContains(event.getMessage(), config.getAnswers()) ) {
                return;
            }

            // タイマー停止
            setsumeiTimer.cancel();
            setsumeiTimer = null;

            // 質問者と回答者が同一なら、ペナルティ
            if ( questioner.getName().equals(event.getPlayer().getName()) ) {
                delayedBroadcast(ChatColor.YELLOW + "自分で回答しとるやないかーい！");
                penalty(questioner);
                return;
            }

            // 報酬の支払い
            delayedBroadcast(
                    ChatColor.YELLOW + "" + event.getPlayer().getName()
                    + "さんが回答してくれたよ！");

            if ( !counters.containsKey(questioner.getName()) ) {
                counters.put(questioner.getName(), new Counter(config.getMaxAwardPerDay()));
            }
            if ( !counters.containsKey(event.getPlayer().getName()) ) {
                counters.put(event.getPlayer().getName(),
                        new Counter(config.getMaxAwardPerDay()));
            }

            if ( !counters.get(questioner.getName()).checkCounterOverMax()
                    && questioner.isOnline() ) {
                counters.get(questioner.getName()).addCounter();
                questioner.getInventory().addItem(config.getAward().clone());
            }
            if ( !counters.get(event.getPlayer().getName()).checkCounterOverMax() ) {
                counters.get(event.getPlayer().getName()).addCounter();
                event.getPlayer().getInventory().addItem(config.getAward().clone());
            }
        }
    }

    /**
     * 生物がスポーンした時に呼び出される
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // bleeding以外は無視する
        if ( event.getSpawnReason() != SpawnReason.BREEDING ) {
            return;
        }

        // エイプリルフールタイマーが有効なら、4月1日じゃないときは動作しない
        if ( config.isAprilfoolTimer() ) {
            Calendar cal = Calendar.getInstance();
            if ( cal.get(Calendar.MONTH) != Calendar.APRIL ||
                    cal.get(Calendar.DAY_OF_MONTH) != 1 ) {
                return;
            }
        }

        // 近隣のエンティティを取得し、サウジアラビア人とインド人がいるかどうかを確認する
        boolean saudiarabian = false;
        boolean indian = false;
        for ( Entity entity : event.getEntity().getNearbyEntities(3, 3, 3) ) {
            if ( !entity.equals(event.getEntity())
                    && entity.getType() == event.getEntityType()
                    && entity.getCustomName() != null ) {
                if ( entity.getCustomName().equals("サウジアラビア人") ) {
                    saudiarabian = true;
                } else if ( entity.getCustomName().equals("インド人") ) {
                    indian = true;
                }
            }
        }

        // 両方見つかったら、名前をラッスンゴレライに変更する
        if ( saudiarabian && indian ) {
            event.getEntity().setCustomName("ラッスンゴレライ");
            event.getEntity().setCustomNameVisible(true);
        }
    }

    private boolean checkContains(String src, List<String> list) {
        for (String l : list) {
            if (src.contains(l)) {
                return true;
            }
        }
        return false;
    }

    private void penalty(Player player) {
        player.getWorld().playSound(player.getLocation(), SoundEnum.ANVIL_LAND.getBukkit(), 1, 1);
        player.damage(config.getPenaltyDamage());
    }

    private void delayedBroadcast(final String message) {
        new BukkitRunnable() {
            public void run() {
                Bukkit.broadcastMessage(message);
            }
        }.runTaskLater(this, 3);
    }

    private void saveCounter() {

        YamlConfiguration conf = new YamlConfiguration();
        for ( String key : counters.keySet() ) {
            Counter counter = counters.get(key);
            conf.set(key + ".date", counter.date.getTime());
            conf.set(key + ".count", counter.count);
        }

        if ( !getDataFolder().exists() ) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "counter.yml");
        try {
            conf.save(file);
        } catch (IOException e) {
            // do nothing.
        }
    }

    private void loadCounter() {

        counters = new HashMap<String, Counter>();

        if ( !getDataFolder().exists() ) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "counter.yml");
        if ( !file.exists() ) {
            return;
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        for ( String key : conf.getKeys(false) ) {
            long time = conf.getLong(key + ".date");
            int count = conf.getInt(key + ".count");
            counters.put(key, new Counter(new Date(time), count, config.getMaxAwardPerDay()));
        }
    }

    /**
     * 報酬をもらった回数のカウンター
     * @author ucchy
     */
    class Counter {

        private Date date;
        private int count;
        private int max;

        Counter(int max) {
            this.max = max;
            date = new Date();
            count = 0;
        }

        Counter(Date date, int count, int max) {
            this.date = date;
            this.count = count;
            this.max = max;
        }

        boolean checkCounterOverMax() {
            if ( !isToday() ) {
                return false;
            }
            return count >= max;
        }

        void addCounter() {
            if ( !isToday() ) {
                date = new Date();
                count = 1;
                return;
            }
            count += 1;
        }

        private boolean isToday() {
            Calendar target = Calendar.getInstance();
            target.setTime(date);
            return (target.get(Calendar.DAY_OF_YEAR)
                    == Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        }
    }
}
