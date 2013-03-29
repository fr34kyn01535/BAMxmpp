package yt.bam.bamxmpp;

import java.io.File;
import java.io.IOException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class BAMxmpp extends JavaPlugin implements Listener {

	public static BAMxmpp plugin;
        
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
		
                if (new File(getDataFolder() + "/smack.jar").exists() && new File(getDataFolder() + "/smackx.jar").exists()){
			try {
				ClasspathHacker.addFile(getDataFolder() + "/smack.jar");
				ClasspathHacker.addFile(getDataFolder() + "/smackx.jar");
			} catch (IOException e){
				LogHelper.logSevere("xmppSmackReadError");
			}

			// now initialize the actual XMPP communication handling class if smack is installed
			new XMPPer();
		} else {
			LogHelper.logSevere("xmppDownloadSmack");
		}
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