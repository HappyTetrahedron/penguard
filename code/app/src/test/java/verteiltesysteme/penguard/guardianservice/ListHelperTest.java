package verteiltesysteme.penguard.guardianservice;


import junit.framework.Assert;

import org.junit.Test;

import java.util.List;
import java.util.Vector;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class ListHelperTest {

    Vector<Guardian> createTestGuardianArray() {
        Vector<Guardian> guardians = new Vector<>();
        guardians.add(new Guardian("derp", "1.2.3.4", 8909));
        guardians.add(new Guardian("dorp", "1.2.3.4", 8909));
        guardians.add(new Guardian("dirp", "1.2.3.4", 8909));
        guardians.add(new Guardian("durp", "1.2.3.4", 8909));
        return guardians;
    }

    List<PenguardProto.PGPGuardian> createTestProtoGuardianArray() {
        PenguardProto.Group group = PenguardProto.Group.newBuilder()
                .setSeqNo(4)
                .addGuardians(
                        PenguardProto.PGPGuardian.newBuilder()
                        .setName("derp").setIp("1.2.3.4").setPort(8909)
                )
                .addGuardians(
                        PenguardProto.PGPGuardian.newBuilder()
                                .setName("dorp").setIp("1.2.3.4").setPort(8909)
                )
                .addGuardians(
                        PenguardProto.PGPGuardian.newBuilder()
                                .setName("ferp").setIp("1.2.3.4").setPort(8909)
                )
                .addGuardians(
                        PenguardProto.PGPGuardian.newBuilder()
                                .setName("forp").setIp("1.2.3.4").setPort(8909)
                )
                .build();

        return group.getGuardiansList();
    }

    PenguinList createTestPenguinArray() {
        Vector<Penguin> penguins = new Vector<>();
        penguins.add(new Penguin("C1:D4:E4:00:00:00", "tux"));
        penguins.add(new Penguin("C2:D4:E4:00:00:00", "tix"));
        penguins.add(new Penguin("C3:D4:E4:00:00:00", "tex"));
        penguins.add(new Penguin("C4:D4:E4:00:00:00", "tox"));
        PenguinList penguinList = new PenguinList();
        penguins.addAll(penguins);
        return penguinList;
    }

    List<PenguardProto.PGPPenguin> createTestProtoPenguinArray() {
        PenguardProto.Group group = PenguardProto.Group.newBuilder()
                .setSeqNo(0)
                .addPenguins(PenguardProto.PGPPenguin.newBuilder()
                    .setMac("C1:D4:E4:00:00:00").setName("tux")
                )
                .addPenguins(PenguardProto.PGPPenguin.newBuilder()
                        .setMac("C2:D4:E4:00:00:00").setName("tux")
                )
                .addPenguins(PenguardProto.PGPPenguin.newBuilder()
                        .setMac("C1:D3:E4:00:00:00").setName("tux")
                )
                .addPenguins(PenguardProto.PGPPenguin.newBuilder()
                        .setMac("C2:D3:E4:00:00:00").setName("tux")
                )
                .build();

        return group.getPenguinsList();
    }

    @Test
    public void testFindGuardianByName() {
        Vector<Guardian> guardians = createTestGuardianArray();
        Guardian sirup = new Guardian("sirup", "1.2.1.2", 6666);
        guardians.add(sirup);
        Guardian sirup2 = ListHelper.getGuardianByName(guardians, "sirup");
        Assert.assertEquals(sirup, sirup2);
        Assert.assertEquals(sirup.getIp(), sirup2.getIp());
        Assert.assertEquals(sirup.getPort(), sirup2.getPort());
    }

    @Test
    public void testFindGuardianByNameNoMatch() {
        Vector<Guardian> guardians = createTestGuardianArray();
        Guardian sirup2 = ListHelper.getGuardianByName(guardians, "sirup");
        Assert.assertNull(sirup2);
    }

    @Test
    public void testFindPenguinByName() {
        PenguinList penguins = createTestPenguinArray();
        Penguin sirup = new Penguin("AA:BB:CC:DD:EE:FF", "sirup");
        penguins.add(sirup);
        Penguin sirup2 = ListHelper.getPenguinByAddress(penguins, "AA:BB:CC:DD:EE:FF");
        Assert.assertEquals(sirup, sirup2);
        Assert.assertEquals(sirup.getName(), sirup2.getName());
    }

    @Test
    public void testFindPenguinByNameNoMatch() {
        PenguinList penguins = createTestPenguinArray();
        Penguin sirup2 = ListHelper.getPenguinByAddress(penguins, "AA:BB:CC:DD:EE:FF");
        Assert.assertNull(sirup2);
    }

    @Test
    public void testGuardianCopy() {
        Vector<Guardian> guardians = createTestGuardianArray();
        List<PenguardProto.PGPGuardian> protos = createTestProtoGuardianArray();

        ListHelper.copyGuardianListFromProtobufList(guardians, protos);

        Assert.assertNotNull(ListHelper.getGuardianByName(guardians, "derp"));
        Assert.assertNotNull(ListHelper.getGuardianByName(guardians, "dorp"));
        Assert.assertNotNull(ListHelper.getGuardianByName(guardians, "ferp"));
        Assert.assertNotNull(ListHelper.getGuardianByName(guardians, "forp"));

        Assert.assertNull(ListHelper.getGuardianByName(guardians, "dirp"));
        Assert.assertNull(ListHelper.getGuardianByName(guardians, "durp"));
    }

    @Test
    public void testPenguinCopy() {
        PenguinList penguins = createTestPenguinArray();
        List<PenguardProto.PGPPenguin> protos = createTestProtoPenguinArray();

        ListHelper.copyPenguinListFromProtobufList(penguins, protos);

        Assert.assertNotNull(ListHelper.getPenguinByAddress(penguins, "C1:D4:E4:00:00:00"));
        Assert.assertNotNull(ListHelper.getPenguinByAddress(penguins, "C2:D4:E4:00:00:00"));
        Assert.assertNotNull(ListHelper.getPenguinByAddress(penguins, "C1:D3:E4:00:00:00"));
        Assert.assertNotNull(ListHelper.getPenguinByAddress(penguins, "C2:D3:E4:00:00:00"));

        Assert.assertNull(ListHelper.getPenguinByAddress(penguins, "C3:D4:E4:00:00:00"));
        Assert.assertNull(ListHelper.getPenguinByAddress(penguins, "C4:D4:E4:00:00:00"));
    }

    @Test
    public void testGuardianCopyFromEmptyProto() {
        Vector<Guardian> gs = createTestGuardianArray();

        Vector<PenguardProto.PGPGuardian> pros = new Vector<>();

        ListHelper.copyGuardianListFromProtobufList(gs, pros);

        Assert.assertEquals(0, gs.size());
    }

    @Test
    public void testFindGuardianNull() {
        ListHelper.getGuardianByName(null, "halp");
    }

    @Test
    public void testFindPenguinNull() {
        ListHelper.getPenguinByAddress(null, "halp");
    }

    @Test
    public void testFindPenguinProtoNull() {
        ListHelper.getPGPPenguinByAddress(null, "halp");
    }

    @Test
    public void testFindGuardianProtoNull() {
        ListHelper.getPGPGuardianByName(null, "halp");
    }

    @Test
    public void testFindNullStringGuardian() {
        ListHelper.getGuardianByName(createTestGuardianArray(), null);
    }

}
