package net.tensura.abyss.commission;

/** Auftragstypen des Shadow-Garden-Commission-Systems. */
public enum CommissionType {
    GATHERING("Gather rare Abyss materials"),
    CRAFTING("Crafting guild equipment"),
    SUBJUGATION("Eliminate threats (Cult of Diablos)"),
    COORDINATE_SUBJUGATION("Travel to the coordinates and defeat the boss"),
    PARTY("Hard party commission (shared EXP)"),
    DWARF("Dwarven commission: workshop expansion & deliveries");

    public final String description;

    CommissionType(String description) {
        this.description = description;
    }
}
