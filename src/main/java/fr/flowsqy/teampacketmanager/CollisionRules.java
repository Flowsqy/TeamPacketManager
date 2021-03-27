package fr.flowsqy.teampacketmanager;

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