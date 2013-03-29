package yt.bam.bamxmpp;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.UserStatusListener;

public class XMPPer implements Listener, PacketListener, SubjectUpdatedListener, UserStatusListener, ParticipantStatusListener {
	
	public static String chatPrefix = "";
	public static Connection xmppConnection;
	public static MultiUserChat chatRoom;
	public static Map<String, String> participantNicks;
	public static String cmdPrefix;
	public static Integer lastMessageStamp = getUnixTimestamp(0L);
        public static CommanderCommandSender ccs = new CommanderCommandSender();
        
	public XMPPer() {
                    FileConfiguration cnf = BAMxmpp.getConf();
                    LogHelper.logInfo("[BAMxmpp] " + "xmppConnecting");
                    cmdPrefix = cnf.getString("xmppCommandPrefix", "#");
                    participantNicks = new HashMap<String, String>();
                    xmppConnection = new XMPPConnection(cnf.getString("xmppHost", "localhost"));
                    try {
                            xmppConnection.connect();
                            if (cnf.getString("xmppUser", "").equals("")) {
                                    LogHelper.logInfo("xmppAnonymousLogin");
                                    xmppConnection.loginAnonymously();
                            } else {
                                    xmppConnection.login(cnf.getString("xmppUser"), cnf.getString("xmppPassword", ""));
                            }
                            // Only do this if the connection didn't fail
                            DiscussionHistory history = new DiscussionHistory();
                            history.setMaxStanzas(0);
                            chatRoom = new MultiUserChat(xmppConnection, cnf.getString("xmppRoom.name"));
                            chatRoom.addMessageListener(this);
                            chatRoom.addParticipantStatusListener(this);
                            chatRoom.addSubjectUpdatedListener(this);
                            chatRoom.addUserStatusListener(this);
                            try {
                                    chatRoom.join(cnf.getString("xmppBotNick", "BAMxmpp"), cnf.getString("xmppRoom.password", ""), history, SmackConfiguration.getPacketReplyTimeout());
                                    for (Occupant occupant : chatRoom.getParticipants()) {
                                            participantNicks.put(occupant.getJid(), occupant.getNick());
                                    }
                            } catch(XMPPException e) {
                                    LogHelper.logSevere("[BAMxmpp] " + "xmppUnableToJoinRoom");
                                    LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause());
                                    return;
                            }
                    } catch (XMPPException e) {
                            LogHelper.logSevere("[BAMxmpp] " + "xmppConnectionFailed");
                            LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause());
                            return;
                    }

                    // set up a recurrent task simply sending a keep-alive message, since keep-alive requests don't seem to do the trick
                    if (BAMxmpp.getConf().getBoolean("xmppEnablePing", true)) {
                            Integer pingTime = BAMxmpp.getConf().getInt("xmppEnablePingTime", 45);
                            Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(BAMxmpp.plugin, new Runnable() {
                                    @Override
                                    public void run () {

                                            if ((getUnixTimestamp(0L) - XMPPer.lastMessageStamp) > 300) {
                                                    try {
                                                            chatRoom.sendMessage(filterOutgoing("(ping)"));
                                                            XMPPer.lastMessageStamp = getUnixTimestamp(0L);
                                                    } catch(XMPPException ex) {
                                                            LogHelper.logDebug("Message: " + ex.getMessage() + ", cause: " + ex.getCause());
                                                    }
                                            }

                                    }
                            }, (20 * 60 * pingTime), (20 * 60 * pingTime));
                    }


                    BAMxmpp.plugin.getServer().getPluginManager().registerEvents(this, BAMxmpp.plugin);
		
	}
        public static Integer getUnixTimestamp(Long i) {
            if (i == 0) {
                    i = System.currentTimeMillis();
            }

            return (int) (i / 1000L);
	}
	/***
	 * Closes XMPP connection on plugin disable.
	 */
	public static void onDisable(BAMxmpp p) {
		if (xmppConnection.isConnected()) {
			xmppConnection.disconnect();
		}
		xmppConnection = null;
	}

	public static String filterOutgoing(String input) {
		StringBuilder in = new StringBuilder(input);
		while (in.indexOf("\u00a7") != -1) {
			in.replace(in.indexOf("\u00a7"), in.indexOf("\u00a7") + 2, "");
		}
		return in.toString();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void sendJoinMessage(PlayerJoinEvent e) {
		try {
			chatRoom.sendMessage(filterOutgoing(e.getJoinMessage()));
			XMPPer.lastMessageStamp = getUnixTimestamp(0L);
		} catch(XMPPException ex) {
			LogHelper.logDebug("Message: " + ex.getMessage() + ", cause: " + ex.getCause());
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void interceptChat(final AsyncPlayerChatEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(BAMxmpp.plugin, new Runnable() {

			@Override
			public void run() {
				try {
					chatRoom.sendMessage(filterOutgoing(String.format(e.getFormat(), e.getPlayer().getName(), e.getMessage())));
					XMPPer.lastMessageStamp = getUnixTimestamp(0L);
				} catch(Exception ex) {
					LogHelper.logDebug("Message: " + ex.getMessage() + ", cause: " + ex.getCause());
				}
			}
			
		});
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void sendLeaveMessage(PlayerQuitEvent e) {
		try {
			chatRoom.sendMessage(filterOutgoing(e.getQuitMessage()));
			XMPPer.lastMessageStamp = getUnixTimestamp(0L);
		} catch(XMPPException ex) {
			LogHelper.logDebug("Message: " + ex.getMessage() + ", cause: " + ex.getCause());
		}
	}
	
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			if (!chatRoom.getOccupant(packet.getFrom()).getNick().equals(chatRoom.getNickname())) {
				Message message = (Message)packet;
				if (message.getBody().startsWith(cmdPrefix)) {
					// execute command on the server as Console when this command comes from a trusted person
					if (BAMxmpp.getConf().getList("xmppAdmins").contains(chatRoom.getOccupant(message.getFrom()).getNick())) {
						String cmd = message.getBody().substring(cmdPrefix.length());
						BAMxmpp.plugin.getServer().dispatchCommand(ccs, cmd);
					}
				} else {
					Occupant actor = chatRoom.getOccupant(message.getFrom());
					String actorNick = (actor.getRole().equals("moderator") ? "@" : actor.getRole().equals("participant") ? "+" : "") + actor.getNick();
					String body = message.getBody();
					BAMxmpp.plugin.getServer().broadcastMessage(chatPrefix + actorNick + ": " + body);
				}
			}
		} else {
			LogHelper.logWarning("xmppUnknownPacket" + packet.getClass().toString());
		}
	}

	public void kicked(String actor, String reason) {
		/*String message = "Kicked by " + actor;
		if (reason != null && !reason.equals(""))
			message += " for the reason " + reason;
		CommandsEX.plugin.getServer().broadcastMessage(chatPrefix + "Kicked from room");
		*/
	}

	public void voiceGranted() {
	}

	public void voiceRevoked() {
	}

	public void banned(String actor, String reason) {
		/*String message = "Kicked by " + actor;
		if (reason != null && !reason.equals(""))
			message += " for the reason " + reason;
		CommandsEX.plugin.getServer().broadcastMessage(chatPrefix + "Banned from room");
		*/
	}

	public void membershipGranted() {
	}

	public void membershipRevoked() {
	}

	public void moderatorGranted() {
	}

	public void moderatorRevoked() {
	}

	public void ownershipGranted() {
	}

	public void ownershipRevoked() {
	}

	public void adminGranted() {
	}

	public void adminRevoked() {
	}

	public void subjectUpdated(String subject, String from) {
		//CommandsEX.plugin.getServer().broadcastMessage(chatPrefix + "Subject changed to " + subject);
	}

	public void joined(String participant) {
		Occupant actor = chatRoom.getOccupant(participant);
		String joiner = chatPrefix + actor.getNick();
		try {
			// try to condense this join if we have this plugin part available
			if (BAMxmpp.getConf().getBoolean("xmppNotifyChatJoin", true)) {
				Handler_condensejoins.joins.add("#" + joiner);
			}
			participantNicks.put(participant, actor.getNick());
		} catch (Throwable e) {
			if (!participantNicks.containsKey(participant)) {
				if (BAMxmpp.getConf().getBoolean("xmppNotifyChatJoin", true)) {
					BAMxmpp.plugin.getServer().broadcastMessage(joiner + " " + "xmppJoin");
				}
				participantNicks.put(participant, actor.getNick());
			}
		}
	}

	public void left(String participant) {
		String actorNick = chatPrefix + participantNicks.get(participant);
		try {
			// try to condense this leave if we have this plugin part available
			if (BAMxmpp.getConf().getBoolean("xmppNotifyChatJoin", true)) {
				Handler_condensejoins.leaves.add("#" + actorNick);
                                
			}
			participantNicks.remove(participant);
		} catch (Throwable e) {
			if (BAMxmpp.getConf().getBoolean("xmppNotifyChatJoin", true)) {
				BAMxmpp.plugin.getServer().broadcastMessage(actorNick + " " +"xmppLeave");
			}
			participantNicks.remove(participant);
		}
	}

	public void kicked(String participant, String actor, String reason) {
		/*String message = participantNicks.get(participant) + " was kicked by " + actor;
		if (reason != null && !reason.equals(""))
			message += " for the reason " + reason;
		CommandsEX.plugin.getServer().broadcastMessage(chatPrefix + message);
		participantNicks.remove(participant);
		*/
	}

	public void voiceGranted(String participant) {
	}

	public void voiceRevoked(String participant) {
	}

	public void banned(String participant, String actor, String reason) {
		/*
		String message = participantNicks.get(participant) + " was banned by " + actor;
		if (reason != null && !reason.equals(""))
			message += " for the reason " + reason;
		CommandsEX.plugin.getServer().broadcastMessage(chatPrefix + message);
		participantNicks.remove(participant);
		*/
	}

	public void membershipGranted(String participant) {
	}

	public void membershipRevoked(String participant) {
	}

	public void moderatorGranted(String participant) {
	}

	public void moderatorRevoked(String participant) {
	}

	public void ownershipGranted(String participant) {
	}

	public void ownershipRevoked(String participant) {
	}

	public void adminGranted(String participant) {
	}

	public void adminRevoked(String participant) {
	}

	public void nicknameChanged(String participant, String newNickname) {
		String message = participantNicks.get(participant) + " " + "xmppNameChange" + newNickname;
		if (BAMxmpp.getConf().getBoolean("xmppNotifyChatJoin", true)) {
			BAMxmpp.plugin.getServer().broadcastMessage(chatPrefix + message);
		}
		participantNicks.put(participant, newNickname);
	}
}
