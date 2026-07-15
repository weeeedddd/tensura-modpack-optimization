package net.tensura.abyss.commission;

/** Auftragstypen des Shadow-Garden-Commission-Systems. */
public enum CommissionType {
    GATHERING("Sammeln seltener Abyss-Materialien"),
    CRAFTING("Herstellen von Gilden-Ausruestung"),
    SUBJUGATION("Eliminieren von Bedrohungen (Kult von Diablos)"),
    COORDINATE_SUBJUGATION("Reise zu Koordinaten und besiege den Boss"),
    PARTY("Schwerer Party-Auftrag (geteilte EXP)"),
    DWARF("Zwergen-Auftrag: Werkstatt-Ausbau & Lieferungen");

    public final String description;

    CommissionType(String description) {
        this.description = description;
    }
}
