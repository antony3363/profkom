package com.example.transactions_service.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
 * (после резервирования стока под покупку).
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: transactions.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TransactionGrpcServiceGrpc {

  private TransactionGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "profkom.transactions.TransactionGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.transactions_service.grpc.AwardEventRewardRequest,
      com.example.transactions_service.grpc.TransactionInfo> getAwardEventRewardMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AwardEventReward",
      requestType = com.example.transactions_service.grpc.AwardEventRewardRequest.class,
      responseType = com.example.transactions_service.grpc.TransactionInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.transactions_service.grpc.AwardEventRewardRequest,
      com.example.transactions_service.grpc.TransactionInfo> getAwardEventRewardMethod() {
    io.grpc.MethodDescriptor<com.example.transactions_service.grpc.AwardEventRewardRequest, com.example.transactions_service.grpc.TransactionInfo> getAwardEventRewardMethod;
    if ((getAwardEventRewardMethod = TransactionGrpcServiceGrpc.getAwardEventRewardMethod) == null) {
      synchronized (TransactionGrpcServiceGrpc.class) {
        if ((getAwardEventRewardMethod = TransactionGrpcServiceGrpc.getAwardEventRewardMethod) == null) {
          TransactionGrpcServiceGrpc.getAwardEventRewardMethod = getAwardEventRewardMethod =
              io.grpc.MethodDescriptor.<com.example.transactions_service.grpc.AwardEventRewardRequest, com.example.transactions_service.grpc.TransactionInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AwardEventReward"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.AwardEventRewardRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.TransactionInfo.getDefaultInstance()))
              .setSchemaDescriptor(new TransactionGrpcServiceMethodDescriptorSupplier("AwardEventReward"))
              .build();
        }
      }
    }
    return getAwardEventRewardMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.transactions_service.grpc.ChargePurchaseRequest,
      com.example.transactions_service.grpc.TransactionInfo> getChargePurchaseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ChargePurchase",
      requestType = com.example.transactions_service.grpc.ChargePurchaseRequest.class,
      responseType = com.example.transactions_service.grpc.TransactionInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.transactions_service.grpc.ChargePurchaseRequest,
      com.example.transactions_service.grpc.TransactionInfo> getChargePurchaseMethod() {
    io.grpc.MethodDescriptor<com.example.transactions_service.grpc.ChargePurchaseRequest, com.example.transactions_service.grpc.TransactionInfo> getChargePurchaseMethod;
    if ((getChargePurchaseMethod = TransactionGrpcServiceGrpc.getChargePurchaseMethod) == null) {
      synchronized (TransactionGrpcServiceGrpc.class) {
        if ((getChargePurchaseMethod = TransactionGrpcServiceGrpc.getChargePurchaseMethod) == null) {
          TransactionGrpcServiceGrpc.getChargePurchaseMethod = getChargePurchaseMethod =
              io.grpc.MethodDescriptor.<com.example.transactions_service.grpc.ChargePurchaseRequest, com.example.transactions_service.grpc.TransactionInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ChargePurchase"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.ChargePurchaseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.TransactionInfo.getDefaultInstance()))
              .setSchemaDescriptor(new TransactionGrpcServiceMethodDescriptorSupplier("ChargePurchase"))
              .build();
        }
      }
    }
    return getChargePurchaseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.transactions_service.grpc.RefundPurchaseRequest,
      com.example.transactions_service.grpc.TransactionInfo> getRefundPurchaseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RefundPurchase",
      requestType = com.example.transactions_service.grpc.RefundPurchaseRequest.class,
      responseType = com.example.transactions_service.grpc.TransactionInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.transactions_service.grpc.RefundPurchaseRequest,
      com.example.transactions_service.grpc.TransactionInfo> getRefundPurchaseMethod() {
    io.grpc.MethodDescriptor<com.example.transactions_service.grpc.RefundPurchaseRequest, com.example.transactions_service.grpc.TransactionInfo> getRefundPurchaseMethod;
    if ((getRefundPurchaseMethod = TransactionGrpcServiceGrpc.getRefundPurchaseMethod) == null) {
      synchronized (TransactionGrpcServiceGrpc.class) {
        if ((getRefundPurchaseMethod = TransactionGrpcServiceGrpc.getRefundPurchaseMethod) == null) {
          TransactionGrpcServiceGrpc.getRefundPurchaseMethod = getRefundPurchaseMethod =
              io.grpc.MethodDescriptor.<com.example.transactions_service.grpc.RefundPurchaseRequest, com.example.transactions_service.grpc.TransactionInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RefundPurchase"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.RefundPurchaseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.transactions_service.grpc.TransactionInfo.getDefaultInstance()))
              .setSchemaDescriptor(new TransactionGrpcServiceMethodDescriptorSupplier("RefundPurchase"))
              .build();
        }
      }
    }
    return getRefundPurchaseMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TransactionGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceStub>() {
        @java.lang.Override
        public TransactionGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TransactionGrpcServiceStub(channel, callOptions);
        }
      };
    return TransactionGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TransactionGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceBlockingStub>() {
        @java.lang.Override
        public TransactionGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TransactionGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return TransactionGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TransactionGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TransactionGrpcServiceFutureStub>() {
        @java.lang.Override
        public TransactionGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TransactionGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return TransactionGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
   * (после резервирования стока под покупку).
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void awardEventReward(com.example.transactions_service.grpc.AwardEventRewardRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAwardEventRewardMethod(), responseObserver);
    }

    /**
     */
    default void chargePurchase(com.example.transactions_service.grpc.ChargePurchaseRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getChargePurchaseMethod(), responseObserver);
    }

    /**
     */
    default void refundPurchase(com.example.transactions_service.grpc.RefundPurchaseRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRefundPurchaseMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TransactionGrpcService.
   * <pre>
   * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
   * (после резервирования стока под покупку).
   * </pre>
   */
  public static abstract class TransactionGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TransactionGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TransactionGrpcService.
   * <pre>
   * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
   * (после резервирования стока под покупку).
   * </pre>
   */
  public static final class TransactionGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TransactionGrpcServiceStub> {
    private TransactionGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TransactionGrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void awardEventReward(com.example.transactions_service.grpc.AwardEventRewardRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAwardEventRewardMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void chargePurchase(com.example.transactions_service.grpc.ChargePurchaseRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getChargePurchaseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void refundPurchase(com.example.transactions_service.grpc.RefundPurchaseRequest request,
        io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRefundPurchaseMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TransactionGrpcService.
   * <pre>
   * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
   * (после резервирования стока под покупку).
   * </pre>
   */
  public static final class TransactionGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TransactionGrpcServiceBlockingStub> {
    private TransactionGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TransactionGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.example.transactions_service.grpc.TransactionInfo awardEventReward(com.example.transactions_service.grpc.AwardEventRewardRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAwardEventRewardMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.example.transactions_service.grpc.TransactionInfo chargePurchase(com.example.transactions_service.grpc.ChargePurchaseRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getChargePurchaseMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.example.transactions_service.grpc.TransactionInfo refundPurchase(com.example.transactions_service.grpc.RefundPurchaseRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRefundPurchaseMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TransactionGrpcService.
   * <pre>
   * Вызывается check_in_service (после чек-ина на мероприятие) и shop_service
   * (после резервирования стока под покупку).
   * </pre>
   */
  public static final class TransactionGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TransactionGrpcServiceFutureStub> {
    private TransactionGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TransactionGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TransactionGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.transactions_service.grpc.TransactionInfo> awardEventReward(
        com.example.transactions_service.grpc.AwardEventRewardRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAwardEventRewardMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.transactions_service.grpc.TransactionInfo> chargePurchase(
        com.example.transactions_service.grpc.ChargePurchaseRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getChargePurchaseMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.transactions_service.grpc.TransactionInfo> refundPurchase(
        com.example.transactions_service.grpc.RefundPurchaseRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRefundPurchaseMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_AWARD_EVENT_REWARD = 0;
  private static final int METHODID_CHARGE_PURCHASE = 1;
  private static final int METHODID_REFUND_PURCHASE = 2;

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
        case METHODID_AWARD_EVENT_REWARD:
          serviceImpl.awardEventReward((com.example.transactions_service.grpc.AwardEventRewardRequest) request,
              (io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo>) responseObserver);
          break;
        case METHODID_CHARGE_PURCHASE:
          serviceImpl.chargePurchase((com.example.transactions_service.grpc.ChargePurchaseRequest) request,
              (io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo>) responseObserver);
          break;
        case METHODID_REFUND_PURCHASE:
          serviceImpl.refundPurchase((com.example.transactions_service.grpc.RefundPurchaseRequest) request,
              (io.grpc.stub.StreamObserver<com.example.transactions_service.grpc.TransactionInfo>) responseObserver);
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
          getAwardEventRewardMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.transactions_service.grpc.AwardEventRewardRequest,
              com.example.transactions_service.grpc.TransactionInfo>(
                service, METHODID_AWARD_EVENT_REWARD)))
        .addMethod(
          getChargePurchaseMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.transactions_service.grpc.ChargePurchaseRequest,
              com.example.transactions_service.grpc.TransactionInfo>(
                service, METHODID_CHARGE_PURCHASE)))
        .addMethod(
          getRefundPurchaseMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.transactions_service.grpc.RefundPurchaseRequest,
              com.example.transactions_service.grpc.TransactionInfo>(
                service, METHODID_REFUND_PURCHASE)))
        .build();
  }

  private static abstract class TransactionGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TransactionGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.transactions_service.grpc.Transactions.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TransactionGrpcService");
    }
  }

  private static final class TransactionGrpcServiceFileDescriptorSupplier
      extends TransactionGrpcServiceBaseDescriptorSupplier {
    TransactionGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class TransactionGrpcServiceMethodDescriptorSupplier
      extends TransactionGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TransactionGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (TransactionGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TransactionGrpcServiceFileDescriptorSupplier())
              .addMethod(getAwardEventRewardMethod())
              .addMethod(getChargePurchaseMethod())
              .addMethod(getRefundPurchaseMethod())
              .build();
        }
      }
    }
    return result;
  }
}
