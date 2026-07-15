package net.tensura.abyss.guild;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Session-basierte Party (Trupp). Nicht persistiert — loest sich beim Server-Stop auf. */
public class Party {
    public UUID leader;
    public final Set<UUID> members = new LinkedHashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public int size() {
        return members.size();
    }
}
