package net.tensura.abyss.guild;

/** Interner Gilden-Rang eines Mitglieds. */
public enum GuildRank {
    LEADER,   // Anfuehrer
    VICE,     // Vize
    MEMBER;   // Mitglied

    public String display() {
        return switch (this) {
            case LEADER -> "Leader";
            case VICE -> "Vice";
            case MEMBER -> "Member";
        };
    }
}
