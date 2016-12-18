package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Vector;

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
    static Penguin getPenguinByAddress(PenguinList penguins, String mac) {
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
     * Adds all members of a Vector of PGPGuardians to a vector of Guardians
     * @param pgpGuardianVector Vector of which all members shall be added
     * @param guardianVector Vector to which all members shall be added
     */
    static void addPGPGuardianListToGuardianList(Vector<PenguardProto.PGPGuardian> pgpGuardianVector, Vector<Guardian> guardianVector){
        for (PenguardProto.PGPGuardian pgpGuardian : pgpGuardianVector) {
            if (getGuardianByName(guardianVector, pgpGuardian.getName()) == null) { //No corresponding guardian in guardianList
                guardianVector.add(new Guardian(pgpGuardian.getName(), pgpGuardian.getIp(), pgpGuardian.getPort()));
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
    static void copyPenguinListFromProtobufList(PenguinList penguinList, List<PenguardProto.PGPPenguin> protobufList, Context context, PenguinSeenCallback callback) {
        // first, delete all penguins from the list that are not in the protobuf
        for (int i = 0; i < penguinList.size(); i++) {
            Penguin penguin = penguinList.get(i);
            if (getPGPPenguinByAddress(protobufList, penguin.getAddress()) == null) { //No corresponding penguin in protobuf
                penguinList.remove(i);
                i--;
            }
        }

        // now, add all penguins from the protobuf that are not in the list.
        for (PenguardProto.PGPPenguin proto : protobufList) {
            if (getPenguinByAddress(penguinList, proto.getMac()) == null) { //No corresponding penguin in penguinList
                Penguin penguin = new Penguin(proto.getMac(), proto.getName(), context);
                penguin.registerSeenCallback(callback);
                penguinList.add(penguin);

            }
        }
    }

    /**
     * Returns a new list that contains all PGPGuardians both in A and B.
     * @param a first PGPGuardian list
     * @param b second PGPGuardian list
     * @return Merged list of PGPGuardians
     */
    static List<PenguardProto.PGPGuardian> mergeGuardiansList(List<PenguardProto.PGPGuardian> a, List<PenguardProto.PGPGuardian> b) {
        // Add all a's
        List<PenguardProto.PGPGuardian> results = new Vector<>(a);
        // Add all b's that aren't already present
        for (PenguardProto.PGPGuardian g : b) {
            if (getPGPGuardianByName(results, g.getName()) == null) {
                results.add(g);
            }
        }
        return results;
    }

    /**
     * Returns a new list that contains all PGPpenguins both in A and B.
     * @param a first PGPPenguin list
     * @param b second PGPPenguin list
     * @return Merged list of PGPpenguins
     */
    static List<PenguardProto.PGPPenguin> mergePenguinLists(List<PenguardProto.PGPPenguin> a, List<PenguardProto.PGPPenguin> b) {
        // Add all a's
        List<PenguardProto.PGPPenguin> results = new Vector<>(a);
        // Add all b's that aren't already present
        for (PenguardProto.PGPPenguin p : b) {
            if (getPGPPenguinByAddress(results, p.getMac()) == null) {
                results.add(p);
            }
        }
        return results;
    }

    /**
     * Converts a Vector of Penguins in a Vector of PGPPenguins
     * @param penguinlist A Vector of Penguins
     * @return A new Vector of PGPPenguins
     */
    static Vector<PenguardProto.PGPPenguin> convertToPGPPenguinList(PenguinList penguinlist){
        Vector<PenguardProto.PGPPenguin> pgppenguinlist = new Vector<>();
        for (Penguin p : penguinlist){
            PenguardProto.PGPPenguin pgppenguin = PenguardProto.PGPPenguin.newBuilder()
                    .setMac(p.getAddress())
                    .setName(p.getName())
                    .setSeen(p.isSeen())
                    .build();

            pgppenguinlist.add(pgppenguin);
        }
        return pgppenguinlist;
    }


    /**
     * Converts a Vector of Guardians in a Vector of PGPGuardians
     * @param guardianlist A Vector of Penguins
     * @return A new Vector of PGPPenguins
     */
    static Vector<PenguardProto.PGPGuardian> convertToPGPGuardianList(Vector<Guardian> guardianlist){
        Vector<PenguardProto.PGPGuardian> pgpguardianlist = new Vector<>();
        for (Guardian g : guardianlist){
            pgpguardianlist.add(g.toProto());
        }
        return pgpguardianlist;
    }

}
