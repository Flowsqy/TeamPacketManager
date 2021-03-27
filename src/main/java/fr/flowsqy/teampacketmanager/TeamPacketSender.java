package fr.flowsqy.teampacketmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

public class TeamPacketSender {

    private static final Constructor<?> packetPlayOutScoreboardTeamConstructor;

    private static final Field nameField;
    private static final Field displayNameField;
    private static final Field prefixField;
    private static final Field suffixField;
    private static final Field nameTagVisibilityField;
    private static final Field collisionRuleField;
    private static final Field colorField;
    private static final Field playersField;
    private static final Field methodField;
    private static final Field optionField;

    private static final Constructor<?> chatComponentTextConstructor;

    private static final java.lang.reflect.Method getHandlePlayerMethod;
    private static final Field playerConnectionField;
    private static final java.lang.reflect.Method sendPacketMethod;

    private static final Object BLACK;
    private static final Object DARK_BLUE;
    private static final Object DARK_GREEN;
    private static final Object DARK_AQUA;
    private static final Object DARK_RED;
    private static final Object DARK_PURPLE;
    private static final Object GOLD;
    private static final Object GRAY;
    private static final Object DARK_GRAY;
    private static final Object BLUE;
    private static final Object GREEN;
    private static final Object AQUA;
    private static final Object RED;
    private static final Object LIGHT_PURPLE;
    private static final Object YELLOW;
    private static final Object WHITE;
    private static final Object OBFUSCATED;
    private static final Object BOLD;
    private static final Object STRIKETHROUGH;
    private static final Object UNDERLINE;
    private static final Object ITALIC;
    private static final Object RESET;

    /*
     * Initialization of Reflection
     */
    static {
        try{
            final String versionName = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            final String nms = "net.minecraft.server."+versionName+".";
            final Class<?> packetPlayOutScoreboardTeamClass = Class.forName(nms+"PacketPlayOutScoreboardTeam");

            packetPlayOutScoreboardTeamConstructor = packetPlayOutScoreboardTeamClass.getDeclaredConstructor();

            nameField = packetPlayOutScoreboardTeamClass.getDeclaredField("a");
            displayNameField = packetPlayOutScoreboardTeamClass.getDeclaredField("b");
            prefixField = packetPlayOutScoreboardTeamClass.getDeclaredField("c");
            suffixField = packetPlayOutScoreboardTeamClass.getDeclaredField("d");
            nameTagVisibilityField = packetPlayOutScoreboardTeamClass.getDeclaredField("e");
            collisionRuleField = packetPlayOutScoreboardTeamClass.getDeclaredField("f");
            colorField = packetPlayOutScoreboardTeamClass.getDeclaredField("g");
            playersField = packetPlayOutScoreboardTeamClass.getDeclaredField("h");
            methodField = packetPlayOutScoreboardTeamClass.getDeclaredField("i");
            optionField = packetPlayOutScoreboardTeamClass.getDeclaredField("j");

            final Class<?> chatComponentTextClass = Class.forName(nms+"ChatComponentText");
            chatComponentTextConstructor = chatComponentTextClass.getDeclaredConstructor(String.class);

            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit."+versionName+".entity.CraftPlayer");
            getHandlePlayerMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            final Class<?> entityPlayerClass = Class.forName(nms+"EntityPlayer");
            playerConnectionField = entityPlayerClass.getDeclaredField("playerConnection");
            final Class<?> playerConnectionClass = Class.forName(nms+"PlayerConnection");
            final Class<?> packetClass = Class.forName(nms+"Packet");
            sendPacketMethod = playerConnectionClass.getDeclaredMethod("sendPacket", packetClass);

            final Class<?> enumChatFormat = Class.forName(nms+"EnumChatFormat");

            BLACK = getColor("BLACK", enumChatFormat);
            DARK_BLUE = getColor("DARK_BLUE", enumChatFormat);
            DARK_GREEN = getColor("DARK_GREEN", enumChatFormat);
            DARK_AQUA = getColor("DARK_AQUA", enumChatFormat);
            DARK_RED = getColor("DARK_RED", enumChatFormat);
            DARK_PURPLE = getColor("DARK_PURPLE", enumChatFormat);
            GOLD = getColor("GOLD", enumChatFormat);
            GRAY = getColor("GRAY", enumChatFormat);
            DARK_GRAY = getColor("DARK_GRAY", enumChatFormat);
            BLUE = getColor("BLUE", enumChatFormat);
            GREEN = getColor("GREEN", enumChatFormat);
            AQUA = getColor("AQUA", enumChatFormat);
            RED = getColor("RED", enumChatFormat);
            LIGHT_PURPLE = getColor("LIGHT_PURPLE", enumChatFormat);
            YELLOW = getColor("YELLOW", enumChatFormat);
            WHITE = getColor("WHITE", enumChatFormat);
            OBFUSCATED = getColor("OBFUSCATED", enumChatFormat);
            BOLD = getColor("BOLD", enumChatFormat);
            STRIKETHROUGH = getColor("STRIKETHROUGH", enumChatFormat);
            UNDERLINE = getColor("UNDERLINE", enumChatFormat);
            ITALIC = getColor("ITALIC", enumChatFormat);
            RESET = getColor("RESET", enumChatFormat);
        }catch (ReflectiveOperationException e){
            throw new RuntimeException("Can not load PacketPlayOutScoreboardTeam fields", e);
        }

        packetPlayOutScoreboardTeamConstructor.setAccessible(true);

        nameField.setAccessible(true);
        displayNameField.setAccessible(true);
        prefixField.setAccessible(true);
        suffixField.setAccessible(true);
        nameTagVisibilityField.setAccessible(true);
        collisionRuleField.setAccessible(true);
        colorField.setAccessible(true);
        playersField.setAccessible(true);
        methodField.setAccessible(true);
        optionField.setAccessible(true);

        chatComponentTextConstructor.setAccessible(true);

        getHandlePlayerMethod.setAccessible(true);
        playerConnectionField.setAccessible(true);
        sendPacketMethod.setAccessible(true);

        System.out.println("Successfully load the TagAPI");
    }

