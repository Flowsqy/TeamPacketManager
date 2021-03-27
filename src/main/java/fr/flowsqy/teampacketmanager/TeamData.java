package fr.flowsqy.teampacketmanager;

import org.bukkit.Color;

import java.util.Objects;

public class TeamData {

    private String id;
    private String displayName;
    private String prefix;
    private String suffix;
    private NameTagVisibility nameTagVisibility;
    private CollisionRules collisionRules;
    private Color color;

    public TeamData() {
    }

    public TeamData(String id, String displayName, String prefix, String suffix, NameTagVisibility nameTagVisibility, CollisionRules collisionRules, Color color) {
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRules = collisionRules;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamData teamData = (TeamData) o;
        return Objects.equals(id, teamData.id) && Objects.equals(displayName, teamData.displayName) && Objects.equals(prefix, teamData.prefix) && Objects.equals(suffix, teamData.suffix) && nameTagVisibility == teamData.nameTagVisibility && collisionRules == teamData.collisionRules && Objects.equals(color, teamData.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, prefix, suffix, nameTagVisibility, collisionRules, color);
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
                '}';
    }

    public static class Builder {

        private String id;
        private String displayName;
        private String prefix;
        private String suffix;
        private NameTagVisibility nameTagVisibility;
        private CollisionRules collisionRules;
        private Color color;

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

        public Color color() {
            return color;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        private TeamData create(){
            return new TeamData(
                    id,
                    displayName,
                    prefix,
                    suffix,
                    nameTagVisibility,
                    collisionRules,
                    color
            );
        }

    }

}
