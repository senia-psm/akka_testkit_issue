package info.senia.akka.testkit;

import akka.actor.testkit.typed.Effect;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.internal.AdaptWithRegisteredMessageAdapter;
import akka.actor.typed.javadsl.Behaviors;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestKitTests {

    private interface Internal {
    }

    public static class InternalResponse implements Internal {
    }

    @Value
    public static class SendRequest implements Internal {
        ActorRef<Request> service;
    }

    public static class ExternalResponse {
    }

    @Value
    public static class Request {
        ActorRef<ExternalResponse> replyTo;
    }

    final Behavior<Request> serviceBehavior =
            Behaviors.receiveMessage(req -> {
                req.getReplyTo().tell(new ExternalResponse());
                return Behaviors.same();
            });

    final Behavior<Internal> clientBehavior =
            Behaviors
                    .receive(Internal.class)
                    .onMessage(SendRequest.class, (ctx, msg) -> {
                        final ActorRef<ExternalResponse> adapter =
                                ctx.messageAdapter(ExternalResponse.class, externalResponse -> new InternalResponse());

                        msg.getService().tell(new Request(adapter));
                        return Behaviors.same();
                    })
                    .onMessage(InternalResponse.class, (ctx, msg) -> Behaviors.same())
                    .build();

    @Test
    public void testFailed() {
        final BehaviorTestKit<Request> serviceTestKit = BehaviorTestKit.create(serviceBehavior);
        final BehaviorTestKit<Internal> clientTestKit = BehaviorTestKit.create(clientBehavior);

        clientTestKit.run(new SendRequest(serviceTestKit.getRef()));
        serviceTestKit.runOne();

        clientTestKit.runOne();

        assertNotEquals(clientTestKit.returnedBehavior(), Behaviors.unhandled());
    }

    @Test
    public void successTest() {
        final BehaviorTestKit<Request> serviceTestKit = BehaviorTestKit.create(serviceBehavior);
        final BehaviorTestKit<Internal> clientTestKit = BehaviorTestKit.create(clientBehavior);

        clientTestKit.run(new SendRequest(serviceTestKit.getRef()));

        @SuppressWarnings("unchecked") final Effect.MessageAdapter<ExternalResponse, InternalResponse> adapter =
                clientTestKit.expectEffectClass(Effect.MessageAdapter.class);

        serviceTestKit.runOne();

        @SuppressWarnings("unchecked") final ExternalResponse msg =
                ((AdaptWithRegisteredMessageAdapter<ExternalResponse>) (Object) clientTestKit.selfInbox().getAllReceived().get(0)).msg();

        clientTestKit.run(adapter.adaptFunction().apply(msg));

        assertNotEquals(clientTestKit.returnedBehavior(), Behaviors.unhandled());
    }
}