    /**
     * Gets nms color Object from EnumChatColor
     * @param color The name of the color field
     * @param enumChatColorClass The EnumChatColor class
     * @return Nms color Object
     * @throws ReflectiveOperationException If there is no field with this name in the EnumChatColor class
     */
    private static Object getColor(String color, Class<?> enumChatColorClass) throws ReflectiveOperationException{
        final Field colorField = enumChatColorClass.getDeclaredField(color);
        colorField.setAccessible(true);
        return colorField.get(null);
    }

    /**
     * Gets Nms color Object for given ChatColor enum constant
     * @param color The ChatColor to convert
     * @return Nms color Object corresponding to the given ChatColor
     */
    private static Object getColor(ChatColor color){
        switch (color){
            case BLACK:
                return BLACK;
            case DARK_BLUE:
                return DARK_BLUE;
            case DARK_GREEN:
                return DARK_GREEN;
            case DARK_AQUA:
                return DARK_AQUA;
            case DARK_RED:
                return DARK_RED;
            case DARK_PURPLE:
                return DARK_PURPLE;
            case GOLD:
                return GOLD;
            case GRAY:
                return GRAY;
            case DARK_GRAY:
                return DARK_GRAY;
            case BLUE:
                return BLUE;
            case GREEN:
                return GREEN;
            case AQUA:
                return AQUA;
            case RED:
                return RED;
            case LIGHT_PURPLE:
                return LIGHT_PURPLE;
            case YELLOW:
                return YELLOW;
            case WHITE:
                return WHITE;
            case MAGIC:
                return OBFUSCATED;
            case BOLD:
                return BOLD;
            case STRIKETHROUGH:
                return STRIKETHROUGH;
            case UNDERLINE:
                return UNDERLINE;
            case ITALIC:
                return ITALIC;
            case RESET:
                return RESET;
        }

        return null;
    }

    /**
     * Send a fake Scoreboard team to Players
     * @param receivers The players that will receive the information
     * @param name The id of the team
     * @param displayName The name displayed in spectator team hotbar
     * @param prefix The prefix of the team
     * @param suffix The suffix of the team
     * @param nameTagVisibility NameTag visibility rules
     * @param collisionRules Collision rules
     * @param color The color of the prefix, suffix and pseudo of the player that are in the team.
     *             The prefix and suffix color can be override by specifying a color in their respective parameter.
     * @param players The players in the team
     * @param method The method for information interpretation clientside
     * @param option Other options
     */
    public static void sendTeamInfo(
            Iterable<Player> receivers,
            String name,
            String displayName,
            String prefix,
            String suffix,
            NameTagVisibility nameTagVisibility,
            CollisionRules collisionRules,
            ChatColor color,
            Collection<String> players,
            Method method,
            Option option
    ){
        Objects.requireNonNull(receivers);
        Objects.requireNonNull(name);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(players);
        Objects.requireNonNull(suffix);
        Objects.requireNonNull(nameTagVisibility);
        Objects.requireNonNull(collisionRules);
        Objects.requireNonNull(color);
        Objects.requireNonNull(players);
        Objects.requireNonNull(method);
        Objects.requireNonNull(option);

        try{
            sendPacket(
                    receivers,
                    name,
                    displayName,
                    prefix,
                    suffix,
                    nameTagVisibility.getVisibility(),
                    collisionRules.getRule(),
                    getColor(color),
                    players,
                    method.getMethod(),
                    option.getPackedOption()
            );
        }catch (ReflectiveOperationException e){
            throw new RuntimeException("Can not send team information", e);
        }
    }

