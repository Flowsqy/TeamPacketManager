package fr.flowsqy.teampacketmanager;

import org.bukkit.ChatColor;

import java.util.Objects;
import java.util.function.Consumer;

public class TeamData {

    public final static String DEFAULT_TEAM_ID = "NULL";

    private String id;
    private String displayName;
    private String prefix;
    private String suffix;
    private NameTagVisibility nameTagVisibility;
    private CollisionRules collisionRules;
    private ChatColor color;
    private Option option;

    public TeamData(String id) {
        checkId(id);
        this.id = id;
    }

    public TeamData(String id, String displayName, String prefix, String suffix, NameTagVisibility nameTagVisibility, CollisionRules collisionRules, ChatColor color, Option option) {
        checkId(id);
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRules = collisionRules;
        this.color = color;
        this.option = option;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        checkId(id);
        this.id = id;
    }

    private void checkId(String id){
        if (id == null || id.isEmpty())
            throw new UnsupportedOperationException("You can not set a null or empty team id");
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
    }

    public CollisionRules getCollisionRules() {
        return collisionRules;
    }

    public void setCollisionRules(CollisionRules collisionRules) {
        this.collisionRules = collisionRules;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public TeamData merge(TeamData teamData) {
        return new TeamData(
                mergeData(id, teamData.id, this::setId, DEFAULT_TEAM_ID),
                mergeData(displayName, teamData.displayName, this::setDisplayName),
                mergeData(prefix, teamData.prefix, this::setPrefix),
                mergeData(suffix, teamData.suffix, this::setSuffix),
                mergeData(nameTagVisibility, teamData.nameTagVisibility, this::setNameTagVisibility),
                mergeData(collisionRules, teamData.collisionRules, this::setCollisionRules),
                mergeData(color, teamData.color, this::setColor),
                mergeData(option, teamData.option, this::setOption)
        );
    }

    private <T> T mergeData(T current, T newer, Consumer<T> setter){
        return mergeData(current, newer, setter, null);
    }

    private <T> T mergeData(T current, T newer, Consumer<T> setter, T defaultValue){
        if(current == null){
            if(newer != null)
                setter.accept(newer);
            return defaultValue;
        }
        if(current.equals(newer))
            return defaultValue;
        setter.accept(newer);
        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamData teamData = (TeamData) o;
        return id.equals(teamData.id) && Objects.equals(displayName, teamData.displayName) && Objects.equals(prefix, teamData.prefix) && Objects.equals(suffix, teamData.suffix) && nameTagVisibility == teamData.nameTagVisibility && collisionRules == teamData.collisionRules && color == teamData.color && Objects.equals(option, teamData.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, prefix, suffix, nameTagVisibility, collisionRules, color, option);
    }

    @Override
    public String toString() {
        return "TeamData{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", nameTagVisibility=" + nameTagVisibility +
                ", collisionRules=" + collisionRules +
                ", color=" + color +
                ", option=" + option +
                '}';
    }

    public static class Builder {

        private String id;
        private String displayName;
        private String prefix;
        private String suffix;
        private NameTagVisibility nameTagVisibility;
        private CollisionRules collisionRules;
        private ChatColor color;
        private Option option;

        public Builder() {
        }

        public String id() {
            return id;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public String displayName() {
            return displayName;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public String prefix() {
            return prefix;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public String suffix() {
            return suffix;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public NameTagVisibility nameTagVisibility() {
            return nameTagVisibility;
        }

        public Builder nameTagVisibility(NameTagVisibility nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this;
        }

        public CollisionRules collisionRules() {
            return collisionRules;
        }

        public Builder collisionRules(CollisionRules collisionRules) {
            this.collisionRules = collisionRules;
            return this;
        }

        public ChatColor color() {
            return color;
        }

        public Builder color(ChatColor color) {
            this.color = color;
            return this;
        }

        public Option option() {
            return option;
        }

        public Builder option(Option option) {
            this.option = option;
            return this;
        }

        public TeamData create(){
            return new TeamData(
                    id,
                    displayName,
                    prefix,
                    suffix,
                    nameTagVisibility,
                    collisionRules,
                    color,
                    option
            );
        }

    }

}
