package com.example.events_service.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Вызывается check_in_service при чек-ине на мероприятие.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: event.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EventGrpcServiceGrpc {

  private EventGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "profkom.events.EventGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.events_service.grpc.GetEventRequest,
      com.example.events_service.grpc.EventInfo> getGetEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEvent",
      requestType = com.example.events_service.grpc.GetEventRequest.class,
      responseType = com.example.events_service.grpc.EventInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.events_service.grpc.GetEventRequest,
      com.example.events_service.grpc.EventInfo> getGetEventMethod() {
    io.grpc.MethodDescriptor<com.example.events_service.grpc.GetEventRequest, com.example.events_service.grpc.EventInfo> getGetEventMethod;
    if ((getGetEventMethod = EventGrpcServiceGrpc.getGetEventMethod) == null) {
      synchronized (EventGrpcServiceGrpc.class) {
        if ((getGetEventMethod = EventGrpcServiceGrpc.getGetEventMethod) == null) {
          EventGrpcServiceGrpc.getGetEventMethod = getGetEventMethod =
              io.grpc.MethodDescriptor.<com.example.events_service.grpc.GetEventRequest, com.example.events_service.grpc.EventInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.events_service.grpc.GetEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.events_service.grpc.EventInfo.getDefaultInstance()))
              .setSchemaDescriptor(new EventGrpcServiceMethodDescriptorSupplier("GetEvent"))
              .build();
        }
      }
    }
    return getGetEventMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EventGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceStub>() {
        @java.lang.Override
        public EventGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventGrpcServiceStub(channel, callOptions);
        }
      };
    return EventGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EventGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceBlockingStub>() {
        @java.lang.Override
        public EventGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return EventGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EventGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EventGrpcServiceFutureStub>() {
        @java.lang.Override
        public EventGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EventGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return EventGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Вызывается check_in_service при чек-ине на мероприятие.
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void getEvent(com.example.events_service.grpc.GetEventRequest request,
        io.grpc.stub.StreamObserver<com.example.events_service.grpc.EventInfo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetEventMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service EventGrpcService.
   * <pre>
   * Вызывается check_in_service при чек-ине на мероприятие.
   * </pre>
   */
  public static abstract class EventGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return EventGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service EventGrpcService.
   * <pre>
   * Вызывается check_in_service при чек-ине на мероприятие.
   * </pre>
   */
  public static final class EventGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<EventGrpcServiceStub> {
    private EventGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventGrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void getEvent(com.example.events_service.grpc.GetEventRequest request,
        io.grpc.stub.StreamObserver<com.example.events_service.grpc.EventInfo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetEventMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service EventGrpcService.
   * <pre>
   * Вызывается check_in_service при чек-ине на мероприятие.
   * </pre>
   */
  public static final class EventGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<EventGrpcServiceBlockingStub> {
    private EventGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.example.events_service.grpc.EventInfo getEvent(com.example.events_service.grpc.GetEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetEventMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service EventGrpcService.
   * <pre>
   * Вызывается check_in_service при чек-ине на мероприятие.
   * </pre>
   */
  public static final class EventGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<EventGrpcServiceFutureStub> {
    private EventGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EventGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.events_service.grpc.EventInfo> getEvent(
        com.example.events_service.grpc.GetEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetEventMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_EVENT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_EVENT:
          serviceImpl.getEvent((com.example.events_service.grpc.GetEventRequest) request,
              (io.grpc.stub.StreamObserver<com.example.events_service.grpc.EventInfo>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.events_service.grpc.GetEventRequest,
              com.example.events_service.grpc.EventInfo>(
                service, METHODID_GET_EVENT)))
        .build();
  }

  private static abstract class EventGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EventGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.events_service.grpc.Event.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EventGrpcService");
    }
  }

  private static final class EventGrpcServiceFileDescriptorSupplier
      extends EventGrpcServiceBaseDescriptorSupplier {
    EventGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class EventGrpcServiceMethodDescriptorSupplier
      extends EventGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    EventGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EventGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EventGrpcServiceFileDescriptorSupplier())
              .addMethod(getGetEventMethod())
              .build();
        }
      }
    }
    return result;
  }
}
