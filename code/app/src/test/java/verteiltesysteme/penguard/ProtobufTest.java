package verteiltesysteme.penguard;

import org.junit.Test;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class ProtobufTest {

    @Test
    public void CreateMessageTest() {
        PenguardProto.PGPMessage message = PenguardProto.PGPMessage.newBuilder()
                .setName("Anneliese")
                .setType(PenguardProto.PGPMessage.Type.SG_ACK)
                .setAck(PenguardProto.Ack.newBuilder()
                        .setUuid("beef")
                        .build())
                .build();

        System.out.println(message.toString());
    }
}
