package verteiltesysteme.penguard;

import org.junit.Test;

import verteiltesysteme.penguard.protobuf.PenguardProto;

public class ProtobufTest {

    @Test
    public void CreateMessageTest() {
        PenguardProto.Message message = PenguardProto.Message.newBuilder()
                .setName("Anneliese")
                .setType(PenguardProto.Message.Type.G_ACK)
                .setAck(PenguardProto.Ack.newBuilder()
                        .setUuid("beef")
                        .build())
                .build();

        System.out.println(message.toString());
    }
}
