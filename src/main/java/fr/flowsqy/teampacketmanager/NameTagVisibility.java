package fr.flowsqy.teampacketmanager;

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

    public String getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "NameTagVisibility{" +
                "visibility='" + visibility + '\'' +
                "} " + super.toString();
    }
}