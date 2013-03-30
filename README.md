BAMxmpp
=======
BAMxmpp is a Minecraft XMPP chatroom bridge that connects the ingame Minecraft chat with a XMPP MUC of your choice.

Download
=====
You can get development versions at [ci.bam.yt](http://ci.bam.yt/) or download the latest version directly [here](http://bam.yt:8080/job/BAMxmpp/lastBuild/yt.bam$BAMxmpp/)

Configuration
=====

information required to create XMPP (Jabber) connection from your Minecraft server to XMPP server's room
- xmppUser: name of the user under which we'll log in to the XMPP (Jabber) service itself
- xmppHost: the XMPP (Jabber) server, e.g. jabber.com (so when your username is john@jabber.com, user = john, host = jabber.com)
- xmppPassword: password to use to log the user into the server
- xmppRoom: name of the room where all chat will happen. This is REQUIRED. You may need to find a service which provides Jabber rooms. I use conference.bam.yt Therefore my room name is "myroom@conference.bam.yt" - myroom being the room name, conference.bam.yt the hostname
- xmppRoom.password: if the room requires a password to join it, set it up here
- xmppBotNick: this is the nickname for the plugin. All chat will originate from this nickname. You will, however see who did each chat message originate from.
- xmppCommandPrefix: when a person from the Jabber room starts line with this character, it will be passed through to the Bukkit server and executed as if it was an ordinary command from the CONSOLE! Therefore be sure to only add players to xmppAdmins you really trust!
- xmppAdmins: list of XMPP (Jabber) users who have the right to send commands to Bukkit server. Example = xmppAdmins: [zathrus_writer, djrazr]
- xmppEnablePing: if true and nobody sends any message on the server for configurable number of minutes, the bot will simply say (ping) to keep the connection alive
- xmppEnablePingTime: timeout (in minutes) for the previous setting
- xmppNotifyChatJoin: if true, players on MC server will see when someone joins Jabber chat, if set to false, they won't


BAMxmpp is developed for [BAMcraft.de](http://BAMcraft.de)

![BAMgaming](http://cdn.bam.yt/bamcraft-animatedbanner.gif)

