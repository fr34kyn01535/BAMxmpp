package yt.bam.bamxmpp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class BAMxmpp extends JavaPlugin implements Listener {

	public static BAMxmpp plugin;
	public final static String intRegex = "(-)?(\\d){1,10}(\\.(\\d){1,10})?";

        public BAMxmpp() {
		plugin = this;
	}
        
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		PluginDescriptionFile pdfFile = this.getDescription();
		LogHelper.logInfo("[" + pdfFile.getName() + "] " + "startupMessage");
		LogHelper.logInfo("[" + pdfFile.getName() + "] " + "version" + " " + pdfFile.getVersion() + " " + "enableMsg");
		new XMPPer();
	}

	@Override
	public void onDisable() {
		XMPPer.onDisable(this);
		LogHelper.logInfo("[" + this.getDescription().getName() + "] " + "disableMsg");
	}

	public static FileConfiguration getConf() {
		return plugin.getConfig();
	}	
}