    private static void sendPacket (
            Iterable<Player> receivers,
            String name,
            String displayName,
            String prefix,
            String suffix,
            String nameTagVisibility,
            String collisionRules,
            Object color,
            Collection<String> players,
            int method,
            int option
    ) throws ReflectiveOperationException {

        final Object packet = packetPlayOutScoreboardTeamConstructor.newInstance();

        nameField.set(packet, name);
        displayNameField.set(packet, chatComponentTextConstructor.newInstance(displayName));
        prefixField.set(packet, chatComponentTextConstructor.newInstance(prefix));
        suffixField.set(packet, chatComponentTextConstructor.newInstance(suffix));
        nameTagVisibilityField.set(packet, nameTagVisibility);
        collisionRuleField.set(packet, collisionRules);
        colorField.set(packet, color);
        playersField.set(packet, players);
        methodField.set(packet, method);
        optionField.set(packet, option);

        for(Player receiver : receivers){
            sendPacketMethod.invoke(playerConnectionField.get(getHandlePlayerMethod.invoke(receiver)), packet);
        }

    }

    /**
     * How to display the name tags of players on this team
     */
    public enum NameTagVisibility {
        /**
         * Apply this option to everyone.
         */
        ALWAYS("always"),
        /**
         * Never apply this option.
         */
        NEVER("never"),
        /**
         * Apply this option only for opposing teams.
         */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        /**
         * Apply this option for only team members.
         */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String visibility;

        NameTagVisibility(String visibility) {
            this.visibility = visibility;
        }

        private String getVisibility() {
            return visibility;
        }

        @Override
        public String toString() {
            return "NameTagVisibility{" +
                    "visibility='" + visibility + '\'' +
                    "} " + super.toString();
        }
    }

    /**
     * How players of this team collide with others
     */
    public enum CollisionRules {
        /**
         * Apply this option to everyone.
         */
        ALWAYS("always"),
        /**
         * Never apply this option.
         */
        NEVER("never"),
        /**
         * Apply this option only for opposing teams.
         */
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        /**
         * Apply this option for only team members.
         */
        PUSH_OWN_TEAM("pushOwnTeam");

        private final String rule;

        CollisionRules(String rule) {
            this.rule = rule;
        }

        public String getRule() {
            return rule;
        }

        @Override
        public String toString() {
            return "CollisionRules{" +
                    "rule='" + rule + '\'' +
                    "} " + super.toString();
        }
    }

    /**
     * Method for clientside interpretation of the information
     */
    public enum Method {

        //https://wiki.vg/Protocol#Teams
        /*
         *  0 Create
         *  1 Remove
         *  2 Update
         *  3 Add player
         *  4 Remove Player
         */

        CREATE(0),
        REMOVE(1),
        UPDATE_INFO(2),
        ADD_PLAYERS(3),
        REMOVE_PLAYERS(4);

        private final int method;

        Method(int method) {
            this.method = method;
        }

        public int getMethod() {
            return method;
        }

        @Override
        public String toString() {
            return "Method{" +
                    "method=" + method +
                    "} " + super.toString();
        }
    }

    /**
     * Other options
     */
    public static final class Option {

        private boolean allowFriendlyFire;
        private boolean canSeeFriendlyInvisibles;

        public Option() {
        }

        public Option(boolean allowFriendlyFire, boolean canSeeFriendlyInvisibles) {
            this.allowFriendlyFire = allowFriendlyFire;
            this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
        }

        /**
         * Gets the team friendly fire state
         * @return true if friendly fire is enabled
         */
        public boolean isAllowFriendlyFire() {
            return allowFriendlyFire;
        }

        /**
         * Sets the team friendly fire state
         * @param allowFriendlyFire true if friendly fire is to be allowed
         */
        public void setAllowFriendlyFire(boolean allowFriendlyFire) {
            this.allowFriendlyFire = allowFriendlyFire;
        }

        /**
         * Gets the team's ability to see invisible teammates
         * @return true if team members can see invisible members
         */
        public boolean isCanSeeFriendlyInvisibles() {
            return canSeeFriendlyInvisibles;
        }

        /**
         * Sets the team's ability to see invisible teammates
         * @param canSeeFriendlyInvisibles true if invisible teammates are to be visible
         */
        public void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
            this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
        }

        private int getPackedOption(){
            int options = 0;
            if (allowFriendlyFire)
                options |= 1;

            if (canSeeFriendlyInvisibles)
                options |= 2;

            return options;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            return allowFriendlyFire == option.allowFriendlyFire &&
                    canSeeFriendlyInvisibles == option.canSeeFriendlyInvisibles;
        }

        @Override
        public int hashCode() {
            return Objects.hash(allowFriendlyFire, canSeeFriendlyInvisibles);
        }

        @Override
        public String toString() {
            return "Option{" +
                    "allowFriendlyFire=" + allowFriendlyFire +
                    ", canSeeFriendlyInvisibles=" + canSeeFriendlyInvisibles +
                    '}';
        }
    }

}
