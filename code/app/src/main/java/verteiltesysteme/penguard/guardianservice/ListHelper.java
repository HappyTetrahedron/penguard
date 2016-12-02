package verteiltesysteme.penguard.guardianservice;

import android.support.annotation.Nullable;

import java.util.List;

import verteiltesysteme.penguard.protobuf.PenguardProto;

final class ListHelper {

    /**
     * Traverses a list of guardians and finds the first guardian with the given name.
     * Returns null if no guardian by that name exists in the list.
     * @param guardians The list of guardians to be searched
     * @param name The name to be searched for
     * @return The first guardian in the list with the given name. Null if no such guardian exists.
     */
    @Nullable
    static Guardian getGuardianByName(List<Guardian> guardians, String name) {
        if (guardians == null) return null; //corner case: can't find anything in null list
        for (Guardian guardian : guardians) {
            if (guardian.getName().equals(name)) return guardian;
        }
        return null;
    }

    /**
     * Traverses a list of PGPGuardians and finds the first guardian with the given name.
     * Returns null if no guardian by that name exists in the list.
     * @param guardians The list of guardians to be searched
     * @param name The name to be searched for
     * @return The first PGPGuardian in the list with the given name. Null if no such guardian exists.
     */
    @Nullable
    static PenguardProto.PGPGuardian getPGPGuardianByName(List<PenguardProto.PGPGuardian> guardians, String name) {
        if (guardians == null) return null; //corner case: can't find anything in null list
        for (PenguardProto.PGPGuardian guardian : guardians) {
            if (guardian.getName().equals(name)) return guardian;
        }
        return null;
    }

    /**
     * Traverses a list of penguins and finds the first penguin with the given mac address.
     * Returns null if no penguin by that address exists in the list.
     * @param penguins The list of penguins to be searched
     * @param mac The address to be searched for
     * @return The first penguin in the list with the given address. Null if no such penguin exists.
     */
    @Nullable
    static Penguin getPenguinByAddress(List<Penguin> penguins, String mac) {
        if (penguins == null) return null; //corner case: can't find anything in null list
        for (Penguin penguin : penguins) {
            if (penguin.getAddress().equals(mac)) return penguin;
        }
        return null;
    }

    /**
     * Traverses a list of PGPPenguins and finds the first penguin with the given mac address.
     * Returns null if no penguin by that address exists in the list.
     * @param penguins The list of PGPPenguins to be searched
     * @param mac The address to be searched for
     * @return The first penguin in the list with the given address. Null if no such penguin exists.
     */
    @Nullable
    static PenguardProto.PGPPenguin getPGPPenguinByAddress(List<PenguardProto.PGPPenguin> penguins, String mac) {
        if (penguins == null) return null; //corner case: can't find anything in null list
        for (PenguardProto.PGPPenguin penguin : penguins) {
            if (penguin.getMac().equals(mac)) return penguin;
        }
        return null;
    }

    /**
     * Updates a list of Guardians to correspond to a list of PGPGuardians without entirely flushing
     * the list, i.e. guardians that are both in the old and new list are not removed and re-inserted.
     * Runs in O(N*M), use with care
     * @param guardianList The list of Guardians to copy to
     * @param protobufList The list of PGPGuardians to copy from
     */
    static void copyGuardianListFromProtobufList(List<Guardian> guardianList, List<PenguardProto.PGPGuardian> protobufList) {
        // first, delete all guardians from the list that are not in the protobuf
        for (int i = 0; i < guardianList.size(); i++) {
            Guardian guardian = guardianList.get(i);
            if (getPGPGuardianByName(protobufList, guardian.getName()) == null) { //No corresponding guardian in protobuf
                guardianList.remove(i);
                i--;
            }
        }

        // now, add all guardians from the protobuf that are not in the list.
        for (PenguardProto.PGPGuardian proto : protobufList) {
            if (getGuardianByName(guardianList, proto.getName()) == null) { //No corresponding guardian in guardianList
                guardianList.add(new Guardian(proto.getName(), proto.getIp(), proto.getPort()));
            }
        }
    }

    /**
     * Updates a list of Penguins to correspond to a list of PGPPenguins without entirely flushing
     * the list, i.e. penguins that are both in the old and new list are not removed and re-inserted.
     * Runs in O(N*M), use with care
     * @param penguinList The list of Penguins to copy to
     * @param protobufList The list of PGPPenguins to copy from
     */
    static void copyPenguinListFromProtobufList(List<Penguin> penguinList, List<PenguardProto.PGPPenguin> protobufList) {
        // first, delete all penguins from the list that are not in the protobuf
        for (int i = 0; i < penguinList.size(); i++) {
            Penguin penguin = penguinList.get(i);
            if (getPGPPenguinByAddress(protobufList, penguin.getAddress()) == null) { //No corresponding guardian in protobuf
                penguinList.remove(i);
                i--;
            }
        }

        // now, add all penguins from the protobuf that are not in the list.
        for (PenguardProto.PGPPenguin proto : protobufList) {
            if (getPenguinByAddress(penguinList, proto.getMac()) == null) { //No corresponding guardian in guardianList
                penguinList.add(new Penguin(proto.getMac(), proto.getName()));
            }
        }
    }

}
