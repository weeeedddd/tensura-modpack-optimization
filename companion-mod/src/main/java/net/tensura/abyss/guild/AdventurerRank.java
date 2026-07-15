package net.tensura.abyss.guild;

/** Abenteurer-Raenge F (Anfang) bis S (Spitze), gestaffelt nach Guild-EXP. */
public enum AdventurerRank {
    F(0),
    E(500),
    D(1500),
    C(4000),
    B(10000),
    A(25000),
    S(60000);

    public final int expRequired;

    AdventurerRank(int expRequired) {
        this.expRequired = expRequired;
    }

    /** Hoechster Rang, dessen EXP-Schwelle vom Wert erreicht wird. */
    public static AdventurerRank fromExp(int exp) {
        AdventurerRank best = F;
        for (AdventurerRank r : values()) {
            if (exp >= r.expRequired) best = r;
        }
        return best;
    }

    /** Naechster Rang oder null, wenn bereits S. */
    public AdventurerRank next() {
        int i = ordinal();
        return i + 1 < values().length ? values()[i + 1] : null;
    }
}
