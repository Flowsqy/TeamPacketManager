package fr.flowsqy.teampacketmanager.commons;

import java.util.Objects;

/**
 * Other options
 */
public final class Option {

    private boolean allowFriendlyFire;
    private boolean canSeeFriendlyInvisible;

    public Option() {
    }

    public Option(boolean allowFriendlyFire, boolean canSeeFriendlyInvisibles) {
        this.allowFriendlyFire = allowFriendlyFire;
        this.canSeeFriendlyInvisible = canSeeFriendlyInvisibles;
    }

    /**
     * Gets the team friendly fire state
     *
     * @return true if friendly fire is enabled
     */
    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    /**
     * Sets the team friendly fire state
     *
     * @param allowFriendlyFire true if friendly fire is to be allowed
     */
    public void setAllowFriendlyFire(boolean allowFriendlyFire) {
        this.allowFriendlyFire = allowFriendlyFire;
    }

    /**
     * Gets the team's ability to see invisible teammates
     *
     * @return true if team members can see invisible members
     */
    public boolean isCanSeeFriendlyInvisible() {
        return canSeeFriendlyInvisible;
    }

    /**
     * Sets the team's ability to see invisible teammates
     *
     * @param canSeeFriendlyInvisible true if invisible teammates are to be visible
     */
    public void setCanSeeFriendlyInvisible(boolean canSeeFriendlyInvisible) {
        this.canSeeFriendlyInvisible = canSeeFriendlyInvisible;
    }

    public int getPackedOption() {
        int options = 0;
        if (allowFriendlyFire)
            options |= 1;

        if (canSeeFriendlyInvisible)
            options |= 2;

        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return allowFriendlyFire == option.allowFriendlyFire &&
                canSeeFriendlyInvisible == option.canSeeFriendlyInvisible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowFriendlyFire, canSeeFriendlyInvisible);
    }

    @Override
    public String toString() {
        return "Option{" +
                "allowFriendlyFire=" + allowFriendlyFire +
                ", canSeeFriendlyInvisible=" + canSeeFriendlyInvisible +
                '}';
    }
}